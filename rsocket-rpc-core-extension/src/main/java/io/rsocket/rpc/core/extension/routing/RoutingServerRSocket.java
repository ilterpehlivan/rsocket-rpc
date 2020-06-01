package io.rsocket.rpc.core.extension.routing;

import com.google.common.base.Preconditions;
import io.grpc.HandlerRegistry;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.ResponderRSocket;
import io.rsocket.rpc.core.extension.routing.ServiceHandlerRegistry.Builder;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class RoutingServerRSocket extends AbstractRSocket implements ResponderRSocket {
  private final HandlerRegistry registry;

  public RoutingServerRSocket(HandlerRegistry registryHandler) {
    this.registry = registryHandler;
  }

  @Override
  public Mono<Void> fireAndForget(Payload payload) {
    return new RSocketServerCall(registry).invokeService(payload).then();
  }

  @Override
  public Mono<Payload> requestResponse(Payload payload) {
    log.info("received requestResponse");
    //1- Generate ServerCall (registry,calldetails)
    //3- Find callHandler
    //4- Call callHandler.startCall
    //5- Get a stream from the listener via ServerCall
    //   serverCall.getResponse(listener)
    return new RSocketServerCall(registry)
        .invokeService(payload);
  }

  @Override
  public Flux<Payload> requestStream(Payload payload) {
    return new RSocketServerCall(registry).invokeStreamService(payload);
  }

  @Override
  public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
    return new RSocketServerCall(registry).invokeBiDirectionService(payloads);
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  @Override
  public Mono<Void> onClose() {
    return super.onClose();
  }

  @Override
  public Flux<Payload> requestChannel(Payload payload, Publisher<Payload> payloads) {
    // TODO:
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
