package com.gcplot.cluster;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/9/17
 */
public class Worker {
    private final String hostsGroup;
    private final String hostname;
    private final String hostAddress;

    public Worker(String hostsGroup, String hostname, String hostAddress) {
        this.hostsGroup = hostsGroup;
        this.hostname = hostname;
        this.hostAddress = hostAddress;
    }

    public String getHostsGroup() {
        return hostsGroup;
    }

    public String getHostname() {
        return hostname;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Worker worker = (Worker) o;

        if (hostsGroup != null ? !hostsGroup.equals(worker.hostsGroup) : worker.hostsGroup != null) return false;
        if (hostname != null ? !hostname.equals(worker.hostname) : worker.hostname != null) return false;
        return hostAddress != null ? hostAddress.equals(worker.hostAddress) : worker.hostAddress == null;
    }

    @Override
    public int hashCode() {
        int result = hostsGroup != null ? hostsGroup.hashCode() : 0;
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        result = 31 * result + (hostAddress != null ? hostAddress.hashCode() : 0);
        return result;
    }
}
