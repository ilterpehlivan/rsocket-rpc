package demo;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import demo.proto.HelloRequest;
import demo.proto.RsocketGreeterRpc;
import demo.proto.RsocketGreeterRpc.RsocketGreeterStub;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.rsocket.rpc.core.extension.RpcClient;
import io.rsocket.rpc.core.extension.RpcServer;
import io.rsocket.rpc.core.extension.RsocketClientBuilder;
import io.rsocket.rpc.core.extension.RsocketServerBuilder;
import io.rsocket.rpc.core.extension.tracing.RSocketTracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws InterruptedException {
    logger.info("starting the sample app");
//    callRequestResponseWithTracing();
    // callRequestResponseWithMetrics();
    //callRequestResponseWithTracingAndMetrics();
//    callFireAndForgetWithTracingAndMetrics();

    callRequestStreamWithTracingAndMetrics();

    //    CountDownLatch latch = new CountDownLatch(9);
    //    rsocketGreeterStub
    //        .multiGreet(HelloRequest.newBuilder().setName("hello").build())
    //        .subscribe(
    //            consumer -> System.out.println("client subscribe response " +
    // consumer.getMessage()),
    //            er -> System.out.println("client subscribe error " + er.getMessage()));

    //    latch.await();
    logger.info("**End of main***");
  }

  private static void callRequestStreamWithTracingAndMetrics() throws InterruptedException {
    logger.info("****starting the RequestStream test****");
    Tracing tracing =
        Tracing.newBuilder()
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.create())
                    .build())
            .localServiceName("test-service")
            .build();
    Tracer tracer = tracing.tracer();
    SimpleMeterRegistry simpleServerMeterRegistry = new SimpleMeterRegistry();
    RpcServer server =
        RsocketServerBuilder.forPort(9090)
            .addService(new GreeterImpl())
            .withMetrics(simpleServerMeterRegistry)
            .interceptor(RSocketTracing.create(tracing).newServerInterceptor())
            .build()
            .start();

    logger.info("Starting server in port 9090");
    server.awaitTermination();

    SimpleMeterRegistry simpleClientMeterRegistry = new SimpleMeterRegistry();

    RsocketClientBuilder clientBuilder =
        RsocketClientBuilder.forAddress("localhost", 9090)
            .withLoadBalancing()
            .withMetrics(simpleClientMeterRegistry)
            .interceptor(RSocketTracing.create(tracing).newClientInterceptor());

    Span span = tracer.newTrace().name("encode2").start();
    try (CurrentTraceContext.Scope scope =
        tracing.currentTraceContext().maybeScope(span.context())) {
      RsocketGreeterStub rsocketGreeterStub = RsocketGreeterRpc.newReactorStub(clientBuilder);
      rsocketGreeterStub
          .multiGreet(HelloRequest.newBuilder().setName("hello").build())
          .doOnNext(r -> logger.info("Request Stream:response received {}", r.getMessage()))
          .blockLast();
    } finally {
      span.finish();
      server.shutDown();
      printClientMetrics(simpleClientMeterRegistry);
      printServerMetrics(simpleServerMeterRegistry);
    }
  }

  private static void callRequestResponseWithTracingAndMetrics() throws InterruptedException {
    Tracing tracing =
        Tracing.newBuilder()
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.create())
                    .build())
            .localServiceName("test-service")
            .build();
    Tracer tracer = tracing.tracer();
    SimpleMeterRegistry simpleServerMeterRegistry = new SimpleMeterRegistry();
    RpcServer server =
        RsocketServerBuilder.forPort(9090)
            .addService(new GreeterImpl())
            .withMetrics(simpleServerMeterRegistry)
            .interceptor(RSocketTracing.create(tracing).newServerInterceptor())
            .build()
            .start();

    logger.info("Starting server in port 9090");
    server.awaitTermination();

    SimpleMeterRegistry simpleClientMeterRegistry = new SimpleMeterRegistry();

    RsocketClientBuilder clientBuilder =
        RsocketClientBuilder.forAddress("localhost", 9090)
            .withLoadBalancing()
            .withMetrics(simpleClientMeterRegistry)
            .interceptor(RSocketTracing.create(tracing).newClientInterceptor());

    Span span = tracer.newTrace().name("encode2").start();
    try (CurrentTraceContext.Scope scope =
        tracing.currentTraceContext().maybeScope(span.context())) {
      RsocketGreeterStub rsocketGreeterStub = RsocketGreeterRpc.newReactorStub(clientBuilder);
      rsocketGreeterStub
          .greet(HelloRequest.newBuilder().setName("hello").build())
          .doOnNext(r -> logger.info("RequestResponse:response received {}", r.getMessage()))
          .block();
    } finally {
      span.finish();
      server.shutDown();
      printClientMetrics(simpleClientMeterRegistry);
      printServerMetrics(simpleServerMeterRegistry);
    }
  }

  private static void callFireAndForgetWithTracingAndMetrics() throws InterruptedException {
    logger.info("****starting the fireAndForget call****");
    Tracing tracing =
        Tracing.newBuilder()
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.create())
                    .build())
            .localServiceName("test-service")
            .build();
    Tracer tracer = tracing.tracer();
    SimpleMeterRegistry simpleServerMeterRegistry = new SimpleMeterRegistry();
    RpcServer server =
        RsocketServerBuilder.forPort(9090)
            .addService(new GreeterImpl())
            .withMetrics(simpleServerMeterRegistry)
            .interceptor(RSocketTracing.create(tracing).newServerInterceptor())
            .build()
            .start();

    logger.info("Starting server in port 9090");
    server.awaitTermination();

    SimpleMeterRegistry simpleClientMeterRegistry = new SimpleMeterRegistry();

    RsocketClientBuilder clientBuilder =
        RsocketClientBuilder.forAddress("localhost", 9090)
            .withLoadBalancing()
            .withMetrics(simpleClientMeterRegistry)
            .interceptor(RSocketTracing.create(tracing).newClientInterceptor());

    Span span = tracer.newTrace().name("encode2").start();
    try (CurrentTraceContext.Scope scope =
        tracing.currentTraceContext().maybeScope(span.context())) {
      RsocketGreeterStub rsocketGreeterStub = RsocketGreeterRpc.newReactorStub(clientBuilder);
      rsocketGreeterStub
          .greetAndForget(HelloRequest.newBuilder().setName("hello").build())
          .doOnNext(r -> logger.info("FireAndForget:response received"))
          .block();
    } finally {
      span.finish();
      server.shutDown();
      printClientMetrics(simpleClientMeterRegistry);
      printServerMetrics(simpleServerMeterRegistry);
    }
  }

  private static void printServerMetrics(SimpleMeterRegistry simpleServerMeterRegistry) {
    simpleServerMeterRegistry.forEachMeter(
        meter -> {
          logger.info("server meter id {} result {}", meter.getId(), meter.measure());
        });
  }

  private static void printClientMetrics(SimpleMeterRegistry simpleClientMeterRegistry) {
    simpleClientMeterRegistry.forEachMeter(
        meter -> {
          logger.info("client meter id {} result {}", meter.getId(), meter.measure());
        });
  }

  private static void callRequestResponseWithMetrics() throws InterruptedException {
    logger.info("Starting the callRequestResponseWithMetrics");
    SimpleMeterRegistry simpleServerMeterRegistry = new SimpleMeterRegistry();
    RpcServer server =
        RsocketServerBuilder.forPort(9091)
            .withMetrics(simpleServerMeterRegistry)
            .addService(new GreeterImpl())
            .build()
            .start();
    logger.info("server started in port 9091");
    server.awaitTermination();

    SimpleMeterRegistry simpleClientMeterRegistry = new SimpleMeterRegistry();

    RsocketClientBuilder rsocketClientBuilder =
        RsocketClientBuilder.forAddress("localhost", 9091)
            .withLoadBalancing()
            .withMetrics(simpleClientMeterRegistry);

    RsocketGreeterStub rsocketGreeterStub = RsocketGreeterRpc.newReactorStub(rsocketClientBuilder);

    try {
      rsocketGreeterStub
          .greet(HelloRequest.newBuilder().setName("hello").build())
          .doOnNext(r -> logger.info("RequestResponse:response received {}", r.getMessage()))
          .block();
    } finally {
      simpleClientMeterRegistry.forEachMeter(
          meter -> {
            logger.info("client meter id {} result {}", meter.getId(), meter.measure());
          });

      simpleServerMeterRegistry.forEachMeter(
          meter -> {
            logger.info("server meter id {} result {}", meter.getId(), meter.measure());
          });
      server.shutDown();
    }
  }

  private static void callRequestResponseWithTracing() throws InterruptedException {
    Tracing tracing =
        Tracing.newBuilder()
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.create())
                    .build())
            .localServiceName("test-service")
            .build();
    Tracer tracer = tracing.tracer();
    //    Span span = tracer.newTrace().name("server-start").start();
    RpcServer server =
        RsocketServerBuilder.forPort(9090)
            .addService(new GreeterImpl())
            .interceptor(RSocketTracing.create(tracing).newServerInterceptor())
            .build()
            .start();

    logger.info("server started in port 9090");
    server.awaitTermination();

    RpcClient simpleClient =
        RsocketClientBuilder.forAddress("localhost", 9090)
            .withLoadBalancing()
            .interceptor(RSocketTracing.create(tracing).newClientInterceptor())
            .build();

    Span span = tracer.newTrace().name("encode2").start();
    try (CurrentTraceContext.Scope scope =
        tracing.currentTraceContext().maybeScope(span.context())) {
      RsocketGreeterStub rsocketGreeterStub = RsocketGreeterRpc.newReactorStub(simpleClient);
      rsocketGreeterStub
          .greet(HelloRequest.newBuilder().setName("hello").build())
          .doOnNext(r -> logger.info("RequestResponse:response received {}", r.getMessage()))
          .block();
    } finally {
      span.finish();
      server.shutDown();
    }
  }
}
