package io.rsocket.rpc.core.extension.tracing;

import brave.Tracer;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import brave.rpc.RpcRequest;
import brave.rpc.RpcTracing;
import brave.sampler.SamplerFunction;
import java.util.Map;

public class TracingContext {
  final Tracer tracer;
  final CurrentTraceContext currentTraceContext;
  final SamplerFunction<RpcRequest> sampler;
  public final Extractor<Map<String, String>> mapExtractor;
  public String service;
  public Injector<Map<String, String>> mapInjector;

  public TracingContext(RpcTracing rpcTracing, String serviceName) {
    tracer = rpcTracing.tracing().tracer();
    mapInjector = rpcTracing.tracing().propagation().injector(Map<String, String>::put);
    mapExtractor = rpcTracing.tracing().propagation().extractor(Map<String, String>::get);
    currentTraceContext = rpcTracing.tracing().currentTraceContext();
    sampler = rpcTracing.clientSampler();
    service = serviceName;
  }
}
