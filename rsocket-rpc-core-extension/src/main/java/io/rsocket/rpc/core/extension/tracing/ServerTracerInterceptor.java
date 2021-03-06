package io.rsocket.rpc.core.extension.tracing;

import static io.rsocket.rpc.core.extension.tracing.TracingUtil.getServerSpanWithTags;
import static io.rsocket.rpc.core.extension.tracing.TracingUtil.getServerTraceRequest;
import static io.rsocket.rpc.core.extension.tracing.TracingUtil.getSpanPassingOperator;
import static io.rsocket.rpc.core.extension.tracing.TracingUtil.handleError;
import static io.rsocket.rpc.core.extension.tracing.TracingUtil.handleStreamCompletion;

import brave.Span;
import brave.propagation.CurrentTraceContext;
import brave.propagation.CurrentTraceContext.Scope;
import brave.propagation.TraceContextOrSamplingFlags;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.plugins.RSocketInterceptor;
import io.rsocket.rpc.core.extension.metadata.MetaDataUtil;
import io.rsocket.rpc.core.extension.metadata.MetaDataUtil.RsocketRpcRequest;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class ServerTracerInterceptor implements RSocketInterceptor {

  private final TracingContext tracingContext;

  public ServerTracerInterceptor(RSocketTracing rSocketTracing) {
    Objects.requireNonNull(rSocketTracing, "rpc tracing must not be null");
    tracingContext =
        new TracingContext(rSocketTracing.getRpcTracing(), rSocketTracing.getServiceName());
  }

  @Override
  public RSocket apply(RSocket delegate) {
    Objects.requireNonNull(delegate, "delegate must not be null");
    if (log.isTraceEnabled()) {
      log.trace("applying ServerTracer interceptor");
    }
    return new ServerTraceRSocketResponder(tracingContext, delegate);
  }

  private class ServerTraceRSocketResponder implements RSocket {

    private final RSocket delegate;
    private final TracingContext tracingContext;

    public ServerTraceRSocketResponder(TracingContext tracingContext, RSocket delegate) {
      this.delegate = delegate;
      this.tracingContext = tracingContext;
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {

      if (log.isTraceEnabled()) {
        log.trace("inside the fireAndForget interceptor with metas...");
        MetaDataUtil.printRpcCompositeMetadata(payload.metadata());
      }
      Map<String, String> traceSpanMap = MetaDataUtil.getRpcTracingContextMap(payload.metadata());

      if (log.isDebugEnabled()) {
        log.debug("extracted trace map from the request meta is {}", traceSpanMap);
      }
      TraceContextOrSamplingFlags extractedContex =
          tracingContext.mapExtractor.extract(traceSpanMap);

      RsocketRpcRequest rpcRequest = MetaDataUtil.getRpcRequest(payload.metadata());
      Span span = getServerSpanWithTags(extractedContex, rpcRequest, tracingContext);

      if (log.isTraceEnabled()) {
        log.trace("tracing context in the service {}", span);
      }

      Throwable error = null;
      try (CurrentTraceContext.Scope scope =
          tracingContext.currentTraceContext.maybeScope(span.context())) {
        if (log.isDebugEnabled()) {
          log.debug("calling delegate fireAndForget with scope {}", scope);
        }
        return delegate
            .fireAndForget(payload)
            .transform(
                TracingUtil.<Void>scopePassingSpanOperator(
                    tracingContext.currentTraceContext, span.context()))
            .doOnError(handleError(span))
            .doFinally(
                handleStreamCompletion(rpcRequest.getService(), rpcRequest.getMethod(), span));
      } catch (Throwable e) {
        error = e;
        throw e;
      } finally {
        if (error != null) span.error(error).finish();
      }
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
      if (log.isTraceEnabled()) {
        log.trace("inside the requestResponse interceptor with metas...");
        //        MetaDataUtil.printCompositeMeta(payload.metadata());
        MetaDataUtil.printRpcCompositeMetadata(payload.metadata());
      }

      //      ByteBuf tracingByteBuf = MetaDataUtil.getCompositeTracing(payload.metadata());
      //      Map<String, String> traceSpanMap = Tracing.byteBufToMap(tracingByteBuf);
      Map<String, String> traceSpanMap = MetaDataUtil.getRpcTracingContextMap(payload.metadata());

      if (log.isDebugEnabled()) {
        log.debug("extracted trace map from the request meta is {}", traceSpanMap);
      }
      TraceContextOrSamplingFlags extractedContex =
          tracingContext.mapExtractor.extract(traceSpanMap);

      RsocketRpcRequest rpcRequest = MetaDataUtil.getRpcRequest(payload.metadata());
      Span span = getServerSpanWithTags(extractedContex, rpcRequest, tracingContext);

      if (log.isTraceEnabled()) {
        log.trace("tracing context in the service {}", span);
      }

      Throwable error = null;
      try (CurrentTraceContext.Scope scope =
          tracingContext.currentTraceContext.maybeScope(span.context())) {
        if (log.isDebugEnabled()) {
          log.debug("calling delegate requestResponse with scope {}", scope);
        }
        return delegate
            .requestResponse(payload)
            .transform(getSpanPassingOperator(tracingContext, span))
            .doOnError(handleError(span))
            .doFinally(
                handleStreamCompletion(rpcRequest.getService(), rpcRequest.getMethod(), span));
      } catch (Throwable e) {
        error = e;
        throw e;
      } finally {
        if (error != null) span.error(error).finish();
      }
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {

      if (log.isTraceEnabled()) {
        log.trace("inside the requestStream interceptor with metas...");
        MetaDataUtil.printRpcCompositeMetadata(payload.metadata());
      }
      Map<String, String> traceSpanMap = MetaDataUtil.getRpcTracingContextMap(payload.metadata());

      if (log.isDebugEnabled()) {
        log.debug("extracted trace map from the request meta is {}", traceSpanMap);
      }
      TraceContextOrSamplingFlags extractedContex =
          tracingContext.mapExtractor.extract(traceSpanMap);

      RsocketRpcRequest rpcRequest = MetaDataUtil.getRpcRequest(payload.metadata());
      Span span = getServerSpanWithTags(extractedContex, rpcRequest, tracingContext);

      if (log.isTraceEnabled()) {
        log.trace("tracing context in the service {}", span);
      }

      Throwable error = null;
      try (CurrentTraceContext.Scope scope =
          tracingContext.currentTraceContext.maybeScope(span.context())) {
        if (log.isDebugEnabled()) {
          log.debug("calling delegate requestStream with scope {}", scope);
        }
        return delegate
            .requestStream(payload)
            .transform(getSpanPassingOperator(tracingContext, span))
            .doOnError(handleError(span))
            .doFinally(
                handleStreamCompletion(rpcRequest.getService(), rpcRequest.getMethod(), span));
      } catch (Throwable e) {
        error = e;
        throw e;
      } finally {
        if (error != null) span.error(error).finish();
      }
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {

      AtomicReference<TracingRequest> tracingRequest = new AtomicReference<>();
      Flux<Payload> payloadFlux =
          Flux.from(payloads)
              .switchOnFirst(
                  (first, flux) -> {
                    Payload payload = first.get();
                    if (log.isTraceEnabled()) {
                      log.trace("inside the requestStream interceptor with metas...");
                      MetaDataUtil.printRpcCompositeMetadata(payload.metadata());
                    }
                    Map<String, String> traceSpanMap =
                        MetaDataUtil.getRpcTracingContextMap(payload.metadata());

                    if (log.isDebugEnabled()) {
                      log.debug("extracted trace map from the request meta is {}", traceSpanMap);
                    }
                    TraceContextOrSamplingFlags extractedContex =
                        tracingContext.mapExtractor.extract(traceSpanMap);

                    RsocketRpcRequest rpcRequest = MetaDataUtil.getRpcRequest(payload.metadata());
                    tracingRequest.compareAndSet(
                        null, getServerTraceRequest(extractedContex, rpcRequest, tracingContext));

                    Span span = tracingRequest.get().getSpan();

                    if (log.isTraceEnabled()) {
                      log.trace("tracing context in the service {}", span);
                    }
                    try (Scope scope =
                        tracingContext.currentTraceContext.maybeScope(span.context())) {
                      if (log.isDebugEnabled()) {
                        log.debug("calling delegate requestChannel with scope {}", scope);
                      }
                      return flux.skip(1)
                          .startWith(payload)
                          .transform(getSpanPassingOperator(tracingContext, span));
                    } catch (Throwable e) {
                      log.error("error received, closing span ", e);
                      span.error(e).finish();
                      throw e;
                    }
                  });

      return delegate
          .requestChannel(payloadFlux)
          .doOnError(handleError(tracingRequest.get().getSpan()))
          .doFinally(
              handleStreamCompletion(
                  tracingRequest.get().getServiceName(),
                  tracingRequest.get().getMethodName(),
                  tracingRequest.get().getSpan()));
    }

    // TODO:below functions to implement
    @Override
    public Mono<Void> metadataPush(Payload payload) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> onClose() {
      return delegate.onClose();
    }

    @Override
    public void dispose() {
      delegate.dispose();
    }
  }
}
