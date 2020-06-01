package io.rsocket.rpc.core.extension.routing;

import static io.grpc.MethodDescriptor.generateFullMethodName;

import com.google.common.base.Preconditions;
import com.google.protobuf.Message;
import io.grpc.HandlerRegistry;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import io.rsocket.Payload;
import io.rsocket.rpc.core.extension.error.RpcServiceCallError;
import io.rsocket.rpc.core.extension.error.ServiceNotFound;
import io.rsocket.rpc.core.extension.metadata.MetaDataUtil;
import io.rsocket.rpc.core.extension.metadata.MetaDataUtil.RsocketRpcRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** This class is called for per call and NotThreadSafe */
@Slf4j
public class RSocketServerCall {

  private final HandlerRegistry registry;
  private CorePublisher<? extends Message> functionStream;
  private Message request;
  private Marshaller<? extends Message> requestMarshaller;

  public RSocketServerCall(HandlerRegistry registry) {
    this.registry = registry;
  }

  public Mono<Payload> invokeService(Payload payload) {
    UnaryServerCallHandler<Message, Message> unaryServerCallHandler =
        parsePayloadAndGetHandler(payload);
    return Mono.from(unaryServerCallHandler.startCall(getRequest(payload), null))
        .map(serializer)
        .doOnNext(
            resp -> {
              if (log.isTraceEnabled()) log.trace("response on the server {}", resp);
            })
        .doOnError(
            er -> {
              handleServiceCallError(er);
            });
  }

  public Flux<Payload> invokeStreamService(Payload payload) {
    UnaryServerCallHandler<Message, Message> unaryServerCallHandler =
        parsePayloadAndGetHandler(payload);
    return Flux.from(unaryServerCallHandler.startCall(getRequest(payload), null))
        .map(serializer)
        .doOnNext(
            resp -> {
              if (log.isTraceEnabled()) log.trace("response on the server {}", resp);
            })
        .doOnError(
            er -> {
              handleServiceCallError(er);
            });
  }

  public Flux<Payload> invokeBiDirectionService(Publisher<Payload> payloads) {
    return Flux.from(payloads)
        .switchOnFirst(
            (first, payloadFlux) -> {
              if (first.hasValue()) {
                Payload payload = first.get();
                UnaryServerCallHandler<Message, Message> unaryServerCallHandler =
                    parsePayloadAndGetHandler(payload);

                return payloadFlux
                    .skip(1)
                    .startWith(payload)
                    .flatMap(p -> unaryServerCallHandler.startCall(getRequest(p), null))
                    .map(serializer);
              }
              return payloadFlux;
            })
        .doOnError(
            er -> {
              handleServiceCallError(er);
            });
  }

  private void handleServiceCallError(Throwable er) {
    log.error("error received {}", er);
    if (log.isDebugEnabled()) {
      log.debug("error during the service call {} .Converting to RPC error", er.getMessage());
    }
    Mono.error(new RpcServiceCallError(er));
  }

  private Mono<ServerCallHandler<? extends Message, ? extends Message>>
      parsePayloadAndGetHandlerMono(Payload payload) {
    return Mono.defer(
        () -> {
          ServerCallHandler<? extends Message, ? extends Message> serverCallHandler = null;
          try {
            serverCallHandler = parsePayloadAndGetHandler(payload);
          } catch (NullPointerException e) {
            return Mono.error(new ServiceNotFound(e.getMessage()));
          }
          return Mono.just(serverCallHandler);
        });
  }

  private UnaryServerCallHandler<Message, Message> parsePayloadAndGetHandler(Payload payload) {
    ByteBuf metadata = null;
    UnaryServerCallHandler<Message, Message> serverCallHandler = null;
    try {
      metadata = payload.sliceMetadata();
      // String service = io.rsocket.rpc.frames.Metadata.getService(metadata);
      // String method = io.rsocket.rpc.frames.Metadata.getMethod(metadata);
      RsocketRpcRequest request = MetaDataUtil.getRpcRequest(metadata);
      String service = request.getService();
      String method = request.getMethod();
      if (log.isDebugEnabled()) {
        log.debug("message received service {} method {}", service, method);
      }

      ServerMethodDefinition<?, ?> serverMethodDefinition =
          registry.lookupMethod(generateFullMethodName(service, method));
      if (serverMethodDefinition == null) {
        ReferenceCountUtil.safeRelease(payload);
        throw new NullPointerException(
            String.format("server method %s could not be found", service));
      }
      serverCallHandler =
          (UnaryServerCallHandler<Message, Message>)
              serverMethodDefinition.getServerCallHandler();

      // Convert the payload to Proto object by using embedded marshaller from stub
      Marshaller<? extends Message> requestMarshaller =
          (Marshaller<? extends Message>)
              serverMethodDefinition.getMethodDescriptor().getRequestMarshaller();
      // set marshaller to be used later in the flow
      setRequestMarshaller(requestMarshaller);
    } catch (Exception e) {
      ReferenceCountUtil.safeRelease(payload);
      throw e;
    }
    metadata.release();
    return serverCallHandler;
  }

  private void setRequestMarshaller(Marshaller<? extends Message> requestMarshaller) {
    this.requestMarshaller = requestMarshaller;
  }

  // TODO: consider to use the response marshaller from the method description of stub
  private static final java.util.function.Function<com.google.protobuf.Message, io.rsocket.Payload>
      serializer =
          message -> {
            int length = message.getSerializedSize();
            ByteBuf byteBuf = io.netty.buffer.ByteBufAllocator.DEFAULT.buffer(length);
            try {
              message.writeTo(
                  com.google.protobuf.CodedOutputStream.newInstance(
                      byteBuf.internalNioBuffer(0, length)));
              byteBuf.writerIndex(length);
              return io.rsocket.util.ByteBufPayload.create(byteBuf);
            } catch (Throwable t) {
              byteBuf.release();
              throw new RuntimeException(t);
            }
          };

  /**
   * This function is responsible to store the reference of the stream from the function results
   *
   * @param resultStream
   */
  // TODO: check the synchronized performance impact
  @Deprecated
  public synchronized void createStream(CorePublisher resultStream) {
    Preconditions.checkNotNull(resultStream, "No Stream:service call resulted with error");
    if (functionStream == null) {
      functionStream = resultStream;
    } else {
      // This is for streaming functions
      functionStream = Flux.concat(functionStream, resultStream);
    }
  }

  public Message getRequest(Payload payload) {
    InputStream is = new ByteArrayInputStream(ByteBufUtil.getBytes(payload.data()));
    // Set the converted request
    return requestMarshaller.parse(is);
  }
}
