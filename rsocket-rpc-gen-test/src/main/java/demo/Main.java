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
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.rsocket.rpc.core.extension.RpcClient;
import io.rsocket.rpc.core.extension.RpcServer;
import io.rsocket.rpc.core.extension.RsocketClientBuilder;
import io.rsocket.rpc.core.extension.RsocketServerBuilder;
import io.rsocket.rpc.core.extension.tracing.RSocketTracing;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

// TODO: Move this class to tests
public class Main {

  public static final int PORT = 9099;
  private static Logger logger = LoggerFactory.getLogger(Main.class);
  private static ThreadLocalCurrentTraceContext currentTraceContext = null;

  public static void main(String[] args) throws InterruptedException {
    logger.info("starting the sample app");
    //    callRequestResponseWithTracing();
//    callRequestResponseWithMetrics();
                callRequestResponseWithTracingAndMetrics();
    //    callRequestResponseWithTracingAndZipkin();

    //    callFireAndForgetWithTracingAndMetrics();

    //    callRequestStreamWithTracingAndMetrics();

    // Only for Zipkin test
    // Thread.currentThread().join();

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
            .withTracing(tracing)
            //            .interceptor(RSocketTracing.create(tracing).newServerInterceptor())
            .build()
            .start();

    logger.info("Starting server in port 9090");
    server.awaitTermination();

    SimpleMeterRegistry simpleClientMeterRegistry = new SimpleMeterRegistry();

    RsocketClientBuilder clientBuilder =
        RsocketClientBuilder.forAddress("localhost", 9090)
            .withTracing(tracing)
            .withMetrics(simpleClientMeterRegistry);
    //            .interceptor(RSocketTracing.create(tracing).newClientInterceptor());

    Span span = tracer.newTrace().name("encode2").start();
    try (CurrentTraceContext.Scope scope =
        tracing.currentTraceContext().maybeScope(span.context())) {
      RsocketGreeterStub rsocketGreeterStub = RsocketGreeterRpc.newReactorStub(clientBuilder);
      rsocketGreeterStub
          .greet(HelloRequest.newBuilder().setName("hello").build())
          .doOnCancel(() -> logger.info("upstream is cancelled"))
          .doOnError(er -> logger.error("error received ", er))
          .doOnNext(r -> logger.info("RequestResponse:response received {}", r.getMessage()))
          .block();
    } finally {
      span.finish();
      server.shutDown();
      printClientMetrics(simpleClientMeterRegistry, "request.greet");
      printServerMetrics(simpleServerMeterRegistry, "request.greet");
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

  private static void printServerMetrics(
      SimpleMeterRegistry simpleClientMeterRegistry, String name) {
    Search search = simpleClientMeterRegistry.find(name);
    Collection<Meter> meters = search.meters();
    if (meters != null) {
      meters.forEach(
          meter -> {
            logger.info("Server meter id {} result {}", meter.getId(), meter.measure());
          });
    } else {
      logger.info("Server meter is null {}", name);
    }
  }

  private static void printClientMetrics(
      SimpleMeterRegistry simpleClientMeterRegistry, String name) {
    Search search = simpleClientMeterRegistry.find(name);
    Collection<Meter> meters = search.meters();
    if (meters != null) {
      meters.forEach(
          meter -> {
            logger.info("client meter id {} result {}", meter.getId(), meter.measure());
          });
    } else {
      logger.info("Client meter is null {}", name);
    }
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
        RsocketServerBuilder.forPort(PORT)
            .withMetrics(simpleServerMeterRegistry)
            .addService(new GreeterImpl())
            .build()
            .start();
    logger.info("server started in port {}", PORT);
    server.awaitTermination();

    SimpleMeterRegistry simpleClientMeterRegistry = new SimpleMeterRegistry();

    RsocketClientBuilder rsocketClientBuilder =
        RsocketClientBuilder.forAddress("localhost", PORT)
            //            .withLoadBalancing()
            .withMetrics(simpleClientMeterRegistry);

    RsocketGreeterStub rsocketGreeterStub = RsocketGreeterRpc.newReactorStub(rsocketClientBuilder);

    try {
      rsocketGreeterStub
          .greet(HelloRequest.newBuilder().setName("hello").build())
          .doOnNext(r -> logger.info("RequestResponse:response received {}", r.getMessage()))
          .block();
    } finally {
      // printClientMetrics(simpleClientMeterRegistry, "request.greet");
      // printServerMetrics(simpleServerMeterRegistry, "request.greet");
//      printClientMetrics(simpleClientMeterRegistry);
//      printServerMetrics(simpleServerMeterRegistry);
      validateMetrics(simpleClientMeterRegistry);
      validateMetrics(simpleServerMeterRegistry);
      server.shutDown();
    }
  }

  // This function is validating if same metrics-ids have same tags
  private static void validateMetrics(SimpleMeterRegistry simpleClientMeterRegistry) {
    Map<String, List<Tag>> meterMap = new HashMap<>();
    simpleClientMeterRegistry.forEachMeter(
        meter -> {
          logger.info("validating the tag {}", meter.getId().getName());
          if (meterMap.containsKey(meter.getId().getName())) {
            // compare tags
            logger.info("already existing meter id");
            if (!(meter.getId().getTags().size() == meterMap.get(meter.getId().getName()).size())) {
              logger.warn("**meter tag sizes are not same for meter {}", meter.getId());
              throw new RuntimeException("two same metrics size not same");
            }
            if (!meter.getId().getTags().stream()
                .allMatch(
                    tag ->
                        meterMap.get(meter.getId().getName()).stream()
                            .anyMatch(tagElement -> {
                              logger.info("checking tag {} from registry with in map {} ",tag.getKey(),tagElement.getKey());
                              return tagElement.getKey().equals(tag.getKey());
                            }))) {
              logger.warn("**meter tags are not identical meter {}", meter.getId());
              throw new RuntimeException("two same metrics tags not identical");
            }

          } else {
            // add it to
            logger.info("this meter id is new adding to list");
            meterMap.put(meter.getId().getName(), meter.getId().getTags());
          }
        });
  }

  private static void callRequestResponseWithTracing() throws InterruptedException {
    String localServiceName = "MAIN";
    Tracing mainTracing = createTracing(localServiceName);
    Tracing serverTracing = createTracing("server");
    Tracer tracer = mainTracing.tracer();
    //    Span span = tracer.newTrace().name("server-start").start();
    RpcServer server =
        RsocketServerBuilder.forPort(9090)
            .addService(new GreeterImpl())
            .withTracing(serverTracing)
            .build()
            .start();

    logger.info("server started in port 9090");
    server.awaitTermination();

    Span mainSpan = tracer.nextSpan().name("main");
    try (Tracer.SpanInScope ws = tracer.withSpanInScope(mainSpan.start())) {
      logger.info("main span is started,setting the client first");
      RpcClient simpleClient =
          RsocketClientBuilder.forAddress("localhost", 9090).withTracing(mainTracing).build();
      RsocketGreeterStub rsocketGreeterStub = RsocketGreeterRpc.newReactorStub(simpleClient);
      logger.info("calling the remote service");
      rsocketGreeterStub
          .greet(HelloRequest.newBuilder().setName("hello").build())
          .doOnNext(r -> logger.info("RequestResponse:response received {}", r.getMessage()))
          .block();
    } finally {
      mainSpan.finish();
      server.shutDown();
    }
  }

  private static Tracing createTracing(String localServiceName) {
    return Tracing.newBuilder()
        .currentTraceContext(createTracingContext())
        .localServiceName(localServiceName)
        .build();
  }

  private static ThreadLocalCurrentTraceContext createTracingContext() {
    if (currentTraceContext == null) {
      currentTraceContext =
          ThreadLocalCurrentTraceContext.newBuilder()
              .addScopeDecorator(MDCScopeDecorator.get())
              .build();
    }
    return currentTraceContext;
  }

  // with zipkin
  private static void callRequestResponseWithTracingAndZipkin() throws InterruptedException {
    // First let's initialize the zipkin endpoint
    Sender okHttpSender = OkHttpSender.create("http://127.0.0.1:9411/api/v2/spans");

    Tracing serverTracing =
        Tracing.newBuilder()
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.get())
                    .build())
            .localServiceName("test-service")
            .addSpanHandler(AsyncZipkinSpanHandler.create(okHttpSender))
            .build();
    //    Span span = tracer.newTrace().name("server-start").start();
    RpcServer server =
        RsocketServerBuilder.forPort(9090)
            .addService(new GreeterImpl())
            .interceptor(RSocketTracing.create(serverTracing).newServerInterceptor())
            .build()
            .start();

    logger.info("server started in port 9090");
    server.awaitTermination();

    Tracing clientTracing =
        Tracing.newBuilder()
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.get())
                    .build())
            .localServiceName("test-service-client")
            .addSpanHandler(AsyncZipkinSpanHandler.create(okHttpSender))
            .build();

    RpcClient simpleClient =
        RsocketClientBuilder.forAddress("localhost", 9090)
            .interceptor(RSocketTracing.create(clientTracing).newClientInterceptor())
            .build();

    Span span = clientTracing.tracer().newTrace().name("client-tracer").start();

    try (CurrentTraceContext.Scope scope =
        serverTracing.currentTraceContext().maybeScope(span.context())) {
      RsocketGreeterStub rsocketGreeterStub = RsocketGreeterRpc.newReactorStub(simpleClient);
      Flux.range(1, 100)
          .flatMap(
              i ->
                  rsocketGreeterStub
                      .greet(HelloRequest.newBuilder().setName("hello-" + i).build())
                      .doOnNext(
                          r -> logger.info("RequestResponse:response received {}", r.getMessage())))
          .blockLast();
    } finally {
      span.finish();
      server.shutDown();
    }
  }
}
