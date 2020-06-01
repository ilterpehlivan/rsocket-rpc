package io.rsocket.rpc.core.extension.routing;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.CorePublisher;

public interface BiDirectServerCallHandler<ReqT, RespT> extends ServerCallHandler<ReqT, RespT> {

  CorePublisher<RespT> startCall(Publisher<ReqT> requests, ByteBuf meta);

  // This method is not needed
  @Override
  default Listener<ReqT> startCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata) {
    return null;
  }
}
