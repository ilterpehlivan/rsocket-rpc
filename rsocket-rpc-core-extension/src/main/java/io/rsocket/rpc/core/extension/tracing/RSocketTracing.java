package io.rsocket.rpc.core.extension.tracing;

import brave.Tracing;
import brave.rpc.RpcClientHandler;
import brave.rpc.RpcTracing;
import io.rsocket.plugins.RSocketInterceptor;

public class RSocketTracing {
  final RpcTracing rpcTracing;
  String serviceName;
  String methodName;

  public RSocketTracing(RpcTracing rpcTracing) {
    this.rpcTracing = rpcTracing;
  }

  public static RSocketTracing create(Tracing tracing) {
    return new RSocketTracing(RpcTracing.create(tracing));
  }

  public static RSocketTracing create(RpcTracing rpcTracing) {
    return new RSocketTracing(rpcTracing);
  }

  public RSocketTracing serviceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public RSocketTracing methodName(String methodName) {
    this.methodName = methodName;
    return this;
  }

  public RpcTracing getRpcTracing() {
    return rpcTracing;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getServiceName() {
    return serviceName;
  }

  /** This interceptor traces outbound calls */
  public final RSocketInterceptor newClientInterceptor() {
    return new ClientTracerInterceptor(this);
  }

  /** This interceptor traces inbound calls */
  public RSocketInterceptor newServerInterceptor() {
    return new ServerTracerInterceptor(this);
  }

}
