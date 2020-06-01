package io.rsocket.rpc.core.extension.routing;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.netty.buffer.ByteBuf;
import reactor.core.CorePublisher;

public interface UnaryServerCallHandler<ReqT,RespT> extends ServerCallHandler<ReqT,RespT> {

  CorePublisher<RespT> startCall(ReqT request, ByteBuf meta);

  //This method is not needed
  @Override
  default Listener<ReqT> startCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata) {
    return null;
  }

}
