package com.gcplot.services.cluster;

import com.gcplot.commons.exceptions.Exceptions;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/10/17
 */
public class ZookeeperConnector {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperConnector.class);
    private ZooKeeper client;
    private int port;
    private String host;
    private int sessionTimeout;
    private String uid;
    private String secret;
    private String secretDigest;
    private List<Watcher> watchers = new CopyOnWriteArrayList<>();
    private List<ACL> acls = Collections.emptyList();

    public void init() throws Exception {
        LOG.info("ZC: init()");
        client = new ZooKeeper(host + ":" + port, sessionTimeout, event -> watchers.forEach(w -> {
            try {
                w.process(event);
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }));
        client.addAuthInfo("digest", (uid + ":" + secret).getBytes());
        secretDigest = DigestAuthenticationProvider.generateDigest(secret);
        acls = Collections.singletonList(new ACL(ZooDefs.Perms.ALL, new Id("digest", uid + ":" + secretDigest)));
    }

    public void destroy() {
        LOG.info("ZC: destroy()");
        if (client != null) {
            try {
                client.close();
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }
    }

    public String create(String path) {
        return create(path, new byte[0], CreateMode.PERSISTENT);
    }

    public String create(String path, byte[] data) {
        return create(path, data, CreateMode.PERSISTENT);
    }

    public String create(String path, CreateMode mode) {
        return create(path, new byte[0], mode);
    }

    public String create(String path, byte[] data, CreateMode mode) {
        try {
            return client.create(path, data, acls, mode);
        } catch (KeeperException | InterruptedException e) {
            throw Exceptions.runtime(e);
        }
    }

    public void addWatcher(Watcher watcher) {
        this.watchers.add(watcher);
    }

    public void removeWatcher(Watcher watcher) {
        this.watchers.remove(watcher);
    }

    public ZooKeeper getClient() {
        return client;
    }

    public String getSecretDigest() {
        return secretDigest;
    }

    public List<ACL> getAcls() {
        return acls;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
