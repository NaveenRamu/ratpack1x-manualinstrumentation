package com.datastreaming.module;

import com.datastreaming.handler.AuthHandler;
import com.datastreaming.handler.GlobalHandler;
import com.datastreaming.otel.OpenTelemetryConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GlobalHandler.class).in(Scopes.SINGLETON);
        bind(OpenTelemetryConfiguration.class).in(Scopes.SINGLETON);
        bind(AuthHandler.class).in(Scopes.SINGLETON);
    }
}
