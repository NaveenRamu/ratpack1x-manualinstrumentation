package com.datastreaming;

import com.datastreaming.handler.AuthHandler;
import com.datastreaming.handler.GlobalHandler;
import com.datastreaming.module.AppModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.guice.Guice;
import ratpack.server.RatpackServer;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);


    public static void main(String[] args) throws Exception {

        RatpackServer.start(
                server -> {
                    server
                            .serverConfig(
                                    serverConfigBuilder -> {
                                        int port = Integer.parseInt("5050");
                                        boolean devMode =
                                                Boolean.parseBoolean("true");
                                        String protocol = "http";
                                        serverConfigBuilder.maxChunkSize(8 * 1024);
                                        serverConfigBuilder.port(port);
                                        serverConfigBuilder.development(devMode);
                                        serverConfigBuilder.threads(16);
                                    }).registry(Guice.registry(
                                    bindingsSpec ->
                                            bindingsSpec
                                                    .module(AppModule.class)))
                            .handlers(
                                    chain -> {
                                       // chain.all(TracedHandler.class);
                                        chain.all(AuthHandler.class);
                                        chain.all(GlobalHandler.class);
                                    });
                });
    }
}
