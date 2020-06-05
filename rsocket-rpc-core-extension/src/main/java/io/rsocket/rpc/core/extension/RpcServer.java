package io.rsocket.rpc.core.extension;

import io.rsocket.rpc.core.extension.routing.RoutingServerRSocket;
import io.rsocket.transport.netty.server.CloseableChannel;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public final class RpcServer {

  private final Mono<CloseableChannel> transportController;
  private final RoutingServerRSocket routingServerRSocket;
  private CloseableChannel closeableChannel;
  private Thread server;
  private final Object lock = new Object();
  private boolean started;

  protected RpcServer(Mono<CloseableChannel> transport, RoutingServerRSocket routingServerRSocket) {
    transportController = transport;
    this.routingServerRSocket = routingServerRSocket;
  }

  public RpcServer start() throws InterruptedException {
    synchronized (this.lock) {
      closeableChannel = transportController.block();
      return this;
    }
  }

  public void shutDown() {
    synchronized (this.lock) {
      if (!started) return;
      closeableChannel.dispose();
      // server.interrupt();
    }
  }

  public void awaitTermination() throws InterruptedException {
    synchronized (this.lock) {
      server =
          new Thread(
              () ->
                  closeableChannel
                      .onClose()
                      .doOnSubscribe(
                          s -> {
                            started = true;
                            log.info("server started address {}", closeableChannel.address());
                          })
                      .block(),
              format("rsocket-server-", 0));
      server.start();
      while (!started) ; // wait until thread executed
    }
  }

  private static String format(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }
}
