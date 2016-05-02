package com.gcplot.accounts.orientdb;

public class OrientDbConfig {

    public final String connectionString;
    public final String user;
    public final String password;

    public OrientDbConfig(String connectionString, String user, String password) {
        this.connectionString = connectionString;
        this.user = user;
        this.password = password;
    }

}
