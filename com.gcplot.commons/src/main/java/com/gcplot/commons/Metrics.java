package com.gcplot.commons;

public abstract class Metrics {

    public static String name(Class clazz, String... parts) {
        return name(clazz.getSimpleName(), parts);
    }

    public static String name(String part, String... parts) {
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.getHostName()).append(".").append(part).append(".");
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i < parts.length - 1)
                sb.append(".");
        }
        return sb.toString();
    }

}
