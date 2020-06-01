package io.rsocket.rpc.core.extension;

import io.rsocket.RSocketFactory.Start;
import io.rsocket.rpc.core.extension.routing.RoutingServerRSocket;
import io.rsocket.transport.netty.server.CloseableChannel;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RpcServer {

  private final Start<CloseableChannel> transportController;
  private final RoutingServerRSocket routingServerRSocket;
  private CloseableChannel closeableChannel;
  private Thread server;
  private final Object lock = new Object();
  private boolean started;
  private boolean terminated;

  // TODO: do we need routingServerRsocket here ?
  protected RpcServer(
      Start<CloseableChannel> transport, RoutingServerRSocket routingServerRSocket) {
    transportController = transport;
    this.routingServerRSocket = routingServerRSocket;
  }

  public RpcServer start() throws InterruptedException {
    synchronized (this.lock) {
      closeableChannel = transportController.start().block();
      return this;
    }
  }

  public void shutDown() {
    synchronized (this.lock) {
      if (!started) return;
      closeableChannel.dispose();
      //server.interrupt();
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
                            log.info("server started");
                          })
                      .block(),
              "rsocket-server-"+ Instant.now().toString());
      server.start();
      while (!started) ; // wait until thread executed
    }
  }
}
