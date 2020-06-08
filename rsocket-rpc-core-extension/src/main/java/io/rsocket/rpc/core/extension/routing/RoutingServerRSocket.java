package io.rsocket.rpc.core.extension.routing;

import io.grpc.HandlerRegistry;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class RoutingServerRSocket implements RSocket {
  private final HandlerRegistry registry;

  public RoutingServerRSocket(HandlerRegistry registryHandler) {
    this.registry = registryHandler;
  }

  @Override
  public Mono<Void> fireAndForget(Payload payload) {
    if (log.isTraceEnabled()) log.trace("received fireForget");
    return new RSocketServerCall(registry).invokeService(payload).then();
  }

  @Override
  public Mono<Payload> requestResponse(Payload payload) {
    if (log.isTraceEnabled()) log.trace("received requestResponse");
    return new RSocketServerCall(registry).invokeService(payload);
  }

  @Override
  public Flux<Payload> requestStream(Payload payload) {
    if (log.isTraceEnabled()) log.trace("received requestStream");
    return new RSocketServerCall(registry).invokeStreamService(payload);
  }

  @Override
  public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
    if (log.isTraceEnabled()) log.trace("received requestChannel");
    return new RSocketServerCall(registry).invokeBiDirectionService(payloads);
  }
}
