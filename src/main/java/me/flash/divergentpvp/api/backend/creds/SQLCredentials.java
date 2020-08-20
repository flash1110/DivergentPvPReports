package me.flash.divergentpvp.api.backend.creds;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;

public class SQLCredentials {

    @Getter public String host, database, username, password;
    @Getter public int port;

    public SQLCredentials(String host, int port, String username, String password, String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }
}
