//package com.datastreaming.otel;
//
//import io.opentelemetry.api.GlobalOpenTelemetry;
//import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
//import io.opentelemetry.context.propagation.ContextPropagators;
//import io.opentelemetry.exporter.logging.LoggingSpanExporter;
//import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
//import io.opentelemetry.sdk.OpenTelemetrySdk;
//import io.opentelemetry.sdk.trace.SdkTracerProvider;
//import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
//import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
//import io.opentelemetry.sdk.trace.samplers.Sampler;
//
//public class OpenTelemetryConfig {
//    public static Tracer initializeOpenTelemetry() {
//
//
//        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
//                .setSampler(Sampler.alwaysOn())
//                .addSpanProcessor(SimpleSpanProcessor.create(new LoggingSpanExporter())).build();
//
//        /*// Configure the OTLP exporter
//        OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder()
//            .setEndpoint("") // Default to localhost if not set
//            .build();
//
//        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
//            .addSpanProcessor(SimpleSpanProcessor.create(otlpExporter))
//            .build();*/
//        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
//                .setTracerProvider(tracerProvider).
//                setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).build();
//
//        return openTelemetrySdk.getTracer("ratpack-2.x-tracer");
//    }
//}
