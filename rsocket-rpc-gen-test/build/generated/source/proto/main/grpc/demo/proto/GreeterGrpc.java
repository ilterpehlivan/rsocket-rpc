package demo.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * Define the service's operations
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.24.0)",
    comments = "Source: hello.proto")
public final class GreeterGrpc {

  private GreeterGrpc() {}

  public static final String SERVICE_NAME = "demo.proto.Greeter";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<demo.proto.HelloRequest,
      demo.proto.HelloResponse> getGreetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Greet",
      requestType = demo.proto.HelloRequest.class,
      responseType = demo.proto.HelloResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<demo.proto.HelloRequest,
      demo.proto.HelloResponse> getGreetMethod() {
    io.grpc.MethodDescriptor<demo.proto.HelloRequest, demo.proto.HelloResponse> getGreetMethod;
    if ((getGreetMethod = GreeterGrpc.getGreetMethod) == null) {
      synchronized (GreeterGrpc.class) {
        if ((getGreetMethod = GreeterGrpc.getGreetMethod) == null) {
          GreeterGrpc.getGreetMethod = getGreetMethod =
              io.grpc.MethodDescriptor.<demo.proto.HelloRequest, demo.proto.HelloResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Greet"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  demo.proto.HelloRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  demo.proto.HelloResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GreeterMethodDescriptorSupplier("Greet"))
              .build();
        }
      }
    }
    return getGreetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<demo.proto.HelloRequest,
      demo.proto.HelloResponse> getMultiGreetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MultiGreet",
      requestType = demo.proto.HelloRequest.class,
      responseType = demo.proto.HelloResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<demo.proto.HelloRequest,
      demo.proto.HelloResponse> getMultiGreetMethod() {
    io.grpc.MethodDescriptor<demo.proto.HelloRequest, demo.proto.HelloResponse> getMultiGreetMethod;
    if ((getMultiGreetMethod = GreeterGrpc.getMultiGreetMethod) == null) {
      synchronized (GreeterGrpc.class) {
        if ((getMultiGreetMethod = GreeterGrpc.getMultiGreetMethod) == null) {
          GreeterGrpc.getMultiGreetMethod = getMultiGreetMethod =
              io.grpc.MethodDescriptor.<demo.proto.HelloRequest, demo.proto.HelloResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "MultiGreet"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  demo.proto.HelloRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  demo.proto.HelloResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GreeterMethodDescriptorSupplier("MultiGreet"))
              .build();
        }
      }
    }
    return getMultiGreetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<demo.proto.HelloRequest,
      demo.proto.HelloResponse> getStreamGreetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StreamGreet",
      requestType = demo.proto.HelloRequest.class,
      responseType = demo.proto.HelloResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<demo.proto.HelloRequest,
      demo.proto.HelloResponse> getStreamGreetMethod() {
    io.grpc.MethodDescriptor<demo.proto.HelloRequest, demo.proto.HelloResponse> getStreamGreetMethod;
    if ((getStreamGreetMethod = GreeterGrpc.getStreamGreetMethod) == null) {
      synchronized (GreeterGrpc.class) {
        if ((getStreamGreetMethod = GreeterGrpc.getStreamGreetMethod) == null) {
          GreeterGrpc.getStreamGreetMethod = getStreamGreetMethod =
              io.grpc.MethodDescriptor.<demo.proto.HelloRequest, demo.proto.HelloResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StreamGreet"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  demo.proto.HelloRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  demo.proto.HelloResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GreeterMethodDescriptorSupplier("StreamGreet"))
              .build();
        }
      }
    }
    return getStreamGreetMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GreeterStub newStub(io.grpc.Channel channel) {
    return new GreeterStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GreeterBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new GreeterBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GreeterFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new GreeterFutureStub(channel);
  }

  /**
   * <pre>
   * Define the service's operations
   * </pre>
   */
  public static abstract class GreeterImplBase implements io.grpc.BindableService {

    /**
     */
    public void greet(demo.proto.HelloRequest request,
        io.grpc.stub.StreamObserver<demo.proto.HelloResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGreetMethod(), responseObserver);
    }

    /**
     */
    public void multiGreet(demo.proto.HelloRequest request,
        io.grpc.stub.StreamObserver<demo.proto.HelloResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMultiGreetMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<demo.proto.HelloRequest> streamGreet(
        io.grpc.stub.StreamObserver<demo.proto.HelloResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getStreamGreetMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGreetMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                demo.proto.HelloRequest,
                demo.proto.HelloResponse>(
                  this, METHODID_GREET)))
          .addMethod(
            getMultiGreetMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                demo.proto.HelloRequest,
                demo.proto.HelloResponse>(
                  this, METHODID_MULTI_GREET)))
          .addMethod(
            getStreamGreetMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                demo.proto.HelloRequest,
                demo.proto.HelloResponse>(
                  this, METHODID_STREAM_GREET)))
          .build();
    }
  }

  /**
   * <pre>
   * Define the service's operations
   * </pre>
   */
  public static final class GreeterStub extends io.grpc.stub.AbstractStub<GreeterStub> {
    private GreeterStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GreeterStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GreeterStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GreeterStub(channel, callOptions);
    }

    /**
     */
    public void greet(demo.proto.HelloRequest request,
        io.grpc.stub.StreamObserver<demo.proto.HelloResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGreetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void multiGreet(demo.proto.HelloRequest request,
        io.grpc.stub.StreamObserver<demo.proto.HelloResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getMultiGreetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<demo.proto.HelloRequest> streamGreet(
        io.grpc.stub.StreamObserver<demo.proto.HelloResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getStreamGreetMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * Define the service's operations
   * </pre>
   */
  public static final class GreeterBlockingStub extends io.grpc.stub.AbstractStub<GreeterBlockingStub> {
    private GreeterBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GreeterBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GreeterBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GreeterBlockingStub(channel, callOptions);
    }

    /**
     */
    public demo.proto.HelloResponse greet(demo.proto.HelloRequest request) {
      return blockingUnaryCall(
          getChannel(), getGreetMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<demo.proto.HelloResponse> multiGreet(
        demo.proto.HelloRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getMultiGreetMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Define the service's operations
   * </pre>
   */
  public static final class GreeterFutureStub extends io.grpc.stub.AbstractStub<GreeterFutureStub> {
    private GreeterFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GreeterFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GreeterFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GreeterFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<demo.proto.HelloResponse> greet(
        demo.proto.HelloRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGreetMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GREET = 0;
  private static final int METHODID_MULTI_GREET = 1;
  private static final int METHODID_STREAM_GREET = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final GreeterImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(GreeterImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GREET:
          serviceImpl.greet((demo.proto.HelloRequest) request,
              (io.grpc.stub.StreamObserver<demo.proto.HelloResponse>) responseObserver);
          break;
        case METHODID_MULTI_GREET:
          serviceImpl.multiGreet((demo.proto.HelloRequest) request,
              (io.grpc.stub.StreamObserver<demo.proto.HelloResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_STREAM_GREET:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.streamGreet(
              (io.grpc.stub.StreamObserver<demo.proto.HelloResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class GreeterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GreeterBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return demo.proto.HelloWorldProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Greeter");
    }
  }

  private static final class GreeterFileDescriptorSupplier
      extends GreeterBaseDescriptorSupplier {
    GreeterFileDescriptorSupplier() {}
  }

  private static final class GreeterMethodDescriptorSupplier
      extends GreeterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    GreeterMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (GreeterGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GreeterFileDescriptorSupplier())
              .addMethod(getGreetMethod())
              .addMethod(getMultiGreetMethod())
              .addMethod(getStreamGreetMethod())
              .build();
        }
      }
    }
    return result;
  }
}
