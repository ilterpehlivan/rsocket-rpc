package com.salesforce.jprotoc;

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
comments = "Source: helloworld.proto")
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
     *  The greeting service definition.
     * </pre>
     */
    public static final class RsocketGreeterStub extends GreeterImplBase {

        private final Mono<RSocket> rSocketMono;

        private RsocketGreeterStub(Mono<RSocket> rSocketMono) {
            this.rSocketMono = rSocketMono;
        }


          public reactor.core.publisher.Mono<com.salesforce.jprotoc.HelloWorldProto.HelloResponse> sayHello(com.salesforce.jprotoc.HelloWorldProto.HelloRequest request,io.netty.buffer.ByteBuf metadata) {
          return io.rsocket.rpc.core.extension.routing.ClientCalls.requestReply(
          rSocketMono,
          request,
          metadata,
          GreeterImplBase.SERVICE,
          "sayHello",
          com.salesforce.jprotoc.HelloWorldProto.HelloResponse.parser());
          }

        public reactor.core.publisher.Mono<com.salesforce.jprotoc.HelloWorldProto.HelloResponse> sayHello(com.salesforce.jprotoc.HelloWorldProto.HelloRequest request) {
            return sayHello(
          request,
          io.netty.buffer.Unpooled.EMPTY_BUFFER);
        }


            @java.lang.Deprecated
          public reactor.core.publisher.Mono<com.salesforce.jprotoc.HelloWorldProto.TimeResponse> sayTime(com.google.protobuf.Empty request,io.netty.buffer.ByteBuf metadata) {
          return io.rsocket.rpc.core.extension.routing.ClientCalls.requestReply(
          rSocketMono,
          request,
          metadata,
          GreeterImplBase.SERVICE,
          "sayTime",
          com.salesforce.jprotoc.HelloWorldProto.TimeResponse.parser());
          }

        @java.lang.Deprecated
        public reactor.core.publisher.Mono<com.salesforce.jprotoc.HelloWorldProto.TimeResponse> sayTime(com.google.protobuf.Empty request) {
            return sayTime(
          request,
          io.netty.buffer.Unpooled.EMPTY_BUFFER);
        }


          public reactor.core.publisher.Mono<my.someparameters.SomeParameterOuterClass.SomeParameter> someParam(my.someparameters.SomeParameterOuterClass.SomeParameter request,io.netty.buffer.ByteBuf metadata) {
          return io.rsocket.rpc.core.extension.routing.ClientCalls.requestReply(
          rSocketMono,
          request,
          metadata,
          GreeterImplBase.SERVICE,
          "someParam",
          my.someparameters.SomeParameterOuterClass.SomeParameter.parser());
          }

        public reactor.core.publisher.Mono<my.someparameters.SomeParameterOuterClass.SomeParameter> someParam(my.someparameters.SomeParameterOuterClass.SomeParameter request) {
            return someParam(
          request,
          io.netty.buffer.Unpooled.EMPTY_BUFFER);
        }


          public reactor.core.publisher.Mono<com.google.protobuf.Empty> fireAndForget(my.someparameters.SomeParameterOuterClass.SomeParameter request,io.netty.buffer.ByteBuf metadata) {
          return io.rsocket.rpc.core.extension.routing.ClientCalls.requestReply(
          rSocketMono,
          request,
          metadata,
          GreeterImplBase.SERVICE,
          "fireAndForget",
          com.google.protobuf.Empty.parser());
          }

        public reactor.core.publisher.Mono<com.google.protobuf.Empty> fireAndForget(my.someparameters.SomeParameterOuterClass.SomeParameter request) {
            return fireAndForget(
          request,
          io.netty.buffer.Unpooled.EMPTY_BUFFER);
        }


    }

    /**
     * <pre>
     *  The greeting service definition.
     * </pre>
     */
    public static abstract class GreeterImplBase implements io.grpc.BindableService {

        public static String SERVICE = "com.salesforce.jprotoc.Greeter";

        public reactor.core.publisher.Mono<com.salesforce.jprotoc.HelloWorldProto.HelloResponse> sayHello(com.salesforce.jprotoc.HelloWorldProto.HelloRequest request,io.netty.buffer.ByteBuf metadata) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        @java.lang.Deprecated
        public reactor.core.publisher.Mono<com.salesforce.jprotoc.HelloWorldProto.TimeResponse> sayTime(com.google.protobuf.Empty request,io.netty.buffer.ByteBuf metadata) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        public reactor.core.publisher.Mono<my.someparameters.SomeParameterOuterClass.SomeParameter> someParam(my.someparameters.SomeParameterOuterClass.SomeParameter request,io.netty.buffer.ByteBuf metadata) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        public reactor.core.publisher.Mono<com.google.protobuf.Empty> fireAndForget(my.someparameters.SomeParameterOuterClass.SomeParameter request,io.netty.buffer.ByteBuf metadata) {
            throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
        }

        @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(SERVICE)
                      .addMethod(
                            getMethod(SERVICE,
                                     METHOD_SAY_HELLO,
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.asyncUnaryCall(),
                                     io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"),
                                     com.salesforce.jprotoc.HelloWorldProto.HelloRequest.getDefaultInstance(),
                                     com.salesforce.jprotoc.HelloWorldProto.HelloResponse.getDefaultInstance()),
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.requestReply(new MethodHandlers<com.salesforce.jprotoc.HelloWorldProto.HelloRequest, com.salesforce.jprotoc.HelloWorldProto.HelloResponse>(
                                     this, METHODID_SAY_HELLO)))

                      .addMethod(
                            getMethod(SERVICE,
                                     METHOD_SAY_TIME,
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.asyncUnaryCall(),
                                     io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"),
                                     com.google.protobuf.Empty.getDefaultInstance(),
                                     com.salesforce.jprotoc.HelloWorldProto.TimeResponse.getDefaultInstance()),
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.requestReply(new MethodHandlers<com.google.protobuf.Empty, com.salesforce.jprotoc.HelloWorldProto.TimeResponse>(
                                     this, METHODID_SAY_TIME)))

                      .addMethod(
                            getMethod(SERVICE,
                                     METHOD_SOME_PARAM,
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.asyncUnaryCall(),
                                     io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"),
                                     my.someparameters.SomeParameterOuterClass.SomeParameter.getDefaultInstance(),
                                     my.someparameters.SomeParameterOuterClass.SomeParameter.getDefaultInstance()),
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.requestReply(new MethodHandlers<my.someparameters.SomeParameterOuterClass.SomeParameter, my.someparameters.SomeParameterOuterClass.SomeParameter>(
                                     this, METHODID_SOME_PARAM)))

                      .addMethod(
                            getMethod(SERVICE,
                                     METHOD_FIRE_AND_FORGET,
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.asyncUnaryCall(),
                                     io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"),
                                     my.someparameters.SomeParameterOuterClass.SomeParameter.getDefaultInstance(),
                                     com.google.protobuf.Empty.getDefaultInstance()),
                                     io.rsocket.rpc.core.extension.routing.ServerCalls.requestReply(new MethodHandlers<my.someparameters.SomeParameterOuterClass.SomeParameter, com.google.protobuf.Empty>(
                                     this, METHODID_FIRE_AND_FORGET)))

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
          .put(io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"), "sayHello")
          .put(io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"), "sayTime")
          .put(io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"), "someParam")
          .put(io.rsocket.rpc.core.extension.routing.SchemaDescriptor.getRpcMethod("requestReply"), "fireAndForget")
          .build();

    private static final int METHODID_SAY_HELLO = 0;
    private static final String METHOD_SAY_HELLO = "sayHello";
    private static final int METHODID_SAY_TIME = 1;
    private static final String METHOD_SAY_TIME = "sayTime";
    private static final int METHODID_SOME_PARAM = 2;
    private static final String METHOD_SOME_PARAM = "someParam";
    private static final int METHODID_FIRE_AND_FORGET = 3;
    private static final String METHOD_FIRE_AND_FORGET = "fireAndForget";

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
        case METHODID_SAY_HELLO:
          return (CorePublisher<Resp>) serviceImpl.sayHello((com.salesforce.jprotoc.HelloWorldProto.HelloRequest) req, byteBuf);
        case METHODID_SAY_TIME:
          return (CorePublisher<Resp>) serviceImpl.sayTime((com.google.protobuf.Empty) req, byteBuf);
        case METHODID_SOME_PARAM:
          return (CorePublisher<Resp>) serviceImpl.someParam((my.someparameters.SomeParameterOuterClass.SomeParameter) req, byteBuf);
        case METHODID_FIRE_AND_FORGET:
          return (CorePublisher<Resp>) serviceImpl.fireAndForget((my.someparameters.SomeParameterOuterClass.SomeParameter) req, byteBuf);
        default:
          return reactor.core.publisher.Flux.error(new UnsupportedOperationException());
      }
    }

      @Override
      @java.lang.SuppressWarnings("unchecked")
      public Flux<Resp> apply(Publisher<Req> publisher, io.netty.buffer.ByteBuf byteBuf) {
        switch (methodId) {
                default:
                  return reactor.core.publisher.Flux.error(new UnsupportedOperationException());
            }
      }


    }
}