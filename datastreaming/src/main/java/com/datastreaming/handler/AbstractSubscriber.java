package com.datastreaming.handler;

import com.datastreaming.otel.OpenTelemetryConfiguration;
import io.netty.buffer.ByteBuf;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;

public abstract class AbstractSubscriber implements Subscriber<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(AbstractSubscriber.class);
    protected Context ctx;
    protected Subscription subscription;
    protected boolean isError;
    protected Throwable cause;
    protected String topicName;
    Span current;
    protected OpenTelemetryConfiguration openTelemetryConfiguration;
    protected AbstractSubscriber(Context ctx) {
        openTelemetryConfiguration = ctx.get(OpenTelemetryConfiguration.class);
        Span childSpan = openTelemetryConfiguration.getTracer().spanBuilder("AbstractSubscriber.handleRequest")
                .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
                .startSpan();
        logger.info("abstract span {}", childSpan);
        current = childSpan;
        try (Scope ignored = childSpan.makeCurrent()) {
            this.ctx = ctx;
        } catch (Throwable t) {
            childSpan.setStatus(StatusCode.ERROR, "AbstractSubscriber.initialization child span error");
        } finally {
            childSpan.end();
        }
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        Span span = openTelemetryConfiguration.getTracer().spanBuilder("AbstractSubscriber.onSubscribe")
                .setParent(io.opentelemetry.context.Context.current().with(current))
                .startSpan();
        logger.info("onSubscribe {}", span);
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("http.method", ctx.getRequest().getMethod().getName());
            span.setAttribute("http.url", ctx.getRequest().getPath());
            this.subscription = subscription;
            subscription.request(1);
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR, "AbstractSubscriber.onSubscribe child span error");
        } finally {
            span.end();
        }
    }

    @Override
    public void onNext(ByteBuf byteBuf) {
        Span span = openTelemetryConfiguration.getTracer().spanBuilder("AbstractSubscriber.onNext")
                .setParent(io.opentelemetry.context.Context.current().with(current))
                .startSpan();
        logger.info("onNext {}", span);
        try (Scope scope = span.makeCurrent()) {
            process(byteBuf);
            if (!isError) {
                byteBuf.release();
                subscription.request(1);
            } else {
                byteBuf.release();
                subscription.cancel();
                ctx.error(cause);
            }
            OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
            openTelemetry.getPropagators();
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR, "handle onNext error");
        } finally {
            span.end();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.debug("onError");
        ctx.error(throwable);
    }

    @Override
    public void onComplete() {
        Span span = openTelemetryConfiguration.getTracer().spanBuilder("AbstractSubscriber.onComplete")
                .setParent(io.opentelemetry.context.Context.current().with(current))
                .startSpan();
        logger.info("onComplete {}", span);
        try (Scope scope = span.makeCurrent()) {
            requestComplete();
            ctx.getResponse().status(200).send("Success");
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR, "handle biz error");
        } finally {
            span.end();
            current.end();
        }
    }

    protected abstract void process(ByteBuf byteBuf);

    protected void error(Throwable throwable) {
        isError = true;
        cause = throwable;
    }

    protected abstract void requestComplete();

    public String getTopicName() {
        return topicName;
    }

}
