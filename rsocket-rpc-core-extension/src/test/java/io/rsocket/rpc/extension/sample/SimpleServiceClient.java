package io.rsocket.rpc.extension.sample;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Empty;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.rpc.annotations.internal.GeneratedMethod;
import io.rsocket.rpc.annotations.internal.ResourceType;
import io.rsocket.rpc.core.extension.routing.ClientCalls;
import io.rsocket.rpc.core.extension.RsocketClientBuilder.Client;
import io.rsocket.rpc.extension.sample.proto.SimpleRequest;
import io.rsocket.rpc.extension.sample.proto.SimpleResponse;
import io.rsocket.rpc.frames.Metadata;
import io.rsocket.rpc.tracing.Tracing;
import io.rsocket.util.ByteBufPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Generated;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Generated(value = "by RSocket RPC proto compiler", comments = "Source: service.proto")
@io.rsocket.rpc.annotations.internal.Generated(
    type = ResourceType.CLIENT,
    idlClass = SimpleService.class)
public final class SimpleServiceClient extends SimpleServiceBase {
  private final Mono<RSocket> rSocketMono;

  public static SimpleServiceClient newReactiveStub(Client client) {
    return new SimpleServiceClient(client.getrSocketMono());
  }

  private SimpleServiceClient(Mono<RSocket> rSocketMono) {
    this.rSocketMono = rSocketMono;
  }

  @GeneratedMethod(returnTypeClass = SimpleResponse.class)
  public Mono<SimpleResponse> requestReply(SimpleRequest message) {
    return requestReply(message, Unpooled.EMPTY_BUFFER);
  }

  @Override
  @GeneratedMethod(returnTypeClass = SimpleResponse.class)
  public Mono<SimpleResponse> requestReply(SimpleRequest message, ByteBuf metadata) {
    return ClientCalls.requestReply(
        rSocketMono,
        message,
        metadata,
        SimpleServiceBase.SERVICE,
        SimpleServiceBase.METHOD_REQUEST_REPLY,
        SimpleResponse.parser());
    //    return Mono.defer(
    //            new Supplier<Mono<Payload>>() {
    //              @Override
    //              public Mono<Payload> get() {
    //                //                final ByteBuf data = serialize(message);
    //                final ByteBuf metadataBuf =
    //                    MetaDataUtil.encodeRpcComposite(
    //                        SimpleServiceBase.SERVICE,
    //                        SimpleServiceBase.METHOD_REQUEST_REPLY,
    //                        metadata
    //                    );
    ////                    MetaDataUtil.encode(
    ////                        ByteBufAllocator.DEFAULT,
    ////                        SimpleServiceBase.SERVICE,
    ////                        SimpleServiceBase.METHOD_REQUEST_REPLY,
    ////                        metadata);
    //                metadata.release();
    //                return rSocketMono
    //                    .flatMap(
    //                        rSocket ->
    //                            rSocket.requestResponse(
    //                                ByteBufPayload.create(
    //                                    message.toByteArray(),
    // ByteBufUtil.getBytes(metadataBuf))));
    //              }
    //            })
    //        .map(deserializer(SimpleResponse.parser()));
  }

  @GeneratedMethod(returnTypeClass = Empty.class)
  public Mono<Empty> fireAndForget(SimpleRequest message) {
    return fireAndForget(message, Unpooled.EMPTY_BUFFER);
  }

  @Override
  @GeneratedMethod(returnTypeClass = Empty.class)
  public Mono<Empty> fireAndForget(SimpleRequest message, ByteBuf metadata) {
    Map<String, String> map = new HashMap<>();
    return Mono.defer(
            new Supplier<Mono<Payload>>() {
              @Override
              public Mono<Payload> get() {
                final ByteBuf data = serialize(message);
                final ByteBuf tracing = Tracing.mapToByteBuf(ByteBufAllocator.DEFAULT, map);
                final ByteBuf metadataBuf =
                    Metadata.encode(
                        ByteBufAllocator.DEFAULT,
                        SimpleServiceBase.SERVICE,
                        SimpleServiceBase.METHOD_FIRE_AND_FORGET,
                        tracing,
                        metadata);
                tracing.release();
                metadata.release();
                return rSocketMono.flatMap(
                    rSocket -> rSocket.requestResponse(ByteBufPayload.create(data, metadataBuf)));
              }
            })
        .map(deserializer(Empty.parser()));
  }

  @GeneratedMethod(returnTypeClass = SimpleResponse.class)
  public Flux<SimpleResponse> requestStream(SimpleRequest message) {
    return requestStream(message, Unpooled.EMPTY_BUFFER);
  }

  @Override
  @GeneratedMethod(returnTypeClass = SimpleResponse.class)
  public Flux<SimpleResponse> requestStream(SimpleRequest message, ByteBuf metadata) {
    Map<String, String> map = new HashMap<>();
    return Flux.defer(
            new Supplier<Flux<Payload>>() {
              @Override
              public Flux<Payload> get() {
                final ByteBuf data = serialize(message);
                final ByteBuf tracing = Tracing.mapToByteBuf(ByteBufAllocator.DEFAULT, map);
                final ByteBuf metadataBuf =
                    Metadata.encode(
                        ByteBufAllocator.DEFAULT,
                        SimpleServiceBase.SERVICE,
                        SimpleServiceBase.METHOD_REQUEST_STREAM,
                        tracing,
                        metadata);
                tracing.release();
                metadata.release();
                return rSocketMono.flatMapMany(
                    rSocket -> rSocket.requestStream(ByteBufPayload.create(data, metadataBuf)));
              }
            })
        .map(deserializer(SimpleResponse.parser()));
  }

  // TODO:below
  //  @GeneratedMethod(returnTypeClass = SimpleResponse.class)
  //  public Mono<SimpleResponse> streamingRequestSingleResponse(Publisher<SimpleRequest> messages)
  // {
  //    return streamingRequestSingleResponse(messages, Unpooled.EMPTY_BUFFER);
  //  }
  //
  //  @Override
  //  @GeneratedMethod(returnTypeClass = SimpleResponse.class)
  //  public Mono<SimpleResponse> streamingRequestSingleResponse(
  //      Publisher<SimpleRequest> messages, ByteBuf metadata) {
  //    Map<String, String> map = new HashMap<>();
  //    return rSocketMono.flatMap(
  //        rSocket ->
  //            rSocket
  //                .requestChannel(
  //                    Flux.from(messages)
  //                        .map(
  //                            new Function<MessageLite, Payload>() {
  //                              private final AtomicBoolean once = new AtomicBoolean(false);
  //
  //                              @Override
  //                              public Payload apply(MessageLite message) {
  //                                ByteBuf data = serialize(message);
  //                                if (once.compareAndSet(false, true)) {
  //                                  final ByteBuf metadataBuf =
  //                                      Metadata.encode(
  //                                          ByteBufAllocator.DEFAULT,
  //                                          SimpleServiceBase.SERVICE,
  //                                          SimpleServiceBase
  //                                              .METHOD_STREAMING_REQUEST_SINGLE_RESPONSE,
  //                                          metadata);
  //                                  return ByteBufPayload.create(data, metadataBuf);
  //                                } else {
  //                                  return ByteBufPayload.create(data);
  //                                }
  //                              }
  //                            }))
  //                .map(deserializer(SimpleResponse.parser()))
  //                .single());
  //  }

  @GeneratedMethod(returnTypeClass = SimpleResponse.class)
  public Flux<SimpleResponse> streamingRequestAndResponse(Publisher<SimpleRequest> messages) {
    return streamingRequestAndResponse(messages, Unpooled.EMPTY_BUFFER);
  }

  @Override
  @GeneratedMethod(returnTypeClass = SimpleResponse.class)
  public Flux<SimpleResponse> streamingRequestAndResponse(
      Publisher<SimpleRequest> messages, ByteBuf metadata) {
    Map<String, String> map = new HashMap<>();
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
                                ByteBuf data = serialize(message);
                                if (once.compareAndSet(false, true)) {
                                  final ByteBuf metadataBuf =
                                      Metadata.encode(
                                          ByteBufAllocator.DEFAULT,
                                          SimpleServiceBase.SERVICE,
                                          SimpleServiceBase.METHOD_STREAMING_REQUEST_AND_RESPONSE,
                                          metadata);
                                  return ByteBufPayload.create(data, metadataBuf);
                                } else {
                                  return ByteBufPayload.create(data);
                                }
                              }
                            }))
                .map(deserializer(SimpleResponse.parser())));
  }

  private static ByteBuf serialize(final MessageLite message) {
    int length = message.getSerializedSize();
    ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(length);
    try {
      message.writeTo(CodedOutputStream.newInstance(byteBuf.internalNioBuffer(0, length)));
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
