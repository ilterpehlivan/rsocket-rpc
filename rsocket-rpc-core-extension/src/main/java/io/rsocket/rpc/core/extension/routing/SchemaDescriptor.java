package io.rsocket.rpc.core.extension.routing;

import io.grpc.MethodDescriptor.MethodType;
import io.netty.util.internal.StringUtil;
import java.util.Arrays;

public class SchemaDescriptor {
  private final String service;
  private final String method;
  private String rpcMethodName;

  public static final String RPC_METHOD_KEY = "rpc.method";

  public SchemaDescriptor(
      String service, String method, MethodType grpcMethodType, String rpcMethodName) {
    this.service = service;
    this.method = method;
    this.rpcMethodName = rpcMethodName;
  }

  public static String getRpcMethod(String method) {
    return RPC_METHOD_KEY + "." + RsocketMethodType.getMethod(method).getName();
  }

  public static RsocketMethodType getRsocketTypeFromRpcMethod(String rpcMethodName) {
    if (StringUtil.isNullOrEmpty(rpcMethodName)) {
      return RsocketMethodType.NOT_SUPPORTED;
    }
    String rsocketMethod = rpcMethodName.substring(rpcMethodName.lastIndexOf(".") + 1);
    return RsocketMethodType.getMethod(rsocketMethod);
  }

  public String getService() {
    return service;
  }

  public String getMethod() {
    return method;
  }

  public String getRpcMethodName() {
    return rpcMethodName;
  }

  public enum RsocketMethodType {
    REQUEST_REPLY("requestReply"),
    FIRE_FORGET("fireAndForget"),
    REQUEST_STREAM("requestStream"),
    REQUEST_CHANNEL("streamingRequestAndResponse"),
    STREAM_REQUEST_SINGLE_RESPONSE("streamingRequestSingleResponse"),
    NOT_SUPPORTED("No Support");

    private final String name;

    RsocketMethodType(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public static RsocketMethodType getMethod(String reactiveMethodName) {
      return Arrays.stream(RsocketMethodType.values())
          .filter(m -> m.name.equalsIgnoreCase(reactiveMethodName))
          .findFirst()
          .orElse(NOT_SUPPORTED);
    }

  }
}
