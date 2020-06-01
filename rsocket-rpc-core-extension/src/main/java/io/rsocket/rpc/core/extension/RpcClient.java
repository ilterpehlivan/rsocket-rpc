package io.rsocket.rpc.core.extension;

import io.rsocket.RSocket;
import io.rsocket.rpc.core.extension.error.RsocketClientInitializationError;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class RpcClient {

  public static final int TIMEOUT = 3;
  private final Mono<RSocket> rSocketMono;

  protected RpcClient(Mono<RSocket> rSocketMono) {
    this.rSocketMono = rSocketMono;
  }

  protected RpcClient(Mono<RSocket> rSocketMono, CountDownLatch rsocketInit) {
    this.rSocketMono = rSocketMono;
    try {
      rsocketInit.await(TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("client initialization failed. could not generate a socket in {} seconds", TIMEOUT);
      throw new RsocketClientInitializationError("Could not be generated socket");
    }
  }

  public Mono<RSocket> getrSocketMono() {
    return rSocketMono;
  }

}
