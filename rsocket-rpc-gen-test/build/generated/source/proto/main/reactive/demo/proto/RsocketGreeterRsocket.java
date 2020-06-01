package demo.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import io.rsocket.RSocket;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.protobuf.ProtoUtils;
import io.rsocket.rpc.core.extension.routing.ServerCalls.BiDirectionStreamingMethod;
import io.rsocket.rpc.core.extension.routing.ServerCalls.ClientStreamingMethod;
import io.rsocket.rpc.core.extension.routing.ServerCalls.FireAndForgetMethod;
import io.rsocket.rpc.core.extension.routing.ServerCalls.RequestResponseMethod;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.CorePublisher;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

@javax.annotation.Generated(
value = "by Rsocket RPC generator",
comments = "Source: hello.proto")
public final class RsocketGreeterRsocket {

    private RsocketGreeterRsocket() {}

    public static RsocketGreeterStub newReactorStub(io.rsocket.rpc.core.extension.RpcClient client) {
        return new RsocketGreeterStub(client.getrSocketMono());
    }

    public static RsocketGreeterStub newReactorStub(
        io.rsocket.rpc.core.extension.RsocketClientBuilder clientBuilder) {
      return new RsocketGreeterStub(
            clientBuilder
                  .serviceName(GreeterImplBase.SERVICE)
                  .addMethods(methods)
                  .build()
                  .getrSocketMono());
    }

    /**
     * <pre>
     * 
     *  Define the service&#39;s operations
     * </pre>
     */
    public static final class RsocketGreeterStub extends GreeterImplBase {

        private final Mono<RSocket> rSocketMono;

        private RsocketGreeterStub(Mono<RSocket> rSocketMono) {
            this.rSocketMono = rSocketMono;
        }


          public reactor.core.publisher.Mono<demo.proto.HelloResponse> greet(demo.proto.HelloRequest request,io.netty.buffer.ByteBuf metadata) {
          return io.rsocket.rpc.core.extension.routing.ClientCalls.requestReply(
          rSocketMono,
          request,
          metadata,
          GreeterImplBase.SERVICE,
          "greet",
          demo.proto.HelloResponse.parser());
          }

        public reactor.core.publisher.Mono<demo.proto.HelloResponse> greet(demo.proto.HelloRequest request) {
            return greet(
          request,
          io.netty.buffer.Unpooled.EMPTY_BUFFER);
        }


          public reactor.core.publisher.Flux<demo.proto.HelloResponse> multiGreet(demo.proto.HelloRequest request,io.netty.buffer.ByteBuf metadata) {
          return io.rsocket.rpc.core.extension.routing.ClientCalls.requestStream(
          rSocketMono,
          request,
          metadata,
          GreeterImplBase.SERVICE,
          "multiGreet",
          demo.proto.HelloResponse.parser());
          }

        public reactor.core.publisher.Flux<demo.proto.HelloResponse> multiGreet(demo.proto.HelloRequest request) {
            return multiGreet(
          request,
          io.netty.buffer.Unpooled.EMPTY_BUFFER);
        }


          public reactor.core.publisher.Flux<demo.proto.HelloResponse> streamGreet(reactor.core.publisher.Flux<demo.proto.HelloRequest> request,io.netty.buffer.ByteBuf metadata) {
          return io.rsocket.rpc.core.extension.routing.ClientCalls.streamingRequestAndResponse(
          rSocketMono,
          request,
          metadata,
          GreeterImplBase.SERVICE,
          "streamGreet",
          demo.proto.HelloResponse.parser());
          }

        public reactor.core.publisher.Flux<demo.proto.HelloResponse> streamGreet(reactor.core.publisher.Flux<demo.proto.HelloRequest> request) {
            return streamGreet(
          request,
          io.netty.buffer.Unpooled.EMPTY_BUFFER);
        }


    }

    /**
     * <pre>
     * 
     *  Define the service&#39;s operations
     * </pre>
     */
    public static abstract class GreeterImplBase implements io.grpc.BindableService {

        public static String SERVICE = "demo.proto.Greeter";

        public reactor.core.publisher.Mono<demo.proto.HelloResponse> greet(demo.proto.HelloRequest request,io.netty.buffer.ByteBuf metadata) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        public reactor.core.publisher.Flux<demo.proto.HelloResponse> multiGreet(demo.proto.HelloRequest request,io.netty.buffer.ByteBuf metadata) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        public reactor.core.publisher.Flux<demo.proto.HelloResponse> streamGreet(reactor.core.publisher.Flux<demo.proto.HelloRequest> request,io.netty.buffer.ByteBuf metadata) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(SERVICE)
                      .addMethod(
                            getMethod(SERVICE,
                                     METHOD_GREET,
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.asyncUnaryCall(),
                                     io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"),
                                     demo.proto.HelloRequest.getDefaultInstance(),
                                     demo.proto.HelloResponse.getDefaultInstance()),
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.requestReply(new MethodHandlers<demo.proto.HelloRequest, demo.proto.HelloResponse>(
                                     this, METHODID_GREET)))

                      .addMethod(
                            getMethod(SERVICE,
                                     METHOD_MULTI_GREET,
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.asyncServerStreamingCall(),
                                     io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestStream"),
                                     demo.proto.HelloRequest.getDefaultInstance(),
                                     demo.proto.HelloResponse.getDefaultInstance()),
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.requestStream(new MethodHandlers<demo.proto.HelloRequest, demo.proto.HelloResponse>(
                                     this, METHODID_MULTI_GREET)))

                      .addMethod(
                            getMethod(SERVICE,
                                     METHOD_STREAM_GREET,
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.asyncBidiStreamingCall(),
                                     io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("streamingRequestAndResponse"),
                                     demo.proto.HelloRequest.getDefaultInstance(),
                                     demo.proto.HelloResponse.getDefaultInstance()),
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.streamingRequestAndResponse(new MethodHandlers<demo.proto.HelloRequest, demo.proto.HelloResponse>(
                                     this, METHODID_STREAM_GREET)))

                    .build();
        }

        private <TRequest extends com.google.protobuf.Message,TResponse extends com.google.protobuf.Message> MethodDescriptor<TRequest, TResponse> getMethod(
              String service, String method, MethodType methodType,String rpcMethodName,final TRequest requestInstance,final TResponse responseInstance) {
              return MethodDescriptor.<TRequest, TResponse>newBuilder()
                    .setFullMethodName(generateFullMethodName(service, method))
                    .setType(methodType)
                    .setSchemaDescriptor(
                        new io.rsocket.rpc.core.extension.routing.SchemaDescriptor(service, method, methodType, rpcMethodName))
                    .setRequestMarshaller(ProtoUtils.<TRequest>marshaller(requestInstance))
                    .setResponseMarshaller(ProtoUtils.<TResponse>marshaller(responseInstance))
                    .setSampledToLocalTracing(true)
                    .build();
        }
    }

    private static final Map<String, String> methods =
      ImmutableMap.<String, String>builder()
          .put(io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"), "greet")
          .put(io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestStream"), "multiGreet")
          .put(io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("streamingRequestAndResponse"), "streamGreet")
          .build();

    private static final int METHODID_GREET = 0;
    private static final String METHOD_GREET = "greet";
    private static final int METHODID_MULTI_GREET = 1;
    private static final String METHOD_MULTI_GREET = "multiGreet";
    private static final int METHODID_STREAM_GREET = 2;
    private static final String METHOD_STREAM_GREET = "streamGreet";

    private static final class MethodHandlers<Req, Resp> implements
                                RequestResponseMethod<Req, Resp>,
                                FireAndForgetMethod<Req, Resp>,
                                ClientStreamingMethod<Req, Resp>,
                                BiDirectionStreamingMethod<Req, Resp>{
        private final GreeterImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(GreeterImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

    @Override
    @java.lang.SuppressWarnings("unchecked")
    public CorePublisher<Resp> apply(Req req, io.netty.buffer.ByteBuf byteBuf) {
      switch (methodId) {
        case METHODID_GREET:
          return (CorePublisher<Resp>) serviceImpl.greet((demo.proto.HelloRequest) req, byteBuf);
        case METHODID_MULTI_GREET:
          return (CorePublisher<Resp>) serviceImpl.multiGreet((demo.proto.HelloRequest) req, byteBuf);
        default:
          return reactor.core.publisher.Flux.error(new UnsupportedOperationException());
      }
    }

      @Override
      @java.lang.SuppressWarnings("unchecked")
      public Flux<Resp> apply(Publisher<Req> publisher, io.netty.buffer.ByteBuf byteBuf) {
        switch (methodId) {
            case METHODID_STREAM_GREET:
            return (Flux<Resp>)
              serviceImpl.streamGreet(
                          Flux.from((Publisher<demo.proto.HelloRequest>) publisher), byteBuf);
                default:
                  return reactor.core.publisher.Flux.error(new UnsupportedOperationException());
            }
      }


    }
}