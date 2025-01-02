/*
 * Copyright 2023 Open Text.
 *
 * The only warranties for products and services of Open Text and its affiliates and licensors (“Open Text”)
 * are as may be set forth in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty. Open Text shall not be liable
 * for technical or editorial errors or omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains confidential information and a valid
 * license is required for possession, use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software, Computer Software
 * Documentation, and Technical Data for Commercial Items are licensed to the U.S. Government under
 * vendor’s standard commercial license.
 */

package com.datastreaming.handler;

import com.datastreaming.otel.OpenTelemetryConfiguration;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.http.Headers;

public class AuthHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    protected OpenTelemetryConfiguration openTelemetryConfiguration;

    @Override
    public void handleRequest(Context ctx) {
        openTelemetryConfiguration = ctx.get(OpenTelemetryConfiguration.class);
        logger.info("Previous span in auth handler {}", Span.current());
        Span currentSpan =
                openTelemetryConfiguration
                        .getTracer()
                        .spanBuilder("AuthHandler.handleRequest-" + ctx.getRequest().getPath())
                        .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
                        .startSpan();
        logger.info("Current span in auth handler {}", currentSpan);
        try (Scope scope = currentSpan.makeCurrent()) {
            currentSpan.setAttribute("http.method", ctx.getRequest().getMethod().getName());
            currentSpan.setAttribute("http.url", ctx.getRequest().getPath());
            validateRequestToken(ctx, "Need a valid token in request header");
            currentSpan.setStatus(StatusCode.OK);
        } catch (Throwable t) {
            currentSpan.setStatus(StatusCode.ERROR, "AuthHandler.handleRequest() error");
        } finally {
            currentSpan.end();
        }
    }

    private void validateRequestToken(Context ctx, String errorMsg) {
        Span currentSpan =
                openTelemetryConfiguration
                        .getTracer()
                        .spanBuilder("AuthHandler.validateRequestToken-" + ctx.getRequest().getPath())
                        .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
                        .startSpan();
        try (Scope scope = currentSpan.makeCurrent()) {
            currentSpan.setAttribute("http.method", ctx.getRequest().getMethod().getName());
            currentSpan.setAttribute("http.url", ctx.getRequest().getPath());
            Headers headers = ctx.getRequest().getHeaders();
            Promise<String> stringPromise = validatePromise();
            stringPromise
                    .onError(
                            err -> {
                                ctx.error(err);
                            })
                    .then(
                            t -> {
                                ctx.next();
                            });
            currentSpan.setStatus(StatusCode.OK);
        } catch (Throwable t) {
            currentSpan.setStatus(StatusCode.ERROR, "AuthHandler.validateRequestToken() error");
        } finally {
            currentSpan.end();
        }
    }


    private Promise<String> validatePromise() {
        try {
            System.out.println("Testing promise");
        } catch (Exception e) {
            return Promise.error(e);
        }
        return Promise.value("valid");
    }
}
