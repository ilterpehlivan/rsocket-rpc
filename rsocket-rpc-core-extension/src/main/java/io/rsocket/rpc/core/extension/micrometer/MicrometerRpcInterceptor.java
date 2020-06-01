package io.rsocket.rpc.core.extension.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.rsocket.RSocket;
import io.rsocket.plugins.RSocketInterceptor;
import java.util.Objects;

public final class MicrometerRpcInterceptor implements RSocketInterceptor {

  private final MeterRegistry meterRegistry;

  private final Tag[] tags;

  /**
   * Creates a new {@link RSocketInterceptor}.
   *
   * @param meterRegistry the {@link MeterRegistry} to use to create
   * @param tags the additional tags to attach
   * @throws NullPointerException if {@code meterRegistry} is {@code null}
   */
  public MicrometerRpcInterceptor(MeterRegistry meterRegistry, Tag... tags) {
    this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry must not be null");
    this.tags = tags;
  }

  @Override
  public MicrometerRpc apply(RSocket delegate) {
    Objects.requireNonNull(delegate, "delegate must not be null");

    return new MicrometerRpc(delegate, meterRegistry, tags);
  }
}
