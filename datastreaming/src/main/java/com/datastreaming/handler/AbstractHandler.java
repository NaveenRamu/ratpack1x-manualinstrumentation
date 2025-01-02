package com.datastreaming.handler;

import com.datastreaming.otel.OpenTelemetryConfiguration;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;

public abstract class AbstractHandler implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);
    protected abstract void handleRequest(Context context) throws Exception;
    protected OpenTelemetryConfiguration openTelemetryConfiguration;

    @Override
    public void handle(Context context) throws Exception {
        openTelemetryConfiguration = context.get(OpenTelemetryConfiguration.class);
        logger.info("Previous span in Abstract handler {}", Span.current());
        Span currentSpan =
                openTelemetryConfiguration
                        .getTracer()
                        .spanBuilder("AbstractHandler.handle-" + context.getRequest().getPath())
                        .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
                        .startSpan();
        logger.info("Current span in Abstract handler {}", currentSpan);
        try (Scope scope = currentSpan.makeCurrent()) {
        handleRequest(context);
            currentSpan.setStatus(StatusCode.OK);
        } catch (Throwable t) {
            currentSpan.setStatus(StatusCode.ERROR, "AbstractHandler.handle() error");
        } finally {
            currentSpan.end();
        }
    }
}