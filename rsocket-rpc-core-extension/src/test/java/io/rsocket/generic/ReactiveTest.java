package io.rsocket.generic;

import io.rsocket.rpc.core.extension.tracing.TracingUtil;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ReactiveTest {

  @Test
  public void testMono() {
    Mono.just("Test-Start")
        .doFinally(signalType -> System.out.println("signal received "+signalType)).block();
  }


  @Test
  public void testReferenceStream2Function2Merge() throws InterruptedException {
    Flux<Integer> rangeFlux = Flux.range(1, 5);
    BiFunction<Integer, Integer, Integer> method = (i, s) -> i * s;

    rangeFlux
        .transform(ReactiveTest.<Integer>doTransform(method))
        .subscribe(i -> System.out.println("inside the subscribe" + i));
    Thread.sleep(200);
  }

  @Test
  public void testContext() throws InterruptedException {
    Flux<Integer> numberFlux = Flux.range(1, 5);
    numberFlux
        .flatMap(
            i ->
                Mono.subscriberContext()
                    .doOnNext(ctx -> System.out.println(Scannable.from(ctx).name()))
                    .map(context -> Tuples.of(i, context.<Integer>getOrEmpty("Test"))))
        //        .handle((d,c) ->{
        //          if (c.currentContext().hasKey("Test")){
        //            c.currentContext().
        //          }
        //        })
        .doOnNext(t -> System.out.println("doOnNext:" + t.getT1()))
        .map(
            tuple -> {
              System.out.println("inside the map" + tuple.getT1());
              if (tuple.getT2().isPresent()) {
                System.out.println("inside context available");
                return tuple.getT1() * tuple.getT2().get();
              } else {
                return tuple.getT1();
              }
            })
        .subscriberContext(Context.of("Test", 300))
        .subscribe(i -> System.out.println("onsubscribe:" + i));
    Thread.sleep(300);
  }

  @Test
  public void testSingleSwithc() {
    Flux<Integer> parameters =
        Flux.range(1, 4)
            .switchOnFirst(
                (first, flux) -> {
                  int f = first.get();
                  return flux
                      .skip(1)
                      .startWith(f * 100)
                      .subscriberContext(ctx->{
                        ctx.put("start",f);
                        ctx.put("test","test-"+f);
                        return ctx;
                      });
                })
            .doOnComplete(() -> System.out.println("Downstream Completed"));

    Flux<Integer> results = getResults(parameters);
    results.subscribe(res -> System.out.println("result:" + res));
  }

  private Flux<Integer> getResults(Flux<Integer> parameters) {
    return parameters.doOnNext(t -> System.out.println(t)).map(x -> x * 2);
  }

  public static <T> Function<? super Publisher<T>, ? extends Publisher<T>> doTransform(
      BiFunction method) {
    return Operators.lift(
        (scannable, sub) -> {
          return new TestSampleSubscriber<T>(sub, method);
        });
  }

  public static class TestSampleSubscriber<T> extends BaseSubscriber {
    private final Subscriber<? super T> subscriber;
    final BiFunction<Integer, Integer, T> method;

    public TestSampleSubscriber(
        Subscriber<? super T> coreSubscriber, BiFunction<Integer, Integer, T> method) {
      this.method = method;
      this.subscriber = coreSubscriber;
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
      super.hookOnSubscribe(subscription);
    }

    @Override
    protected void hookOnNext(Object value) {
      System.out.println("hookOnNext:" + value);
      T appliedValue = method.apply((Integer) value, 2);
      System.out.println("applied value:" + appliedValue);
      subscriber.onNext(appliedValue);
      //      super.hookOnNext(method.apply((Integer) value, "test"));
    }

    @Override
    protected void hookOnComplete() {
      super.hookOnComplete();
    }

    @Override
    protected void hookOnError(Throwable throwable) {
      super.hookOnError(throwable);
    }
  }

  @Test
  public void testDoEach() {

    Consumer<? super Signal<Integer>> consumer =
        integerSignal -> {
          if (integerSignal.isOnNext()) {
            Mono.defer(
                () -> {
                  System.out.println("mono defer");
                  return Mono.just("Hello");
                });
          }
        };
    Flux.range(1, 3).doOnEach(consumer).subscribe(s -> System.out.println("subscribe"));
  }

  @Test
  public void testSupplier() throws InterruptedException {
    Function<? super Mono<Integer>, ? extends Publisher<Object>> doTransform =
        Operators.lift((scannable, coreSubscriber) -> new TestSubscriber<Object>(coreSubscriber));
    CountDownLatch latch = new CountDownLatch(1);
    Mono.defer(
            (Supplier<Mono<Integer>>)
                () -> {
                  System.out.println("inside supplier:get");
                  return Mono.just(1);
                })
        .map(i -> i * 2)
        .transform(doTransform)
        .subscribe(
            o -> {
              System.out.println("inside SUBSCRIBE:" + o);
              latch.countDown();
            });

    latch.await();
  }

  @Test
  public void generateMonoFromFlux() {
    Mono.from(Flux.range(1, 3)).subscribe(e -> System.out.println(e));
  }

  private class TestSubscriber<T> implements CoreSubscriber<Object>, Subscription {

    private final Subscriber<Object> subscriber;
    private Subscription subscription;

    public TestSubscriber(Subscriber<Object> coreSubscriber) {
      this.subscriber = coreSubscriber;
    }

    @Override
    public void onSubscribe(Subscription s) {
      this.subscription = s;
      System.out.println("inside onSubscribe..");
      subscriber.onSubscribe(this);
    }

    @Override
    public void onNext(Object o) {
      System.out.println("inside onNext");
      this.subscriber.onNext(o);
    }

    @Override
    public void onError(Throwable t) {}

    @Override
    public void onComplete() {
      System.out.println("inside onComplete");
    }

    @Override
    public void request(long n) {
      System.out.println("inside request");
      subscription.request(n);
    }

    @Override
    public void cancel() {
      System.out.println("inside cancel");
      subscription.cancel();
    }
  }
}
