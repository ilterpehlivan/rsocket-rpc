package io.rsocket.rpc.core.extension.tracing;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;

/**
 * A {@link SpanSubscription} is a {@link Subscription} that fakes being {@link Fuseable}
 * (implementing {@link reactor.core.Fuseable.QueueSubscription} with default no-op
 * methods and always negotiating fusion to be {@link Fuseable#NONE}).
 *
 * @param <T> - type of the subscription
 * @author Marcin Grzejszczak
 */
interface SpanSubscription<T>
    extends Subscription, CoreSubscriber<T>, Fuseable.QueueSubscription<T> {

  @Override
  default T poll() {
    return null;
  }

  @Override
  default int requestFusion(int i) {
    return Fuseable.NONE; // always negotiate to no fusion
  }

  @Override
  default int size() {
    return 0;
  }

  @Override
  default boolean isEmpty() {
    return true;
  }

  @Override
  default void clear() {
    // NO-OP
  }

}