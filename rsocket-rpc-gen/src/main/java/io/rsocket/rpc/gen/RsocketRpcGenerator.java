package io.rsocket.rpc.gen;

import com.salesforce.jprotoc.ProtocPlugin;

public class RsocketRpcGenerator extends AbstractRpcGenerator {

  public static void main(String[] args) {
    //
    if (args.length == 0) {
      ProtocPlugin.generate(new RsocketRpcGenerator());
    } else {
      ProtocPlugin.debug(new RsocketRpcGenerator(), args[0]);
    }
  }

  @Override
  protected String getClassPrefix() {
    return "Rsocket";
  }
}
