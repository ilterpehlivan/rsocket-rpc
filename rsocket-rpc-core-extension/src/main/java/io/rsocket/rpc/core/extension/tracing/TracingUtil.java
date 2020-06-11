package io.rsocket.rpc.core.extension.tracing;

import brave.Span;
import brave.Span.Kind;
import brave.propagation.CurrentTraceContext;
import brave.propagation.CurrentTraceContext.Scope;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import io.rsocket.Payload;
import io.rsocket.rpc.core.extension.metadata.MetaDataUtil.RsocketRpcRequest;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.publisher.Operators;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

@Slf4j
public class TracingUtil {

  public static final String TAG_RPC_SERVICE_NAME = "rpc.service.name";
  public static final String TAG_RPC_METHOD_TYPE = "rpc.method.type";

  public static <T> Function<? super Publisher<T>, ? extends Publisher<T>> scopePassingSpanOperator(
      CurrentTraceContext currentTraceContext, TraceContext rpcContext) {
    if (log.isTraceEnabled()) {
      log.trace("Creating Scope passing operator");
    }

    return Operators.liftPublisher(
        (p, sub) -> {
          // We don't scope scalar results as they happen in an instant. This prevents
          // excessive overhead when using Flux/Mono #just, #empty, #error, etc.
          if (p instanceof Fuseable.ScalarCallable) {
            return sub;
          }

          Context context = sub.currentContext();

          if (log.isTraceEnabled()) {
            log.trace("Reactor context [" + context + "], name [" + name(sub) + "]");
          }

          // Try to get the current trace context bean, lenient when there are problems
          if (currentTraceContext == null) {
            boolean assertOn = false;
            assert assertOn = true; // gives a message in unit test failures
            if (log.isTraceEnabled() || assertOn) {
              String message =
                  "did not return a CurrentTraceContext. Reactor Context is ["
                      + sub.currentContext()
                      + "] and name is ["
                      + name(sub)
                      + "]";
              log.trace(message);
              assert false : message; // should never happen, but don't break.
            }
            return sub;
          }
          TraceContext parent;
          parent = traceContext(context, currentTraceContext);
          if (parent == null && rpcContext != null) {
            parent = rpcContext;
          }
          if (log.isTraceEnabled()) {
            log.trace("parent context is {}", parent);
          }
          if (parent == null) {
            return sub; // no need to scope a null parent
          }

          if (log.isTraceEnabled()) {
            log.trace(
                "Creating a scope passing span subscriber with Reactor Context "
                    + "["
                    + context
                    + "] and name ["
                    + name(sub)
                    + "]");
          }
          return new ScopePassingSpanSubscriber<>(sub, context, currentTraceContext, parent);
        });
  }

  /**
   * Use this function in the sub-streams like generating another stream within a stream
   *
   * @param <T>
   * @return
   */
  public static <T>
      Function<? super Publisher<T>, ? extends Publisher<T>> scopePassingSpanOperator() {
    if (log.isTraceEnabled()) {
      log.trace("Using scope from context");
    }

    return Operators.liftPublisher(
        (p, sub) -> {
          // We don't scope scalar results as they happen in an instant. This prevents
          // excessive overhead when using Flux/Mono #just, #empty, #error, etc.
          if (p instanceof Fuseable.ScalarCallable) {
            return sub;
          }

          Context context = sub.currentContext();

          if (log.isTraceEnabled()) {
            log.trace("Reactor context [" + context + "], name [" + name(sub) + "]");
          }

          // Try to get the current trace context bean, lenient when there are problems
          CurrentTraceContext currentTraceContext = null;

          if (context.hasKey(CurrentTraceContext.class)) {
            currentTraceContext = context.get(CurrentTraceContext.class);
          }

          TraceContext parent = null;
          if (context.hasKey(TraceContext.class)) {
            parent = context.get(TraceContext.class);
          }

          if (log.isTraceEnabled()) {
            log.trace("parent context is {} currentContext is {}", parent, currentTraceContext);
          }
          if (currentTraceContext == null) {
            return sub; // no need to scope a null parent
          }

          if (log.isTraceEnabled()) {
            log.trace(
                "Using a scope passing span subscriber with Reactor Context "
                    + "["
                    + context
                    + "] and name ["
                    + name(sub)
                    + "]");
          }
          return new ScopePassingSpanSubscriber<>(sub, context, currentTraceContext, parent);
        });
  }

  static String name(CoreSubscriber<?> sub) {
    return Scannable.from(sub).name();
  }

  /** Like {@link CurrentTraceContext#get()}, except it first checks the reactor context. */
  static TraceContext traceContext(Context context, CurrentTraceContext fallback) {
    if (context.hasKey(TraceContext.class)) {
      return context.get(TraceContext.class);
    }
    return fallback.get();
  }

  /** Functions used in tracing interceptors */
  public static Consumer<SignalType> handleStreamCompletion(
      String service, String method, Span span) {
    return signalType -> {
      if (signalType == SignalType.ON_COMPLETE || signalType == SignalType.CANCEL) {
        if (log.isDebugEnabled()) {
          log.debug(
              "stream is completed as {} on the service {} and method {} with tracing span {}",
              signalType.name(),
              service,
              method,
              span);
        }
        span.finish();
      }
    };
  }

  public static Consumer<Throwable> handleError(Span span) {
    return er -> {
      log.error("error received during request response tracer interceptor..Closing span", er);
      span.error(er).finish();
    };
  }

  public static Span getClientSpanWithTags(
      final TracingContext tracingContext,
      TraceContext invocationContext,
      TracingRequest tracingRequest) {
    Span span =
        tracingContext.tracer.nextSpanWithParent(
            tracingContext.sampler, tracingRequest, invocationContext);
    span.tag(TAG_RPC_SERVICE_NAME, tracingRequest.getServiceName());
    span.tag(TAG_RPC_METHOD_TYPE, tracingRequest.getMethodName());

    Span clientSpan = span.kind(Kind.CLIENT);
    return clientSpan;
  }

  public static TracingRequest getClientTraceRequest(
      RsocketRpcRequest rpcRequest,
      final TracingContext tracingContext,
      TraceContext invocationContext) {
    TracingRequest tracingRequest =
        new TracingRequest(rpcRequest.getService(), rpcRequest.getMethod());
    Span span = getClientSpanWithTags(tracingContext, invocationContext, tracingRequest);
    tracingRequest.setSpan(span);

    return tracingRequest;
  }

  public static TracingRequest getServerTraceRequest(
      final TraceContextOrSamplingFlags extractedContex,
      RsocketRpcRequest rpcRequest,
      final TracingContext tracingContext) {
    TracingRequest tracingRequest =
        new TracingRequest(rpcRequest.getService(), rpcRequest.getMethod());
    Span span = getServerSpanWithTags(extractedContex,rpcRequest,tracingContext);
    tracingRequest.setSpan(span);
    return tracingRequest;
  }

  public static Span getServerSpanWithTags(
      final TraceContextOrSamplingFlags extractedContex,
      RsocketRpcRequest rpcRequest,
      final TracingContext tracingContext) {
    Span span = nextSpan(extractedContex, tracingContext);
    span.tag(TAG_RPC_SERVICE_NAME, rpcRequest.getService());
    span.tag(TAG_RPC_METHOD_TYPE, rpcRequest.getMethod());
    return span.kind(Kind.SERVER);
  }

  private static Span nextSpan(
      TraceContextOrSamplingFlags extractedContex, TracingContext tracingContext) {
    return extractedContex.context() != null
        ? tracingContext.tracer.joinSpan(extractedContex.context())
        : tracingContext.tracer.nextSpan(extractedContex);
  }

  public static Consumer<Payload> handleNext(RsocketRpcRequest rpcRequest, Scope scope) {
    return p -> {
      if (log.isDebugEnabled()) {
        log.debug(
            "completed calling the service {} method {} with tracing scope {}",
            rpcRequest.getService(),
            rpcRequest.getMethod(),
            scope);
      }
    };
  }

  public static Function<? super Publisher<Payload>, ? extends Publisher<Payload>>
      getSpanPassingOperator(final TracingContext tracingContext, Span span) {
    return TracingUtil.<Payload>scopePassingSpanOperator(
        tracingContext.currentTraceContext, span.context());
  }
}
