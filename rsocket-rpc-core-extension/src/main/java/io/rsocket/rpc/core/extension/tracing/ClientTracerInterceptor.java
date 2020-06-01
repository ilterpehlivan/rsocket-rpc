package io.rsocket.rpc.core.extension.tracing;

import brave.Span;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.ResponderRSocket;
import io.rsocket.plugins.RSocketInterceptor;
import io.rsocket.rpc.core.extension.metadata.MapBasedTracingMetadata;
import io.rsocket.rpc.core.extension.metadata.MetaDataUtil;
import io.rsocket.rpc.core.extension.metadata.MetaDataUtil.RsocketRpcRequest;
import io.rsocket.util.ByteBufPayload;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class ClientTracerInterceptor implements RSocketInterceptor {

  private final TracingContext tracingContext;

  public ClientTracerInterceptor(RSocketTracing rSocketTracing) {
    Objects.requireNonNull(rSocketTracing, "rpc tracing must not be null");
    tracingContext =
        new TracingContext(rSocketTracing.getRpcTracing(), rSocketTracing.getServiceName());
  }

  @Override
  public RSocket apply(RSocket delegate) {
    Objects.requireNonNull(delegate, "delegate must not be null");
    if (log.isTraceEnabled()) {
      log.trace("applying ClientTracer interceptor");
    }
    return new ClientTraceRSocketResponder(tracingContext, delegate);
  }

  private class ClientTraceRSocketResponder implements ResponderRSocket {

    private final RSocket delegate;
    private final TracingContext tracingContext;

    public ClientTraceRSocketResponder(TracingContext tracingContext, RSocket delegate) {
      this.delegate = delegate;
      this.tracingContext = tracingContext;
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
      if (log.isTraceEnabled()) {
        log.trace("inside the fireAndForget interceptor");
      }
      TraceContext invocationContext = tracingContext.currentTraceContext.get();
      RsocketRpcRequest rpcRequest = MetaDataUtil.getRpcRequest(payload.metadata());
      TracingRequest request = new TracingRequest(rpcRequest.getService(), rpcRequest.getMethod());
      Span span =
          tracingContext.tracer.nextSpanWithParent(
              tracingContext.sampler, request, invocationContext);
      if (log.isTraceEnabled()) {
        log.trace("tracing context is created as {}", span);
      }
      java.util.Map<String, String> map = new java.util.HashMap<>();
      Throwable error = null;
      Payload payloadWithTracing = null;
      try (CurrentTraceContext.Scope scope =
          tracingContext.currentTraceContext.maybeScope(span.context())) {
        tracingContext.mapInjector.inject(span.context(), map);
        span.kind(Span.Kind.CLIENT).start();
        if (log.isDebugEnabled()) {
          log.debug("tracing is added to the request metadata with map {}", map);
        }
        MapBasedTracingMetadata mapBasedTracingMetadata = new MapBasedTracingMetadata(map);
        ByteBuf metadataWithTracing =
            MetaDataUtil.addTracing2RpcComposite(payload.metadata(), mapBasedTracingMetadata);

        // Recreating the payload
        payloadWithTracing = ByteBufPayload.create(payload.data(), metadataWithTracing);

        // no need for the previous metadata
        payload.metadata().release();

        try (CurrentTraceContext.Scope invokeScope =
            tracingContext.currentTraceContext.maybeScope(span.context())) {

          if (log.isTraceEnabled()) {
            log.trace("meta information before call..");
            MetaDataUtil.printRpcCompositeMetadata(payloadWithTracing.metadata());
          }
          return delegate
              .fireAndForget(payloadWithTracing)
              .transform(
                  TracingUtil.<Void>scopePassingSpanOperator(
                      tracingContext.currentTraceContext, span.context()))
              .doOnNext(
                  p -> {
                    if (log.isDebugEnabled()) {
                      log.debug(
                          "completed calling the service {} method {} with tracing scope {}",
                          rpcRequest.getService(),
                          rpcRequest.getMethod(),
                          scope);
                    }
                  })
              .doOnError(er -> log.error("error received", er));
        }
      } catch (Throwable e) {
        log.error("error received for client tracer interceptor", e);
        if (payloadWithTracing != null) ReferenceCountUtil.safeRelease(payloadWithTracing);
        error = e;
        throw e;
      } finally {
        if (error != null) span.error(error).finish();
      }
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
      if (log.isTraceEnabled()) {
        log.trace("inside the requestResponse interceptor");
      }
      TraceContext invocationContext = tracingContext.currentTraceContext.get();
      RsocketRpcRequest rpcRequest = MetaDataUtil.getRpcRequest(payload.metadata());
      TracingRequest request = new TracingRequest(rpcRequest.getService(), rpcRequest.getMethod());
      Span span =
          tracingContext.tracer.nextSpanWithParent(
              tracingContext.sampler, request, invocationContext);
      if (log.isTraceEnabled()) {
        log.trace("tracing context is created as {}", span);
      }
      java.util.Map<String, String> map = new java.util.HashMap<>();
      Throwable error = null;
      Payload payloadWithTracing = null;
      try (CurrentTraceContext.Scope scope =
          tracingContext.currentTraceContext.maybeScope(span.context())) {
        tracingContext.mapInjector.inject(span.context(), map);
        span.kind(Span.Kind.CLIENT).start();
        if (log.isDebugEnabled()) {
          log.debug("tracing is added to the request metadata with map {}", map);
        }
        //        final ByteBuf tracing = Tracing.mapToByteBuf(ByteBufAllocator.DEFAULT, map);
        MapBasedTracingMetadata mapBasedTracingMetadata = new MapBasedTracingMetadata(map);
        //        ByteBuf metadataWithTracing =
        //            MetaDataUtil.encodeRpcComposite(
        //                rpcRequest.getService(),
        //                rpcRequest.getMethod(),
        //                rpcRequest.getHeaders(),
        //                mapBasedTracingMetadata);
        ByteBuf metadataWithTracing =
            MetaDataUtil.addTracing2RpcComposite(payload.metadata(), mapBasedTracingMetadata);
        //        //        CompositeByteBuf compositeByteBufWithTracing =
        //            MetaDataUtil.addCompositeTracing(payload.metadata(), tracing);

        // Recreating the payload
        payloadWithTracing = ByteBufPayload.create(payload.data(), metadataWithTracing);
        //        payloadWithTracing = ByteBufPayload.create(payload.data().copy(),
        // metadataWithTracing);

        try (CurrentTraceContext.Scope invokeScope =
            tracingContext.currentTraceContext.maybeScope(span.context())) {

          if (log.isTraceEnabled()) {
            log.trace("meta information before call..");
            MetaDataUtil.printRpcCompositeMetadata(payloadWithTracing.metadata());
          }
          return delegate
              .requestResponse(payloadWithTracing)
              .transform(
                  TracingUtil.<Payload>scopePassingSpanOperator(
                      tracingContext.currentTraceContext, span.context()))
              .doOnNext(
                  p -> {
                    if (log.isDebugEnabled()) {
                      log.debug(
                          "completed calling the service {} method {} with tracing scope {}",
                          rpcRequest.getService(),
                          rpcRequest.getMethod(),
                          scope);
                    }
                  })
              .doOnError(er -> log.error("error received", er));
        }
      } catch (Throwable e) {
        log.error("error received for tracer interceptor", e);
        if (payloadWithTracing != null) ReferenceCountUtil.safeRelease(payloadWithTracing);
        error = e;
        throw e;
      } finally {
        if (error != null) span.error(error).finish();
      }
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
      if (log.isTraceEnabled()) {
        log.trace("inside the requestStream interceptor");
      }
      TraceContext invocationContext = tracingContext.currentTraceContext.get();
      RsocketRpcRequest rpcRequest = MetaDataUtil.getRpcRequest(payload.metadata());
      TracingRequest request = new TracingRequest(rpcRequest.getService(), rpcRequest.getMethod());
      Span span =
          tracingContext.tracer.nextSpanWithParent(
              tracingContext.sampler, request, invocationContext);
      if (log.isTraceEnabled()) {
        log.trace("tracing context is created as {}", span);
      }
      java.util.Map<String, String> map = new java.util.HashMap<>();
      Throwable error = null;
      Payload payloadWithTracing = null;
      try (CurrentTraceContext.Scope scope =
          tracingContext.currentTraceContext.maybeScope(span.context())) {
        tracingContext.mapInjector.inject(span.context(), map);
        span.kind(Span.Kind.CLIENT).start();
        if (log.isDebugEnabled()) {
          log.debug("tracing is added to the request metadata with map {}", map);
        }
        MapBasedTracingMetadata mapBasedTracingMetadata = new MapBasedTracingMetadata(map);
        ByteBuf metadataWithTracing =
            MetaDataUtil.addTracing2RpcComposite(payload.metadata(), mapBasedTracingMetadata);

        // Recreating the payload
        payloadWithTracing = ByteBufPayload.create(payload.data(), metadataWithTracing);

        // no need for the previous metadata
        payload.metadata().release();

        try (CurrentTraceContext.Scope invokeScope =
            tracingContext.currentTraceContext.maybeScope(span.context())) {

          if (log.isTraceEnabled()) {
            MetaDataUtil.printRpcCompositeMetadata(payloadWithTracing.metadata());
          }
          return delegate
              .requestStream(payloadWithTracing)
              .transform(
                  TracingUtil.<Payload>scopePassingSpanOperator(
                      tracingContext.currentTraceContext, span.context()))
              .doOnNext(
                  p -> {
                    if (log.isDebugEnabled()) {
                      log.debug(
                          "completed calling the service {} method {} with tracing scope {}",
                          rpcRequest.getService(),
                          rpcRequest.getMethod(),
                          scope);
                    }
                  })
              .doOnError(er -> log.error("error received", er));
        }
      } catch (Throwable e) {
        log.error("error received for client tracer interceptor", e);
        if (payloadWithTracing != null) ReferenceCountUtil.safeRelease(payloadWithTracing);
        error = e;
        throw e;
      } finally {
        if (error != null) span.error(error).finish();
      }
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
      if (log.isTraceEnabled()) {
        log.trace("inside the requestStream interceptor");
      }

      Publisher<Payload> payloadWithTracingStream =
          Flux.from(payloads)
              .switchOnFirst(
                  (first, flux) -> {
                    Payload payload = first.get();
                    java.util.Map<String, String> map = new java.util.HashMap<>();

                    MapBasedTracingMetadata mapBasedTracingMetadata =
                        new MapBasedTracingMetadata(map);
                    ByteBuf metadataWithTracing =
                        MetaDataUtil.addTracing2RpcComposite(
                            payload.metadata(), mapBasedTracingMetadata);

                    TraceContext invocationContext = tracingContext.currentTraceContext.get();
                    RsocketRpcRequest rpcRequest = MetaDataUtil.getRpcRequest(payload.metadata());
                    TracingRequest request =
                        new TracingRequest(rpcRequest.getService(), rpcRequest.getMethod());
                    Span span =
                        tracingContext.tracer.nextSpanWithParent(
                            tracingContext.sampler, request, invocationContext);
                    Payload payloadWithTracing = null;
                    try (CurrentTraceContext.Scope scope =
                        tracingContext.currentTraceContext.maybeScope(span.context())) {
                      tracingContext.mapInjector.inject(span.context(), map);
                      if (log.isDebugEnabled()) {
                        log.debug("tracing is added to the request metadata with map {}", map);
                      }
                      span.kind(Span.Kind.CLIENT).start();
                      if (log.isTraceEnabled()) {
                        log.trace("tracing context is created as {}", span);
                      }

                      // Recreating the payload
                      payloadWithTracing =
                          ByteBufPayload.create(payload.data(), metadataWithTracing);

                      // no need for the previous metadata
                      payload.metadata().release();

                      if (log.isTraceEnabled()) {
                        log.trace("meta information before call..");
                        MetaDataUtil.printRpcCompositeMetadata(payloadWithTracing.metadata());
                      }
                      try (CurrentTraceContext.Scope invokeScope =
                          tracingContext.currentTraceContext.maybeScope(span.context())) {
                        return flux.skip(1)
                            .startWith(payloadWithTracing)
                            .transform(
                                TracingUtil.<Payload>scopePassingSpanOperator(
                                    tracingContext.currentTraceContext, span.context()))
                            .doOnComplete(
                                () -> {
                                  if (log.isDebugEnabled()) {
                                    log.debug(
                                        "completed calling the service {} method {} with tracing scope {}",
                                        rpcRequest.getService(),
                                        rpcRequest.getMethod(),
                                        scope);
                                  }
                                });
                      }

                    } catch (Throwable e) {
                      log.error("error received for client tracer interceptor", e);
                      if (payloadWithTracing != null)
                        ReferenceCountUtil.safeRelease(payloadWithTracing);
                      throw e;
                    }
                  });

      return delegate
          .requestChannel(payloadWithTracingStream)
          //TODO:finish span in doOnError
          .doOnError(er -> log.error("error received", er));
    }

    @Override
    public Mono<Void> metadataPush(Payload payload) {
      return null;
    }

    @Override
    public Flux<Payload> requestChannel(Payload payload, Publisher<Payload> payloads) {
      return null;
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
