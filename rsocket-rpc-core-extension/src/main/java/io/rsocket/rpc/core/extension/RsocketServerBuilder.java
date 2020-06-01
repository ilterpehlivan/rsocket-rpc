package io.rsocket.rpc.core.extension;

import com.google.common.base.Preconditions;
import io.grpc.BindableService;
import io.grpc.ServerMethodDefinition;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.rsocket.RSocketFactory;
import io.rsocket.RSocketFactory.ServerRSocketFactory;
import io.rsocket.RSocketFactory.Start;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.plugins.RSocketInterceptor;
import io.rsocket.rpc.RSocketRpcService;
import io.rsocket.rpc.core.extension.micrometer.MicrometerRpcInterceptor;
import io.rsocket.rpc.core.extension.micrometer.RpcTag;
import io.rsocket.rpc.core.extension.routing.RoutingServerRSocket;
import io.rsocket.rpc.core.extension.routing.SchemaDescriptor;
import io.rsocket.rpc.core.extension.routing.ServiceHandlerRegistry;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

public final class RsocketServerBuilder {
  int port;
  RSocketRpcService service;
  List<RSocketInterceptor> interceptorList;
  ServiceHandlerRegistry.Builder registryBuilder = new ServiceHandlerRegistry.Builder();
  private MeterRegistry meterRegistry;

  private RsocketServerBuilder(int port) {
    this.port = port;
  }

  public static RsocketServerBuilder forPort(int port) {
    return new RsocketServerBuilder(port);
  }

  @Deprecated
  public RsocketServerBuilder withService(RSocketRpcService service) {
    this.service = service;
    return this;
  }

  public RsocketServerBuilder addService(BindableService service) {
    this.registryBuilder.addService(service.bindService());
    return this;
  }

  public RsocketServerBuilder interceptors(RSocketInterceptor... interceptors) {
    for (RSocketInterceptor interceptor : interceptors) {
      this.interceptor(interceptor);
    }

    return this;
  }

  public RsocketServerBuilder interceptor(RSocketInterceptor interceptor) {
    if (this.interceptorList == null) {
      this.interceptorList = new ArrayList<>();
    }
    this.interceptorList.add(interceptor);
    return this;
  }

  public final RpcServer build() {
    ServerRSocketFactory serverRSocketFactory = RSocketFactory.receive();
    if (interceptorList != null) {
      interceptorList.forEach(serverRSocketFactory::addResponderPlugin);
    }

    ServiceHandlerRegistry serviceHandlerRegistry = registryBuilder.build();

    if (meterRegistry != null) {
      serverRSocketFactory.addResponderPlugin(
          new MicrometerRpcInterceptor(
              meterRegistry,
              RpcTag.getServerTags(
                  getServiceName(serviceHandlerRegistry), getMethodsMap(serviceHandlerRegistry))));
    }

    RoutingServerRSocket routingServerRSocket =
        new RoutingServerRSocket(
            Preconditions.checkNotNull(serviceHandlerRegistry, "registryBuilder cannot be null"));

    Start<CloseableChannel> transport =
        serverRSocketFactory
            // TODO: frame may be also part of builder
            .frameDecoder(PayloadDecoder.ZERO_COPY)
            .acceptor(
                (setupPayload, reactiveSocket) -> {
                  return Mono.just(routingServerRSocket);
                })
            .transport(TcpServerTransport.create(port));
    return new RpcServer(transport, routingServerRSocket);
  }

  private Map<String, String> getMethodsMap(ServiceHandlerRegistry serviceHandlerRegistry) {
    Collection<ServerMethodDefinition<?, ?>> methods =
        serviceHandlerRegistry.getServices().get(0).getMethods();
    Map<String, String> methodMapping = new HashMap<>(methods.size());
    methods.forEach(
        method -> {
          String rpcMethodName =
              ((SchemaDescriptor) method.getMethodDescriptor().getSchemaDescriptor())
                  .getRpcMethodName();
          methodMapping.put(
              rpcMethodName,
              ((SchemaDescriptor) method.getMethodDescriptor().getSchemaDescriptor()).getMethod());
        });
    return methodMapping;
  }

  private String getServiceName(ServiceHandlerRegistry serviceHandlerRegistry) {
    return serviceHandlerRegistry.getServices().get(0).getServiceDescriptor().getName();
  }

  public RsocketServerBuilder withMetrics(SimpleMeterRegistry serverMeterRegistry) {
    this.meterRegistry = serverMeterRegistry;
    return this;
  }
}
