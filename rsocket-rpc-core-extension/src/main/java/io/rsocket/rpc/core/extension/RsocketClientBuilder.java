package io.rsocket.rpc.core.extension;

import brave.Tracing;
import io.micrometer.core.instrument.MeterRegistry;
import io.rsocket.RSocket;
import io.rsocket.client.LoadBalancedRSocketMono;
import io.rsocket.client.filter.RSocketSupplier;
import io.rsocket.core.RSocketConnector;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.plugins.RSocketInterceptor;
import io.rsocket.rpc.core.extension.micrometer.MicrometerRpcInterceptor;
import io.rsocket.rpc.core.extension.micrometer.RpcTag;
import io.rsocket.rpc.core.extension.tracing.RSocketTracing;
import io.rsocket.transport.netty.client.TcpClientTransport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
public class RsocketClientBuilder {

  private static final long DEFAULT_MAX_RETRY = 10;
  private static final long DEFAULT_MIN_BACK_OFF = 100;
  private String serviceAdress;
  private int servicePort;
  private boolean isLoadbalanced = false;
  private List<RSocketInterceptor> interceptorList;
  private MeterRegistry meterRegistry;
  private Map<String, String> methodMapping;
  private String serviceName;
  private boolean enableReconnect = false;
  private long maxRetryAttempts = DEFAULT_MAX_RETRY;
  private long minBackOffValue = DEFAULT_MIN_BACK_OFF;
  private Tracing tracing;

  private RsocketClientBuilder(String serviceUrl, int servicePort) {
    this.serviceAdress = serviceUrl;
    this.servicePort = servicePort;
  }

  public static RsocketClientBuilder forAddress(String serviceUrl, int servicePort) {
    return new RsocketClientBuilder(serviceUrl, servicePort);
  }

  /**
   * Depends on the issue https://github.com/rsocket/rsocket-java/issues/781 Until then no support
   * for loadbalanced monos
   *
   * @return
   */
  @Deprecated
  public RsocketClientBuilder withLoadBalancing() {
    // TODO: after issue is fixed above this needs to be revisited
    this.isLoadbalanced = false;
    return this;
  }

  public RsocketClientBuilder withTracing(Tracing tracing) {
    this.tracing = tracing;
    return this;
  }

  public RsocketClientBuilder withMetrics(MeterRegistry registry) {
    this.meterRegistry = registry;
    return this;
  }

  public RsocketClientBuilder withRetry(int retry, long minDuration) {
    this.maxRetryAttempts = retry;
    this.minBackOffValue = minDuration;
    this.enableReconnect = true;
    return this;
  }

  public RsocketClientBuilder interceptors(RSocketInterceptor... interceptors) {
    for (RSocketInterceptor interceptor : interceptors) {
      this.interceptor(interceptor);
    }
    return this;
  }

  public RsocketClientBuilder interceptor(RSocketInterceptor interceptor) {
    if (this.interceptorList == null) {
      this.interceptorList = new ArrayList<>();
    }
    this.interceptorList.add(interceptor);
    return this;
  }

  // Following methods are filled automatically inside the stub
  public RsocketClientBuilder serviceName(String service) {
    this.serviceName = service;
    return this;
  }

  public RsocketClientBuilder addMethods(Map<String, String> methods) {
    this.methodMapping = methods;
    return this;
  }

  public RpcClient build() {

    log.info("building client adress: {} port: {}", serviceAdress, servicePort);

    RSocketConnector rSocketConnector = RSocketConnector.create();
    // add interceptors
    if (interceptorList != null) {
      interceptorList.forEach(
          interceptor ->
              rSocketConnector.interceptors(registry -> registry.forRequester(interceptor)));
    }

    // TODO: how to distinguish explicit meterRegistry from interceptors ?
    if (meterRegistry != null) {
      rSocketConnector.interceptors(
          registry ->
              registry.forRequester(
                  new MicrometerRpcInterceptor(
                      meterRegistry, RpcTag.getClientTags(serviceName, methodMapping))));
    }

    if (tracing != null) {
      rSocketConnector.interceptors(
          interceptorRegistry ->
              interceptorRegistry.forRequester(
                  RSocketTracing.create(tracing).newClientInterceptor()));
    }

    // reconnect strategy
    if (!isLoadbalanced && enableReconnect) {
      rSocketConnector.reconnect(
          Retry.backoff(maxRetryAttempts, Duration.ofMillis(minBackOffValue))
              .doBeforeRetry(rs -> log.warn("Retrying to connect, failed with signal {}", rs)));
    }

    // It is needed for Loadbalanced case as it takes time
    CountDownLatch rsocketInit = new CountDownLatch(1);
    Mono<RSocket> rSocketMono =
        rSocketConnector
            .payloadDecoder(PayloadDecoder.ZERO_COPY)
            .connect(TcpClientTransport.create(serviceAdress, servicePort))
            .doOnError(
                e ->
                    // TODO:add error counter from micrometer to count the connection errors as
                    // metrics rpc.connection.counter
                    log.error(
                        "Error received while connecting {} {}",
                        getServiceAdress(),
                        e.getMessage()))
            .doOnSuccess(
                (reactSocket) -> {
                  if (log.isDebugEnabled()) {
                    log.info("connected to {} successfully", getServiceAdress());
                  }
                  rsocketInit.countDown();
                })
            .doOnSubscribe(s -> log.info("trying to connect service {} ", getServiceAdress()));

    log.info("got a socket");

    if (isLoadbalanced) {
      LoadBalancedRSocketMono loadBalancedRSocketMono =
          LoadBalancedRSocketMono.create(
              Flux.just(Collections.singleton(new RSocketSupplier(() -> rSocketMono))),
              LoadBalancedRSocketMono.DEFAULT_EXP_FACTOR,
              LoadBalancedRSocketMono.DEFAULT_LOWER_QUANTILE,
              LoadBalancedRSocketMono.DEFAULT_HIGHER_QUANTILE,
              LoadBalancedRSocketMono.DEFAULT_MIN_PENDING,
              LoadBalancedRSocketMono.DEFAULT_MAX_PENDING,
              LoadBalancedRSocketMono.DEFAULT_MIN_APERTURE,
              LoadBalancedRSocketMono.DEFAULT_MAX_APERTURE,
              LoadBalancedRSocketMono.DEFAULT_MAX_REFRESH_PERIOD_MS,
              maxRetryAttempts,
              // TODO: make them configurable as well
              Duration.ofMillis(300),
              Duration.ofSeconds(5));
      RpcClient rpcClient = new RpcClient(loadBalancedRSocketMono, rsocketInit);
      return rpcClient;
    } else {
      RpcClient rpcClient = new RpcClient(rSocketMono);
      return rpcClient;
    }
  }

  private String getServiceAdress() {
    return serviceAdress + ":" + servicePort;
  }
}
