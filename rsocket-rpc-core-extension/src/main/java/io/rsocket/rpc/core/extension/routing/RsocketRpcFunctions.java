package io.rsocket.rpc.core.extension.routing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.rpc.core.extension.routing.TriFunction;
import java.util.function.BiFunction;
import org.reactivestreams.Publisher;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Those functions are representing the diff type of RPC standard functions */
public final class RsocketRpcFunctions {
  private RsocketRpcFunctions() {}

  @FunctionalInterface
  public interface RequestResponse<I, O> extends BiFunction<I, ByteBuf, CorePublisher<O>> {
    @Override
    CorePublisher<O> apply(I i, ByteBuf byteBuf);

    default CorePublisher<O> apply(I i) {
      return apply(i, Unpooled.EMPTY_BUFFER);
    }
  }

  @FunctionalInterface
  public interface RequestStream<I, O> extends RequestResponse<I,O> {

//    Flux<O> applyMany(I i, ByteBuf byteBuf);
//
//    default Flux<O> applyMany(I i) {
//      return applyMany(i, Unpooled.EMPTY_BUFFER);
//    }
  }

  @FunctionalInterface
  public interface HandleRequestHandle<I, O>
      extends TriFunction<I, Publisher<I>, ByteBuf, Flux<O>> {
    @Override
    Flux<O> apply(I i, Publisher<I> publisher, ByteBuf byteBuf);

    default Flux<O> apply(I i, Publisher<I> publisher) {
      return apply(i, publisher, Unpooled.EMPTY_BUFFER);
    }
  }

  @FunctionalInterface
  public interface RequestChannel<I, O> {
    Flux<O> apply(Publisher<I> publisher, ByteBuf byteBuf);

    default Flux<O> apply(Publisher<I> publisher) {
      return apply(publisher, Unpooled.EMPTY_BUFFER);
    }
  }

  @FunctionalInterface
  public interface FireAndForget<I> {
    Mono<Void> apply(I i, ByteBuf byteBuf);

    default Mono<Void> apply(I i) {
      return apply(i, Unpooled.EMPTY_BUFFER);
    }
  }
}
