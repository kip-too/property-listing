package com.hosea.config;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

public class DatabaseConfig {

    public static Pool createPool(Vertx vertx) {
        //connection options
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost("localhost")
                .setPort(5432)
                .setDatabase("propertydb")
                .setUser("devuser")
                .setPassword("strongpass");
         //create client pool
        return PgBuilder.pool()
                .with(new PoolOptions().setMaxSize(10))
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
    }
}
