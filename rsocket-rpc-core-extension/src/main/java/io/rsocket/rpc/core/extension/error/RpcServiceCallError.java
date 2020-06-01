package io.rsocket.rpc.core.extension.error;

public class RpcServiceCallError extends Exception {

  public static final String ERROR_MSG = "error while calling the service";

  public RpcServiceCallError(Throwable cause) {
    super(ERROR_MSG, cause);
  }
}
