package com.datastreaming.subscriber;

import com.datastreaming.exception.CorruptedJsonException;
import com.datastreaming.handler.AbstractSubscriber;
import com.datastreaming.otel.OpenTelemetryConfiguration;
import io.netty.buffer.ByteBuf;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;

import java.io.ByteArrayOutputStream;

public class JsonSubscriber extends AbstractSubscriber {

  private final Logger logger = LoggerFactory.getLogger(JsonSubscriber.class);
  private int openbraces;
  private boolean insideString;
  private String str;
  private static final String DEFAULT_CHARSET = "UTF-8";
  private ByteArrayOutputStream byteArrayOutputStream;

  private int lastBackslashCount;
  protected OpenTelemetryConfiguration openTelemetryConfiguration;
  public JsonSubscriber(Context ctx) {
    super(ctx);
    openTelemetryConfiguration = ctx.get(OpenTelemetryConfiguration.class);
    Span childSpan = openTelemetryConfiguration.getTracer().spanBuilder("JsonSubscriber.init")
            .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
            .startSpan();
    logger.info("super span {}", Span.current());
    try (Scope scope = childSpan.makeCurrent()) {
      byteArrayOutputStream = new ByteArrayOutputStream();
    }
  }

  @Override
  protected void process(ByteBuf byteBuf) {
    Span childSpan = openTelemetryConfiguration.getTracer().spanBuilder("JsonSubscriber.process")
            .setParent(io.opentelemetry.context.Context.current().with(Span.current()))
            .startSpan();
    logger.info("JsonSubscriber process {}", childSpan.getSpanContext().getTraceId());
    int wrtIdx = byteBuf.writerIndex();
    logger.trace("Write Index is: " + wrtIdx);
    byte c;
    for (int i = 0; i < wrtIdx; i++) {
      c = byteBuf.getByte(i);
      if (c == '{' && !insideString) {
        openbraces++;
      } else if (c == '}' && !insideString) {
        openbraces--;
        if (openbraces == 0) byteArrayOutputStream.write(c);
      } else if (c == '"') {
        if (!insideString) {
          insideString = true;
        } else {
          int backslashCount = 0;
          int j = i - 1;
          while (j >= 0) {
            if (byteBuf.getByte(j) == '\\') {
              backslashCount++;
              j--;
            } else {
              break;
            }
          }
          if (j < 0) backslashCount += lastBackslashCount;
          // The double quote isn't escaped only if there are even "\"s.
          if (backslashCount % 2 == 0) {
            // Since the double quote isn't escaped then this is the end of a string.
            insideString = false;
          }
        }
      }

      if (openbraces > 0) {
        byteArrayOutputStream.write(c);
      } else if (openbraces == 0 && byteArrayOutputStream.size() != 0) {
        try {
          byteArrayOutputStream.flush();
          str = byteArrayOutputStream.toString(DEFAULT_CHARSET);
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          e.printStackTrace();
        }

        String messageBusSendMessage = "false";

        if (!messageBusSendMessage.equalsIgnoreCase("false")) {
          if (true) {
           // logger.debug("Sending message to pulsar");
            sendMessagePulsar();
          }
        } else {
          //logger.debug("Send-message flag is  not enabled. Not sending message to Message bus");
        }
        byteArrayOutputStream.reset();
      }
    }

    lastBackslashCount = 0;
    int j = wrtIdx - 1;
    while (j >= 0) {
      if (byteBuf.getByte(j) == '\\') {
        lastBackslashCount++;
        j--;
      } else {
        break;
      }
    }
    logger.trace("last Backslash count: " + lastBackslashCount);
  }


  private void sendMessagePulsar() {
  }

  @Override
  public void requestComplete() {
    if (byteArrayOutputStream != null) {
      try {
        byteArrayOutputStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    logger.trace("request completed, openbraces = {}, str = {}", openbraces, str);
    logger.debug("request completed");
    if (openbraces != 0 || str == null) {
      error(new CorruptedJsonException("Corrupted Json data"));
    }
  }
}
