package com.datastreaming.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;

public class OpenTelemetryConfiguration {
  private static boolean IS_INITIALIZED = false; // Flag to prevent re-initialization

  public static synchronized Tracer initializeOpenTelemetry() {
    if (IS_INITIALIZED) {
      return GlobalOpenTelemetry.getTracer("ratpack-2.x-tracer"); // Return existing tracer
    }

    // Create and configure Tracer Provider
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .setSampler(Sampler.alwaysOn())
            .addSpanProcessor(BatchSpanProcessor.builder(new LoggingSpanExporter()).build())
            .build();

    // Build OpenTelemetry SDK and register globally
    OpenTelemetrySdk openTelemetrySdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();

    // Set flag to prevent re-initialization
    IS_INITIALIZED = true;

    // Return the Tracer for use
    return openTelemetrySdk.getTracer("ratpack-2.x-tracer");
  }

  public Tracer getTracer() {
    return initializeOpenTelemetry(); // Should return the same tracer instance
  }
}
