package io.rsocket.rpc.core.extension.error;

public class RsocketClientInitializationError extends RuntimeException {

  public RsocketClientInitializationError(String errorMessage) {
    super(errorMessage);
  }
}
