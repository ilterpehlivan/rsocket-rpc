package io.rsocket.rpc.core.extension.tracing;

import brave.rpc.RpcClientRequest;

public class TracingRequest extends RpcClientRequest {

  final String serviceName;
  final String methodName;

  public TracingRequest(String serviceName, String methodName) {
    this.serviceName = serviceName;
    this.methodName = methodName;
  }


  @Override
  public String method() {
    return methodName;
  }

  @Override
  public String service() {
    return serviceName;
  }

  @Override
  public Object unwrap() {
    return this;
  }
}
