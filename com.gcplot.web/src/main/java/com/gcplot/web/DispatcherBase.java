package com.gcplot.web;

import com.gcplot.commons.ConfigProperty;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.model.account.Account;
import com.gcplot.repository.AccountRepository;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         12/4/16
 */
public class DispatcherBase {
    private static final Logger LOG = LoggerFactory.getLogger(DispatcherBase.class);
    protected volatile boolean isOpen = false;
    protected boolean blocking = false;
    protected boolean requireAuth = true;
    protected boolean allowNotConfirmed = false;
    protected String[] mimeTypes;
    protected BiConsumer<Throwable, RequestContext> exceptionHandler = (r, q) -> {};
    protected Predicate<RequestContext> preHandler = r -> true;
    protected Consumer<RequestContext> postHandler = r -> {};
    protected Predicate<RequestContext> filter;
    protected Supplier<String> filterMessage;
    protected long maxUploadSize;
    protected AccountRepository accountRepository;
    protected ConfigurationManager config;
    protected String host;
    protected int port;

    protected <T> void preHandle(BiConsumer<T, RequestContext> handler, boolean auth,
                             boolean allowNotConfirmed, Predicate<RequestContext> filter, Supplier<String> fm,
                             T rc, RequestContext c) {
        if (preHandler.test(c)) {
            if (c.loginInfo().isPresent() && c.loginInfo().get().getAccount().isBlocked()) {
                c.finish(ErrorMessages.buildJson(ErrorMessages.USER_IS_BLOCKED));
            } else {
                if (auth && !c.loginInfo().isPresent()) {
                    c.finish(ErrorMessages.buildJson(ErrorMessages.NOT_AUTHORISED));
                } else {
                    if (auth && !allowNotConfirmed && config.readBoolean(ConfigProperty.CONFIRMATION_IS_RESTRICTED)
                            && !c.loginInfo().get().getAccount().isConfirmed()) {
                        c.finish(ErrorMessages.buildJson(ErrorMessages.ACCOUNT_NOT_CONFIRMED));
                    } else if (filter == null || filter.test(c)) {
                        handler.accept(rc, c);
                    } else if (!c.isFinished()) {
                        c.finish(ErrorMessages.buildJson(ErrorMessages.REQUEST_FILTERED,
                                Strings.nullToEmpty(fm != null ? fm.get() : "")));
                    }
                }
            }
        }
        try {
            if (c.loginInfo().isPresent()) {
                Account account = c.loginInfo().get().getAccount();
                if (!account.ips().contains(c.getIp())) {
                    accountRepository.attachNewIp(account, c.getIp());
                }
            }
        } catch (Throwable t) {
            LOG.error("IP Attach Failed: " + t.getMessage(), t);
        }
    }

    protected void reset() {
        blocking = false;
        allowNotConfirmed = false;
        requireAuth = true;
        filter = null;
        filterMessage = null;
        mimeTypes = null;
    }

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }
    public void setAccountRepository(AccountRepository userService) {
        this.accountRepository = userService;
    }

    public ConfigurationManager getConfig() {
        return config;
    }
    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    public long getMaxUploadSize() {
        return maxUploadSize;
    }
    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }
}
