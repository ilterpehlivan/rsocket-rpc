package demo;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.B3Propagation;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ExtraFieldPropagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import brave.propagation.TraceContextOrSamplingFlags;
import io.rsocket.Payload;
import io.rsocket.rpc.core.extension.tracing.TracingUtil;
import java.util.HashMap;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class TraceDemo {

  private static Logger logger = LoggerFactory.getLogger(TraceDemo.class);

  public static Tracing initTracing() {

    //    AsyncReporter asyncReporter = AsyncReporter.builder(sender)
    //        .closeTimeout(500, TimeUnit.MILLISECONDS)
    //        .build(SpanBytesEncoder.JSON_V2);

    Tracing tracing =
        Tracing.newBuilder()
            .localServiceName("tracer-demo")
            .propagationFactory(
                ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "user-name"))
            // .currentTraceContext(ThreadContextCurrentTraceContext.create())
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.create()) // puts trace IDs into logs
                    .build())
            .build();

    return tracing;
  }

  public static void main(String[] args) {
    Tracing tracing = initTracing();
    //    testTraceNormal(tracing);
    //testTraceReactive(tracing);
    testCorePublisherReactive(tracing);
    //    testTraceTwoPhase(tracing);
    // testTraceTwoPhase2(tracing);
    sleep(1000);
  }

  private static void testCorePublisherReactive(Tracing tracing) {
    Tracer tracer = tracing.tracer();
    Injector<Map<String, String>> mapInjector =
        tracing.propagation().injector(Map<String, String>::put);
    Span span = tracer.newTrace().name("encode2").start();
    try (CurrentTraceContext.Scope scope =
        tracing.currentTraceContext().maybeScope(span.context())) {
      logger.info("Inside the reactive testing");
      Map<String, String> map = new HashMap<>();
      mapInjector.inject(span.context(), map);
      Publisher<String> publisher = new Publisher<String>() {
        @Override
        public void subscribe(Subscriber<? super String> s) {
          s.onNext("test");
        }
      };
      Mono.from(publisher)
          .doOnNext(s -> logger.info("inside the doOnNext {}", s))
          .flatMap(s -> callReactiveFunction(map, tracing).subscribeOn(Schedulers.parallel()))
          .transform(TracingUtil.<String>scopePassingSpanOperator(
              tracing.currentTraceContext(), span.context()))
          .subscribe(s -> logger.info("inside the subscribe {}", s));
      doSomethingExpensive();
    } finally {
      span.finish();
    }
  }

  private static void testTraceReactive(Tracing tracing) {
    Tracer tracer = tracing.tracer();
    Injector<Map<String, String>> mapInjector =
        tracing.propagation().injector(Map<String, String>::put);
    Span span = tracer.newTrace().name("encode2").start();
    try (CurrentTraceContext.Scope scope =
        tracing.currentTraceContext().maybeScope(span.context())) {
      logger.info("Inside the reactive testing");
      Map<String, String> map = new HashMap<>();
      mapInjector.inject(span.context(), map);
      Mono.just("test-span-context")
          .doOnNext(s -> logger.info("inside the doOnNext {}", s))
          .flatMap(s -> callReactiveFunction(map, tracing).subscribeOn(Schedulers.parallel()))
          .transform(TracingUtil.<String>scopePassingSpanOperator(
              tracing.currentTraceContext(), span.context()))
          .subscribe(s -> logger.info("inside the subscribe {}", s));
      doSomethingExpensive();
    } finally {
      span.finish();
    }
  }

  private static Mono<String> callReactiveFunction(Map<String, String> map, Tracing tracing) {
    return Mono.defer(
        () -> {
          Extractor<Map<String, String>> mapExtractor =
              tracing.propagation().extractor(Map<String, String>::get);
          TraceContextOrSamplingFlags extractedContex = mapExtractor.extract(map);
          Span span = tracing.tracer().joinSpan(extractedContex.context());
          try (CurrentTraceContext.Scope scope =
              tracing.currentTraceContext().maybeScope(span.context())) {
            logger.info("created new scope inside the function");
            return Mono.just("new-string");

          } finally {
            span.finish();
          }
        }).subscribeOn(Schedulers.parallel());
  }

  private static void testTraceNormal(Tracing tracing) {
    Tracer tracer = tracing.tracer();
    Span span = tracer.newTrace().name("encode2").start();
    try (CurrentTraceContext.Scope scope =
        tracing.currentTraceContext().maybeScope(span.context())) {
      System.out.println("i am testing now.");
      logger.info("i am testing now.");
      doSomethingExpensive();
    } finally {
      span.finish();
    }
  }

  private static void testTraceTwoPhase(Tracing tracing) {
    Tracer tracer = tracing.tracer();
    Span twoPhase = tracer.newTrace().name("twoPhase").start();
    try {
      Span prepare = tracer.newChild(twoPhase.context()).name("prepare").start();
      try {
        prepare();
      } finally {
        prepare.finish();
      }
      Span commit = tracer.newChild(twoPhase.context()).name("commit").start();
      try {
        commit();
      } finally {
        commit.finish();
      }
    } finally {
      twoPhase.finish();
    }
  }

  private static void testTraceTwoPhase2(Tracing tracing) {
    Tracer tracer = tracing.tracer();
    Span twoPhase = tracer.newTrace().name("twoPhase").start();
    try {
      Span prepare = tracer.newChild(twoPhase.context()).name("prepare").start();
      try {
        prepare2Step();
      } finally {
        prepare.finish();
      }
      Span commit = tracer.newChild(twoPhase.context()).name("commit").start();
      try {
        commit();
      } finally {
        commit.finish();
      }
    } finally {
      twoPhase.finish();
    }
  }

  private static void doSomethingExpensive() {
    sleep(500);
  }

  private static void commit() {
    sleep(500);
  }

  private static void prepare() {
    sleep(500);
  }

  private static void prepare2Step() {
    Tracing tracing = Tracing.current();
    //        tracing.

    sleep(500);
  }

  private static void sleep(long milliseconds) {
    try {
      TimeUnit.MILLISECONDS.sleep(milliseconds);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
