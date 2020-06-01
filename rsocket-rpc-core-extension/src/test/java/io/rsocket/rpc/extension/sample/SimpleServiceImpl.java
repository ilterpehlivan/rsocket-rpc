package io.rsocket.rpc.extension.sample;

import com.google.protobuf.Empty;
import io.netty.buffer.ByteBuf;
import io.rsocket.rpc.extension.sample.proto.SimpleRequest;
import io.rsocket.rpc.extension.sample.proto.SimpleResponse;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class SimpleServiceImpl extends SimpleServiceBase {

  @Override
  public Mono<SimpleResponse> requestReply(SimpleRequest message, ByteBuf metadata) {
    log.info("inside the sayHello function");
    return Mono.just(
        SimpleResponse.newBuilder()
            .setResponseMessage("hello response:" + message.getRequestMessage())
            .build());
  }

  @Override
  public Mono<Empty> fireAndForget(SimpleRequest message, ByteBuf metadata) {
    return Mono.empty();
  }

  @Override
  public Flux<SimpleResponse> requestStream(SimpleRequest message, ByteBuf metadata) {
    return Flux.create(
        emmiter -> {
          for (int i = 0; i < 10; i++)
            emmiter.next(SimpleResponse.newBuilder().setResponseMessage("hello" + i).build());
          emmiter.complete();
        });
  }

  @Override
  public Flux<SimpleResponse> streamingRequestAndResponse(
      Publisher<SimpleRequest> messages, ByteBuf metadata) {
    return Flux.create(
        emmiter -> {
          for (int i = 0; i < 10; i++)
            emmiter.next(SimpleResponse.newBuilder().setResponseMessage("hello" + i).build());
          emmiter.complete();
        });
  }
}
