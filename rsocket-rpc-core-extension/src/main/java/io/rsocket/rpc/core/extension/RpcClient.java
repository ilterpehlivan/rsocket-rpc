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

  protected RpcClient(Mono<RSocket> rSocketMono, String serviceAddress) {
    this.rSocketMono =
        rSocketMono
            .doOnError(
                e ->
                    // TODO:add error counter from micrometer to count the connection errors as
                    // metrics rpc.connection.counter
                    log.error(
                        "Error received while connecting {} {}", serviceAddress, e.getMessage()))
            .doOnSuccess(
                (reactSocket) -> {
                  if (log.isDebugEnabled()) {
                    log.info("connected to {} successfully", serviceAddress);
                  }
                })
            .doOnSubscribe(s -> log.info("trying to connect service {} ", serviceAddress));
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
