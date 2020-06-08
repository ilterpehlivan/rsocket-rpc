//TODO: need to rewrite these tests

//package io.rsocket.rpc.core.extension.routing;
//
//import static io.grpc.MethodDescriptor.generateFullMethodName;
//
//import com.google.protobuf.CodedOutputStream;
//import com.google.protobuf.Empty;
//import com.google.protobuf.MessageLite;
//import io.grpc.BindableService;
//import io.grpc.HandlerRegistry;
//import io.grpc.MethodDescriptor;
//import io.grpc.MethodDescriptor.MethodType;
//import io.grpc.ServerServiceDefinition;
//import io.grpc.protobuf.ProtoUtils;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.ByteBufAllocator;
//import io.netty.buffer.ByteBufUtil;
//import io.netty.buffer.Unpooled;
//import io.rsocket.Payload;
//import io.rsocket.frame.PayloadFrameFlyweight;
//import io.rsocket.rpc.core.extension.routing.ServerCalls.BiDirectionStreamingMethod;
//import io.rsocket.rpc.core.extension.routing.ServerCalls.ClientStreamingMethod;
//import io.rsocket.rpc.core.extension.routing.ServerCalls.FireAndForgetMethod;
//import io.rsocket.rpc.core.extension.routing.ServerCalls.RequestResponseMethod;
//import io.rsocket.rpc.core.extension.routing.ServiceHandlerRegistry.Builder;
//import io.rsocket.rpc.extension.sample.proto.SimpleRequest;
//import io.rsocket.rpc.extension.sample.proto.SimpleResponse;
//import io.rsocket.rpc.frames.Metadata;
//import io.rsocket.util.ByteBufPayload;
//import io.rsocket.util.DefaultPayload;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.reactivestreams.Publisher;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//@Slf4j
//public class RSocketServerCallTest {
//
//  public static final String TEST_SERVICE = "TestService";
//  private static final String TEST_METHOD_NAME = "sayHello";
//  private static final String TEST_FIRE_FORGET_METHOD_NAME = "fireForget";
//  private static final String TEST_REQUEST_STREAM_METHOD_NAME = "streamHello";
//  private static final String TEST_BIDI_STREAM_METHOD_NAME = "bidiHello";
//
//  static HandlerRegistry registry;
//
//  @BeforeClass
//  public static void beforeClass() throws Exception {
//    // setup the registry
//    Builder builder = new Builder();
//    builder.addService(new TestSimpleService().bindService());
//    registry = builder.build();
//  }
//
//  @Test
//  public void shouldServerCallInvokeMethodForRequestReplyThenValidateResults() {
//    SimpleRequest request = SimpleRequest.newBuilder().setRequestMessage("hello").build();
//    ByteBuf metaData =
//        Metadata.encode(
//            ByteBufAllocator.DEFAULT, TEST_SERVICE, TEST_METHOD_NAME, Unpooled.EMPTY_BUFFER);
//    Payload samplePayload =
//        ByteBufPayload.create(request.toByteArray(), ByteBufUtil.getBytes(metaData));
//    log.info("invoking the service payload data: {} meta:{}",samplePayload.getDataUtf8(),samplePayload.getMetadataUtf8());
//    new RSocketServerCall(registry)
//        .invokeService(samplePayload)
//        .subscribe(result -> log.info("result {}", result));
//    log.info("end of the test");
//  }
//
//  private static ByteBuf serialize(final MessageLite message) {
//    int length = message.getSerializedSize();
//    ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(length);
//    try {
//      message.writeTo(CodedOutputStream.newInstance(byteBuf.internalNioBuffer(0, length)));
//      byteBuf.writerIndex(length);
//      return byteBuf;
//    } catch (Throwable t) {
//      byteBuf.release();
//      throw new RuntimeException(t);
//    }
//  }
//  @Test
//  public void shouldServerCallInvokeMethodForFireForgetThenValidateResults() {
//    SimpleRequest request = SimpleRequest.newBuilder().setRequestMessage("hello").build();
//    ByteBuf metaData =
//        Metadata.encode(
//            ByteBufAllocator.DEFAULT,
//            TEST_SERVICE,
//            TEST_FIRE_FORGET_METHOD_NAME,
//            Unpooled.EMPTY_BUFFER);
//    Payload samplePayload =
//        DefaultPayload.create(request.toByteArray(), ByteBufUtil.getBytes(metaData));
//    log.info("invoking the service");
//    new RSocketServerCall(registry)
//        .invokeService(samplePayload)
//        .subscribe(result -> log.info("result {}", result.getData()));
//    log.info("end of the test");
//  }
//
//  @Test
//  public void shouldServerCallInvokeMethodForRequestStreamThenValidateResults() {
//    SimpleRequest request = SimpleRequest.newBuilder().setRequestMessage("hello").build();
//    ByteBuf metaData =
//        Metadata.encode(
//            ByteBufAllocator.DEFAULT,
//            TEST_SERVICE,
//            TEST_REQUEST_STREAM_METHOD_NAME,
//            Unpooled.EMPTY_BUFFER);
//    Payload samplePayload =
//        DefaultPayload.create(request.toByteArray(), ByteBufUtil.getBytes(metaData));
//    log.info("invoking the service");
//    new RSocketServerCall(registry)
//        .invokeStreamService(samplePayload)
//        .subscribe(result -> log.info("result {}", result.getData()));
//    log.info("end of the test");
//  }
//
//  /** @return {@link ByteBuf} representing test instance of Payload frame */
//  public ByteBuf createTestPayloadFrame() {
//    return PayloadFrameFlyweight.encode(
//        ByteBufAllocator.DEFAULT, 1, false, true, false, null, Unpooled.EMPTY_BUFFER);
//  }
//
//
//  // Below is supposed to be created as STUB
//  private static final int METHOD_REQUEST_RESPONSE = 1;
//  private static final int METHOD_FIRE_FORGET = 2;
//  private static final int METHOD_REQUEST_STREAM = 3;
//  private static final int METHOD_BIDI_STREAM = 4;
//
//  private abstract static class TestBindableService<Req, Resp> implements BindableService {
//
//    // Those methods are auto-generated by plugin
//    public abstract reactor.core.publisher.Mono<Resp> sayHello(
//        Req message, io.netty.buffer.ByteBuf metadata);
//
//    public abstract reactor.core.publisher.Mono<Void> fireForget(
//        Req message, io.netty.buffer.ByteBuf metadata);
//
//    public abstract reactor.core.publisher.Flux<SimpleResponse> streamHello(
//        Req message, io.netty.buffer.ByteBuf metadata);
//
//    public abstract reactor.core.publisher.Flux<SimpleResponse> bidiHello(
//        Flux<Req> messages, io.netty.buffer.ByteBuf metadata);
//
//    @Override
//    public ServerServiceDefinition bindService() {
//      return ServerServiceDefinition.builder(TEST_SERVICE)
//          .addMethod(
//              getRequestResponseMethod(TEST_SERVICE,TEST_METHOD_NAME, MethodType.UNARY),
//              ServerCalls.requestReply(
//                  new MethodHandlers<SimpleRequest, SimpleResponse>(this, METHOD_REQUEST_RESPONSE)))
//          .addMethod(
//              getFireForgetMethod(TEST_SERVICE,TEST_FIRE_FORGET_METHOD_NAME, MethodType.UNARY),
//              ServerCalls.requestReply(
//                  new MethodHandlers<SimpleRequest, Empty>(this, METHOD_FIRE_FORGET)))
//          .addMethod(
//              getRequestResponseMethod(TEST_SERVICE,TEST_REQUEST_STREAM_METHOD_NAME, MethodType.CLIENT_STREAMING),
//              ServerCalls.requestReply(
//                  new MethodHandlers<SimpleRequest, SimpleResponse>(this, METHOD_REQUEST_STREAM)))
//          .addMethod(
//              getRequestResponseMethod(TEST_SERVICE,TEST_BIDI_STREAM_METHOD_NAME, MethodType.BIDI_STREAMING),
//              ServerCalls.requestReply(
//                  new MethodHandlers<SimpleRequest, SimpleResponse>(this, METHOD_BIDI_STREAM)))
//          .build();
//    }
//
//    private MethodDescriptor<SimpleRequest, SimpleResponse> getRequestResponseMethod(String service,
//        String method, MethodType methodType) {
//      return MethodDescriptor.<SimpleRequest, SimpleResponse>newBuilder()
//          .setFullMethodName(generateFullMethodName(service,method))
//          .setType(methodType)
//          .setRequestMarshaller(ProtoUtils.marshaller(SimpleRequest.getDefaultInstance()))
//          .setResponseMarshaller(ProtoUtils.marshaller(SimpleResponse.getDefaultInstance()))
//          .setSampledToLocalTracing(true)
//          .build();
//    }
//
//    private MethodDescriptor<SimpleRequest, Empty> getFireForgetMethod(String service,
//        String method, MethodType methodType) {
//      return MethodDescriptor.<SimpleRequest, Empty>newBuilder()
//          .setFullMethodName(generateFullMethodName(service,method))
//          .setType(methodType)
//          .setRequestMarshaller(ProtoUtils.marshaller(SimpleRequest.getDefaultInstance()))
//          .setResponseMarshaller(ProtoUtils.marshaller(Empty.getDefaultInstance()))
//          .setSampledToLocalTracing(true)
//          .build();
//    }
//
//    private class MethodHandlers<Req, Resp>
//        implements RequestResponseMethod<Req, Resp>,
//            FireAndForgetMethod<Req, Resp>,
//            ClientStreamingMethod<Req, Resp>,
//            BiDirectionStreamingMethod<Req, Resp> {
//
//      private final TestBindableService serviceImpl;
//      private final int methodId;
//
//      public MethodHandlers(TestBindableService service, int methodRequestResponse) {
//        this.methodId = methodRequestResponse;
//        this.serviceImpl = service;
//      }
//
//      @Override
//      public Flux<Resp> apply(Req req, ByteBuf byteBuf) {
//        log.info("inside the apply for the methodid {}", methodId);
//        switch (methodId) {
//          case METHOD_REQUEST_RESPONSE:
//            return serviceImpl.sayHello(req, byteBuf).flux();
//          case METHOD_FIRE_FORGET:
//            return serviceImpl.fireForget(req, byteBuf).flux();
//          default:
//            {
//              return reactor.core.publisher.Flux.error(new UnsupportedOperationException());
//            }
//        }
//      }
//
//      // TODO: merge this with apply
////      @Override
////      public Flux<Resp> applyMany(Req req, ByteBuf byteBuf) {
////        log.info("inside the applyMany for the methodid {}", methodId);
////        switch (methodId) {
////          case METHOD_REQUEST_STREAM:
////            return serviceImpl.streamHello(req, byteBuf);
////          default:
////            {
////              return reactor.core.publisher.Flux.error(new UnsupportedOperationException());
////            }
////        }
////      }
//
//      @Override
//      public Flux<Resp> apply(Publisher<Req> publisher, ByteBuf byteBuf) {
//        log.info("inside the apply bidirect for the methodid {}", methodId);
//        switch (methodId) {
//          case METHOD_BIDI_STREAM:
//            return serviceImpl.bidiHello(Flux.from(publisher), byteBuf);
//          default:
//            {
//              return reactor.core.publisher.Flux.error(new UnsupportedOperationException());
//            }
//        }
//      }
//    }
//  }
//
//  // Actual implementation of the service
//  private static class TestSimpleService
//      extends TestBindableService<SimpleRequest, SimpleResponse> {
//    @Override
//    public Mono<SimpleResponse> sayHello(SimpleRequest message, ByteBuf metadata) {
//      log.info("inside the sayHello function");
//      return Mono.just(SimpleResponse.newBuilder().setResponseMessage("hello").build());
//    }
//
//    @Override
//    public Mono<Void> fireForget(SimpleRequest message, ByteBuf metadata) {
//      log.info("inside the fireForget function");
//      return Mono.empty();
//    }
//
//    @Override
//    public Flux<SimpleResponse> streamHello(SimpleRequest message, ByteBuf metadata) {
//      return Flux.create(
//          emmiter -> {
//            for (int i = 0; i < 10; i++)
//              emmiter.next(SimpleResponse.newBuilder().setResponseMessage("hello" + i).build());
//            emmiter.complete();
//          });
//    }
//
//    @Override
//    public Flux<SimpleResponse> bidiHello(Flux<SimpleRequest> messages, ByteBuf metadata) {
//      return null;
//    }
//  }
//}
