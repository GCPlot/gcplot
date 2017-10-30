package com.gcplot.services.network;

import com.gcplot.configuration.ConfigProperty;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.utils.Exceptions;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/22/17
 */
public class GraphiteSender {
    private EventLoopGroup eventLoopGroup;
    private SslContext sslContext;
    private Cache<Pair<String, ProxyConfiguration>, Bootstrap> bootstrapCache;
    private ConfigurationManager config;

    public void init() {
        eventLoopGroup = new NioEventLoopGroup(config.readInt(ConfigProperty.GRAPHITE_EVENT_LOOP_POOL_SIZE),
                new ThreadFactoryBuilder().setNameFormat("graph-%d").build());
        bootstrapCache = Caffeine.newBuilder()
                .maximumSize(config.readInt(ConfigProperty.GRAPHITE_EVENT_LOOP_POOL_SIZE))
                .build();
        try {
            sslContext = SslContextBuilder.forClient().build();
        } catch (SSLException e) {
            throw Exceptions.runtime(e);
        }
    }

    public void destroy() {
        try {
            eventLoopGroup.shutdownGracefully(1, 1, TimeUnit.MINUTES);
        } catch (Throwable ignore) { }
    }

    public void send(String url, ProxyConfiguration pc, Map<String, Long> data) {
        if (!(eventLoopGroup.isShuttingDown() || eventLoopGroup.isShutdown())) {
            String[] parts = url.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid URL: " + url);
            }
            int port = Integer.parseInt(parts[1]);
            Bootstrap b = bootstrapCache.get(Pair.of(url, pc), k -> {
                Bootstrap bb = new Bootstrap()
                        .group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true);

                if (pc != ProxyConfiguration.NONE) {
                    InetSocketAddress sa = new InetSocketAddress(pc.getHost(), pc.getPort());
                    switch (pc.getProxyType()) {
                        case SOCKS5: {
                            bb.handler(new Socks5ProxyHandler(sa, pc.getUsername(), pc.getPassword()));
                            break;
                        }
                        case HTTP:
                        case HTTPS: {
                            if (pc.getProxyType() == ProxyType.HTTPS) {
                                bb.handler(sslContext.newHandler(PooledByteBufAllocator.DEFAULT));
                            }
                            HttpProxyHandler hph = pc.getUsername() != null ? new HttpProxyHandler(sa, pc.getUsername(), pc.getPassword()) :
                                    new HttpProxyHandler(sa);
                            bb.handler(hph);
                            break;
                        }
                        default:
                    }
                }
                bb.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                    }
                });
                return bb;
            });
            try {
                Channel c = b.connect(parts[0], port).sync().channel();
                data.forEach((metric, timestamp) -> {
                    // TODO optimize
                    c.write(Unpooled.copiedBuffer(metric + " " + (timestamp / 1000) + "\n", Charsets.ISO_8859_1));
                });
                c.flush().closeFuture().sync();
            } catch (InterruptedException e) {
                throw Exceptions.runtime(e);
            }
        }
    }

    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }
}
