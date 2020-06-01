package io.rsocket.rpc.extension.sample;

import io.rsocket.rpc.RSocketRpcService;

/**
 */
@javax.annotation.Generated(
    value = "by RSocket RPC proto compiler",
    comments = "Source: service.proto")
public interface SimpleService  {
  String SERVICE = "io.rsocket.rpc.extension.sample.SimpleService";
  String METHOD_REQUEST_REPLY = "RequestReply";
  String METHOD_FIRE_AND_FORGET = "FireAndForget";
  String METHOD_REQUEST_STREAM = "RequestStream";
  String METHOD_STREAMING_REQUEST_SINGLE_RESPONSE = "StreamingRequestSingleResponse";
  String METHOD_STREAMING_REQUEST_AND_RESPONSE = "StreamingRequestAndResponse";

  /**
   * <pre>
   * Request / Response
   * </pre>
   */
  reactor.core.publisher.Mono<io.rsocket.rpc.extension.sample.proto.SimpleResponse> requestReply(
      io.rsocket.rpc.extension.sample.proto.SimpleRequest message, io.netty.buffer.ByteBuf metadata);

  /**
   * <pre>
   * Fire-and-Forget
   * </pre>
   */
  reactor.core.publisher.Mono<com.google.protobuf.Empty> fireAndForget(
      io.rsocket.rpc.extension.sample.proto.SimpleRequest message, io.netty.buffer.ByteBuf metadata);

  /**
   * <pre>
   * Single Request / Streaming Response
   * </pre>
   */
  reactor.core.publisher.Flux<io.rsocket.rpc.extension.sample.proto.SimpleResponse> requestStream(
      io.rsocket.rpc.extension.sample.proto.SimpleRequest message, io.netty.buffer.ByteBuf metadata);

  /**
   * <pre>
   * Streaming Request / Single Response
   * </pre>
   */
  reactor.core.publisher.Mono<io.rsocket.rpc.extension.sample.proto.SimpleResponse> streamingRequestSingleResponse(
      org.reactivestreams.Publisher<io.rsocket.rpc.extension.sample.proto.SimpleRequest> messages,
      io.netty.buffer.ByteBuf metadata);

  /**
   * <pre>
   * Streaming Request / Streaming Response
   * </pre>
   */
  reactor.core.publisher.Flux<io.rsocket.rpc.extension.sample.proto.SimpleResponse> streamingRequestAndResponse(
      org.reactivestreams.Publisher<io.rsocket.rpc.extension.sample.proto.SimpleRequest> messages,
      io.netty.buffer.ByteBuf metadata);
}
