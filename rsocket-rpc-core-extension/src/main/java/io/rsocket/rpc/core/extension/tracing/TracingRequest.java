package io.rsocket.rpc.core.extension.tracing;

import brave.Span;
import brave.rpc.RpcClientRequest;

public class TracingRequest extends RpcClientRequest {

  final private String serviceName;
  final private String methodName;
  Span span;

  public TracingRequest(String serviceName, String methodName) {
    this.serviceName = serviceName;
    this.methodName = methodName;
  }

  public TracingRequest(String serviceName, String methodName, Span span) {
    this.serviceName = serviceName;
    this.methodName = methodName;
    this.span = span;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getMethodName() {
    return methodName;
  }

  public Span getSpan() {
    return span;
  }

  public void setSpan(final Span span) {
    this.span = span;
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
