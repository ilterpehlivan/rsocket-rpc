package io.rsocket.rpc.core.extension.routing;

import io.grpc.MethodDescriptor.MethodType;
import io.grpc.ServerCallHandler;
import io.netty.buffer.ByteBuf;
import io.rsocket.rpc.core.extension.routing.RsocketRpcFunctions.FireAndForget;
import io.rsocket.rpc.core.extension.routing.RsocketRpcFunctions.RequestChannel;
import io.rsocket.rpc.core.extension.routing.RsocketRpcFunctions.RequestResponse;
import io.rsocket.rpc.core.extension.routing.RsocketRpcFunctions.RequestStream;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.CorePublisher;

public class ServerCalls {

  private ServerCalls() {}

  public static <ReqT, RespT> UnaryServerCallHandler<ReqT, RespT> reactiveRequestResponse(
      RequestResponse<ReqT, RespT> method) {
    return reactiveRequestResponseCall(method);
  }

  /**
   * Creates a {@link ServerCallHandler} for a unary call method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> requestReply(
      RequestResponse<ReqT, RespT> method) {
    return requestResponseCall(method);
  }

  /**
   * Creates a {@link ServerCallHandler} for a unary call method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> fireAndForget(
      RequestResponse<ReqT, RespT> method) {
    return requestResponseCall(method);
  }

  /**
   * Creates a {@link ServerCallHandler} for a unary call method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> requestStream(
      RequestStream<ReqT, RespT> method) {
    return requestStreamCall(method);
  }

  /**
   * Creates a {@link ServerCallHandler} for a unary call method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> streamingRequestAndResponse(
      RequestChannel<ReqT, RespT> method) {
    return requestChannelCall(method);
  }

  // TODO: stream request single response

  /**
   * Creates a {@link ServerCallHandler} for a unary request call method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  private static <ReqT, RespT> ServerCallHandler<ReqT, RespT> fireAndForgetCall(
      FireAndForget<ReqT> method) {
    return new FireAndForgetCallHandler<>(method);
  }

  private static <ReqT, RespT> UnaryServerCallHandler<ReqT, RespT> reactiveRequestResponseCall(
      RequestResponse<ReqT, RespT> method) {
    return new ReactiveRequestResponseCallHandler<>(method);
  }

  /**
   * Creates a {@link ServerCallHandler} for a unary request call method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  private static <ReqT, RespT> ServerCallHandler<ReqT, RespT> requestResponseCall(
      RequestResponse<ReqT, RespT> method) {
    return new ReactiveRequestResponseCallHandler<>(method);
  }

  /**
   * Creates a {@link ServerCallHandler} for a unary request call method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  private static <ReqT, RespT> ServerCallHandler<ReqT, RespT> requestStreamCall(
      RequestStream<ReqT, RespT> method) {
    return new RequestStreamCallHandler<>(method);
  }

  /**
   * Creates a {@link ServerCallHandler} for a unary request call method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  private static <ReqT, RespT> ServerCallHandler<ReqT, RespT> requestChannelCall(
      RequestChannel<ReqT, RespT> method) {
    return new RequestChannelCallHandler<>(method);
  }

  /** Adaptor to single request - response type call method. */
  public interface RequestResponseMethod<ReqT, RespT>
      extends RsocketRpcFunctions.RequestResponse<ReqT, RespT> {}

  /** Adaptor to single request empty response type call method. */
  public interface FireAndForgetMethod<ReqT, RespT>
      extends RsocketRpcFunctions.RequestResponse<ReqT, RespT> {}

  /** Adaptor to single request streaming response type call method. */
  public interface ClientStreamingMethod<ReqT, RespT>
      extends RsocketRpcFunctions.RequestStream<ReqT, RespT> {}

  /** Adaptor to stream request stream response type call method. */
  public interface BiDirectionStreamingMethod<ReqT, RespT>
      extends RsocketRpcFunctions.RequestChannel<ReqT, RespT> {}

  // TODO: stream request single response

  // Handlers

  @Slf4j
  private static final class ReactiveRequestResponseCallHandler<ReqT, RespT>
      implements UnaryServerCallHandler<ReqT, RespT> {

    private final RequestResponse requestResponse;

    // Non private to avoid synthetic class
    ReactiveRequestResponseCallHandler(RequestResponse<ReqT, RespT> method) {
      this.requestResponse = method;
    }

    @Override
    public CorePublisher<RespT> startCall(ReqT request, ByteBuf meta) {
      log.info("inside the startCall for requestResponse");
      return requestResponse.apply(request, meta);
    }
  }

  private static class FireAndForgetCallHandler<ReqT, RespT>
      implements UnaryServerCallHandler<ReqT, RespT> {

    private final FireAndForget fireAndForget;

    public <ReqT> FireAndForgetCallHandler(FireAndForget<ReqT> method) {
      this.fireAndForget = method;
    }

    @Override
    public CorePublisher<RespT> startCall(ReqT request, ByteBuf meta) {
      return null;
    }
  }

  private static class RequestStreamCallHandler<ReqT, RespT>
      implements UnaryServerCallHandler<ReqT, RespT> {

    private final RequestStream requestStream;

    public <ReqT> RequestStreamCallHandler(RequestStream<ReqT, RespT> method) {
      this.requestStream = method;
    }

    @Override
    public CorePublisher<RespT> startCall(ReqT request, ByteBuf meta) {
      return requestStream.apply(request, meta);
    }
  }

  private static class RequestChannelCallHandler<ReqT, RespT>
      implements BiDirectServerCallHandler<ReqT, RespT> {

    private final RequestChannel requestChannel;

    public <ReqT> RequestChannelCallHandler(RequestChannel<ReqT, RespT> method) {
      this.requestChannel = method;
    }

    @Override
    public CorePublisher<RespT> startCall(Publisher<ReqT> requests, ByteBuf meta) {
      return requestChannel.apply(requests, meta);
    }
  }

  // Following default GRPC methods part of the stub generation and we overwrite
  public static MethodType asyncUnaryCall() {
    return MethodType.UNARY;
  }

  public static MethodType asyncServerStreamingCall() {
    return MethodType.SERVER_STREAMING;
  }

  public static MethodType asyncClientStreamingCall() {
    return MethodType.CLIENT_STREAMING;
  }

  public static MethodType asyncBidiStreamingCall() {
    return MethodType.BIDI_STREAMING;
  }
}
