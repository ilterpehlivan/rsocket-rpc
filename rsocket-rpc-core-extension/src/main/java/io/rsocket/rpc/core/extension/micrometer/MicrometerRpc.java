package io.rsocket.rpc.core.extension.micrometer;

import static io.rsocket.rpc.core.extension.micrometer.RpcTag.RSOCKET_METHOD_KEY;
import static io.rsocket.rpc.core.extension.routing.SchemaDescriptor.RPC_METHOD_KEY;
import static io.rsocket.rpc.core.extension.routing.SchemaDescriptor.RsocketMethodType.FIRE_FORGET;
import static io.rsocket.rpc.core.extension.routing.SchemaDescriptor.RsocketMethodType.REQUEST_CHANNEL;
import static io.rsocket.rpc.core.extension.routing.SchemaDescriptor.RsocketMethodType.REQUEST_REPLY;
import static io.rsocket.rpc.core.extension.routing.SchemaDescriptor.RsocketMethodType.REQUEST_STREAM;
import static reactor.core.publisher.SignalType.CANCEL;
import static reactor.core.publisher.SignalType.ON_COMPLETE;
import static reactor.core.publisher.SignalType.ON_ERROR;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.rpc.core.extension.routing.SchemaDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

public class MicrometerRpc implements RSocket {

  private final RSocket delegate;

  private final InteractionCounters metadataPush;

  private final InteractionCounters requestChannel;

  private final InteractionCounters requestFireAndForget;

  private final InteractionTimers requestResponse;

  private final InteractionCounters requestStream;

  /**
   * Creates a new {@link RSocket}.
   *
   * @param delegate the {@link RSocket} to delegate to
   * @param meterRegistry the {@link MeterRegistry} to use
   * @param tags additional tags to attach
   * @throws NullPointerException if {@code delegate} or {@code meterRegistry} is {@code null}
   */
  MicrometerRpc(RSocket delegate, MeterRegistry meterRegistry, Tag... tags) {
    this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    Objects.requireNonNull(meterRegistry, "meterRegistry must not be null");

    this.requestChannel = getReqChannelCounter(meterRegistry, tags);
    this.requestResponse = getReqResponseTimer(meterRegistry, tags);
    this.requestFireAndForget = getFireAndForgetCounter(meterRegistry, tags);
    this.requestStream = getReqStreamCounter(meterRegistry, tags);
    // TODO: metadataPush
    this.metadataPush = new InteractionCounters(meterRegistry, "metadata.push", tags);
  }

  private InteractionCounters getFireAndForgetCounter(MeterRegistry meterRegistry, Tag[] tags) {
    String counterName =
        Arrays.stream(tags)
            .filter(
                tag ->
                    SchemaDescriptor.getRsocketTypeFromRpcMethod(tag.getKey())
                            .compareTo(FIRE_FORGET)
                        == 0)
            .findFirst()
            .map(Tag::getValue)
            .orElse(FIRE_FORGET.getName());

    List<Tag> filteredTags =
        Arrays.stream(tags)
            .filter(tag -> !(tag.getKey().contains(RPC_METHOD_KEY)))
            .collect(Collectors.toList());
    filteredTags.add(new RpcTag(RSOCKET_METHOD_KEY, FIRE_FORGET.getName()));
    return new InteractionCounters(meterRegistry, counterName, filteredTags.toArray(new Tag[0]));
  }

  private InteractionTimers getReqResponseTimer(MeterRegistry meterRegistry, Tag[] tags) {
    String timerName =
        Arrays.stream(tags)
            .filter(
                tag ->
                    SchemaDescriptor.getRsocketTypeFromRpcMethod(tag.getKey())
                            .compareTo(REQUEST_REPLY)
                        == 0)
            .findFirst()
            .map(Tag::getValue)
            .orElse(
                SchemaDescriptor.getRpcMethod(
                    REQUEST_REPLY.getName())); // if not found default rpc.method.requestReply
    List<Tag> filteredTags =
        Arrays.stream(tags)
            .filter(
                tag -> !(tag.getKey().contains(RPC_METHOD_KEY))
                //                        ||
                // SchemaDescriptor.getRsocketTypeFromRpcMethod(tag.getKey())
                //                                .compareTo(REQUEST_REPLY)
                //                            == 0)
                )
            .collect(Collectors.toList());
    // After adding rsocket method name as tag
    filteredTags.add(new RpcTag(RSOCKET_METHOD_KEY, REQUEST_REPLY.getName()));
    return new InteractionTimers(meterRegistry, timerName, filteredTags.toArray(new Tag[0]));
  }

  private InteractionCounters getReqChannelCounter(MeterRegistry meterRegistry, Tag[] tags) {
    String counterName =
        Arrays.stream(tags)
            .filter(
                tag ->
                    SchemaDescriptor.getRsocketTypeFromRpcMethod(tag.getKey())
                            .compareTo(REQUEST_CHANNEL)
                        == 0)
            .findFirst()
            .map(Tag::getValue)
            .orElse(REQUEST_CHANNEL.getName());

    List<Tag> filteredTags =
        Arrays.stream(tags)
            .filter(tag -> !(tag.getKey().contains(RPC_METHOD_KEY)))
            .collect(Collectors.toList());
    filteredTags.add(new RpcTag(RSOCKET_METHOD_KEY, REQUEST_CHANNEL.getName()));
    return new InteractionCounters(meterRegistry, counterName, filteredTags.toArray(new Tag[0]));
  }

  private InteractionCounters getReqStreamCounter(MeterRegistry meterRegistry, Tag[] tags) {
    String counterName =
        Arrays.stream(tags)
            .filter(
                tag ->
                    SchemaDescriptor.getRsocketTypeFromRpcMethod(tag.getKey())
                            .compareTo(REQUEST_STREAM)
                        == 0)
            .findFirst()
            .map(Tag::getValue)
            .orElse(REQUEST_STREAM.getName());

    List<Tag> filteredTags =
        Arrays.stream(tags)
            .filter(tag -> !(tag.getKey().contains(RPC_METHOD_KEY)))
            .collect(Collectors.toList());
    filteredTags.add(new RpcTag(RSOCKET_METHOD_KEY, REQUEST_STREAM.getName()));
    return new InteractionCounters(meterRegistry, counterName, filteredTags.toArray(new Tag[0]));
  }

  @Override
  public void dispose() {
    delegate.dispose();
  }

  @Override
  public Mono<Void> fireAndForget(Payload payload) {
    return delegate.fireAndForget(payload).doFinally(requestFireAndForget);
  }

  @Override
  public Mono<Void> metadataPush(Payload payload) {
    return delegate.metadataPush(payload).doFinally(metadataPush);
  }

  @Override
  public Mono<Void> onClose() {
    return delegate.onClose();
  }

  @Override
  public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
    return delegate.requestChannel(payloads).doFinally(requestChannel);
  }

  @Override
  public Mono<Payload> requestResponse(Payload payload) {
    return Mono.defer(
        () -> {
          Sample sample = requestResponse.start();

          return delegate
              .requestResponse(payload)
              .doFinally(signalType -> requestResponse.accept(sample, signalType));
        });
  }

  @Override
  public Flux<Payload> requestStream(Payload payload) {
    return delegate.requestStream(payload).doFinally(requestStream);
  }

  private static final class InteractionCounters implements Consumer<SignalType> {

    private final Counter cancel;

    private final Counter onComplete;

    private final Counter onError;

    private InteractionCounters(MeterRegistry meterRegistry, String interactionModel, Tag... tags) {
      this.cancel = counter(meterRegistry, interactionModel, CANCEL, tags);
      this.onComplete = counter(meterRegistry, interactionModel, ON_COMPLETE, tags);
      this.onError = counter(meterRegistry, interactionModel, ON_ERROR, tags);
    }

    @Override
    public void accept(SignalType signalType) {
      switch (signalType) {
        case CANCEL:
          cancel.increment();
          break;
        case ON_COMPLETE:
          onComplete.increment();
          break;
        case ON_ERROR:
          onError.increment();
          break;
      }
    }

    private static Counter counter(
        MeterRegistry meterRegistry, String interactionModel, SignalType signalType, Tag... tags) {

      return meterRegistry.counter(
          interactionModel, Tags.of(tags).and("signal.type", signalType.name()));
    }
  }

  private static final class InteractionTimers implements BiConsumer<Sample, SignalType> {

    private final Timer cancel;

    private final MeterRegistry meterRegistry;

    private final Timer onComplete;

    private final Timer onError;

    private InteractionTimers(MeterRegistry meterRegistry, String interactionModel, Tag... tags) {
      this.meterRegistry = meterRegistry;

      this.cancel = timer(meterRegistry, interactionModel, CANCEL, tags);
      this.onComplete = timer(meterRegistry, interactionModel, ON_COMPLETE, tags);
      this.onError = timer(meterRegistry, interactionModel, ON_ERROR, tags);
    }

    @Override
    public void accept(Sample sample, SignalType signalType) {
      switch (signalType) {
        case CANCEL:
          sample.stop(cancel);
          break;
        case ON_COMPLETE:
          sample.stop(onComplete);
          break;
        case ON_ERROR:
          sample.stop(onError);
          break;
      }
    }

    Sample start() {
      return Timer.start(meterRegistry);
    }

    private static Timer timer(
        MeterRegistry meterRegistry, String interactionModel, SignalType signalType, Tag... tags) {

      return meterRegistry.timer(
          interactionModel, Tags.of(tags).and("signal.type", signalType.name()));
    }
  }
}
