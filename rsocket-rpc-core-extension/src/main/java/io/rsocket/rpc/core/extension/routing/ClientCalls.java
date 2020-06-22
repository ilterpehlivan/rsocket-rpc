package io.rsocket.rpc.core.extension.routing;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Empty;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import io.netty.buffer.ByteBuf;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.rpc.core.extension.metadata.MetaDataUtil;
import io.rsocket.util.ByteBufPayload;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utility functions for processing different client call idioms. We have one-to-one correspondence
 * between utilities in this class and the potential signatures in a generated stub client class so
 * that the runtime can vary behavior without requiring regeneration of the stub.
 */
public final class ClientCalls {
  private ClientCalls() {}

  /**
   * Implements a unary → unary call using {@link Mono} → {@link Mono}.
   *
   * <p>Rsocket - requestResponse
   */
  public static <TRequest extends MessageLite, TResponse extends MessageLite>
      Mono<TResponse> requestReply(
          Mono<RSocket> rSocketMono,
          TRequest message,
          ByteBuf metadata,
          String serviceName,
          String methodName,
          final Parser<TResponse> responseParser) {
    try {
      return Mono.defer(
              (Supplier<Mono<Payload>>)
                  () -> {
                    final ByteBuf metadataBuf =
                        MetaDataUtil.encodeRpcComposite(serviceName, methodName, metadata);
                    metadata.release();
                    return rSocketMono.flatMap(
                        rSocket ->
                            rSocket.requestResponse(
                                ByteBufPayload.create(proto2ByteBuf(message), metadataBuf)));
                  })
          .map(deserializer(responseParser));
    } catch (Throwable throwable) {
      return Mono.error(throwable);
    }
  }

  // TODO:fix this
  public static <TRequest extends MessageLite, TResponse extends MessageLite>
      Mono<Empty> fireAndForget(
          Mono<RSocket> rSocketMono,
          TRequest message,
          ByteBuf metadata,
          String serviceName,
          String methodName,
          final Parser<TResponse> responseParser) {
    try {
      return Mono.defer(
              (Supplier<Mono<Void>>)
                  () -> {
                    final ByteBuf metadataBuf =
                        MetaDataUtil.encodeRpcComposite(serviceName, methodName, metadata);
                    metadata.release();
                    return rSocketMono.flatMap(
                        rSocket ->
                            rSocket.fireAndForget(
                                ByteBufPayload.create(proto2ByteBuf(message), metadataBuf)));
                  })
          .thenReturn(Empty.getDefaultInstance());
    } catch (Throwable throwable) {
      return Mono.error(throwable);
    }
  }

  /**
   * Implements a unary → stream call as {@link Mono} → {@link Flux}, where the server responds with
   * a stream of messages.
   */
  public static <TRequest extends MessageLite, TResponse extends MessageLite>
      Flux<TResponse> requestStream(
          Mono<RSocket> rSocketMono,
          TRequest message,
          ByteBuf metadata,
          String serviceName,
          String methodName,
          final Parser<TResponse> responseParser) {
    try {
      return Flux.defer(
              (Supplier<Flux<Payload>>)
                  () -> {
                    final ByteBuf metadataBuf =
                        MetaDataUtil.encodeRpcComposite(serviceName, methodName, metadata);
                    metadata.release();
                    return rSocketMono.flatMapMany(
                        rSocket ->
                            rSocket.requestStream(
                                ByteBufPayload.create(proto2ByteBuf(message), metadataBuf)));
                  })
          .map(deserializer(responseParser));
    } catch (Throwable throwable) {
      return Flux.error(throwable);
    }
  }

  public static <TRequest extends MessageLite, TResponse extends MessageLite>
      Flux<TResponse> streamingRequestAndResponse(
          Mono<RSocket> rSocketMono,
          Publisher<TRequest> messages,
          ByteBuf metadata,
          String serviceName,
          String methodName,
          final Parser<TResponse> responseParser) {
    try {
      return rSocketMono.flatMapMany(
          rSocket ->
              rSocket
                  .requestChannel(
                      Flux.from(messages)
                          .map(
                              new Function<MessageLite, Payload>() {
                                private final AtomicBoolean once = new AtomicBoolean(false);

                                @Override
                                public Payload apply(MessageLite message) {
                                  if (once.compareAndSet(false, true)) {
                                    final ByteBuf metadataBuf =
                                        MetaDataUtil.encodeRpcComposite(
                                            serviceName, methodName, metadata);
                                    metadata.release();
                                    return ByteBufPayload.create(
                                        proto2ByteBuf(message), metadataBuf);
                                  } else {
                                    return ByteBufPayload.create(message.toByteArray());
                                  }
                                }
                              }))
                  .map(deserializer(responseParser)));
    } catch (Throwable throwable) {
      return Flux.error(throwable);
    }
  }

  // TODO:
  public static <TRequest extends MessageLite, TResponse extends MessageLite>
      Flux<TResponse> streamingRequestSingleResponse(
          Mono<RSocket> rSocketMono,
          Publisher<TRequest> messages,
          ByteBuf metadata,
          String serviceName,
          String methodName,
          final Parser<TResponse> responseParser) {
    throw new UnsupportedOperationException();
  }

  private static io.netty.buffer.ByteBuf proto2ByteBuf(
      final com.google.protobuf.MessageLite message) {
    int length = message.getSerializedSize();
    io.netty.buffer.ByteBuf byteBuf = io.netty.buffer.ByteBufAllocator.DEFAULT.buffer(length);
    try {
      message.writeTo(
          com.google.protobuf.CodedOutputStream.newInstance(byteBuf.internalNioBuffer(0, length)));
      byteBuf.writerIndex(length);
      return byteBuf;
    } catch (Throwable t) {
      byteBuf.release();
      throw new RuntimeException(t);
    }
  }

  private static <T> Function<Payload, T> deserializer(final Parser<T> parser) {
    return new Function<Payload, T>() {
      @Override
      public T apply(Payload payload) {
        try {
          CodedInputStream is = CodedInputStream.newInstance(payload.getData());
          return parser.parseFrom(is);
        } catch (Throwable t) {
          throw new RuntimeException(t);
        } finally {
          payload.release();
        }
      }
    };
  }
}
