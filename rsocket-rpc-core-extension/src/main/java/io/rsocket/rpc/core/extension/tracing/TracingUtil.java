package io.rsocket.rpc.core.extension.tracing;

import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

@Slf4j
public class TracingUtil {

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
            log.trace("parent context is {}",parent);
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
   * @param <T>
   * @return
   */
  public static <T> Function<? super Publisher<T>, ? extends Publisher<T>> scopePassingSpanOperator() {
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
            log.trace("parent context is {} currentContext is {}",parent,currentTraceContext);
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
}
