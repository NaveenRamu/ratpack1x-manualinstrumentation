package com.datastreaming.handler;

import com.datastreaming.otel.OpenTelemetryConfiguration;
import com.datastreaming.subscriber.JsonSubscriber;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;


public class GlobalHandler extends AbstractHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalHandler.class);

  /*@Override
  public void handleRequest(Context context) throws Exception {

    Span childSpan = Tracing.getTracer().spanBuilder("GlobalHandler.handleRequest")
            .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
            .startSpan();

    logger.info("Previous span {}", Span.current());
    try (Scope scope = childSpan.makeCurrent()) {
      childSpan.setAttribute("http.method", context.getRequest().getMethod().getName());
      childSpan.setAttribute("http.url", context.getRequest().getPath());
      AbstractSubscriber jsonSubscriber = new JsonSubscriber(context);
      context.getRequest().getBodyStream().subscribe(jsonSubscriber);
      childSpan.setStatus(StatusCode.OK);
    } catch (Throwable t) {
      childSpan.setStatus(StatusCode.ERROR, "GlobalHandler.handleRequest parent span error");
    } finally {
      childSpan.end();
    }
  }*/
  protected OpenTelemetryConfiguration openTelemetryConfiguration;
  @Override
  public void handleRequest(Context context) throws Exception {
    openTelemetryConfiguration = context.get(OpenTelemetryConfiguration.class);
    logger.info("Previous span in global handler {}", Span.current());
    Span childSpan = openTelemetryConfiguration.getTracer().spanBuilder("GlobalHandler.handleRequest")
            .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
            .startSpan();
    logger.info("Current span in global handler {}", childSpan);
    try (Scope scope = childSpan.makeCurrent()) {
      childSpan.setAttribute("http.method", context.getRequest().getMethod().getName());
      childSpan.setAttribute("http.url", context.getRequest().getPath());
      AbstractSubscriber jsonSubscriber = new JsonSubscriber(context);
      context.getRequest().getBodyStream().subscribe(jsonSubscriber);
      childSpan.setStatus(StatusCode.OK);
    } catch (Throwable t) {
      childSpan.setStatus(StatusCode.ERROR, "GlobalHandler.handleRequest parent span error");
    } finally {
      childSpan.end();
    }
  }

}
