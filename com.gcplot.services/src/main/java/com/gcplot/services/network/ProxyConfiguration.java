package com.gcplot.services.network;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/22/17
 */
public class ProxyConfiguration {
    public static final ProxyConfiguration NONE = new ProxyConfiguration(ProxyType.NONE, null, 0, null, null);
    private final ProxyType proxyType;
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public ProxyType getProxyType() {
        return proxyType;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ProxyConfiguration(ProxyType proxyType, String host, int port) {
        this(proxyType, host, port, null, null);
    }

    public ProxyConfiguration(ProxyType proxyType, String host, int port, String username, String password) {
        this.proxyType = proxyType;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProxyConfiguration that = (ProxyConfiguration) o;

        if (port != that.port) return false;
        if (proxyType != that.proxyType) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        return password != null ? password.equals(that.password) : that.password == null;
    }

    @Override
    public int hashCode() {
        int result = proxyType != null ? proxyType.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProxyConfiguration{" +
                "proxyType=" + proxyType +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
