package io.rsocket.rpc.core.extension.micrometer;

import static java.util.Objects.requireNonNull;

import io.micrometer.core.instrument.Tag;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RpcTag implements Tag {
  public static final String REQUEST_REPLY = "requestReply";
  public static final String FIRE_FORGET = "fireAndForget";
  public static final String REQUEST_STREAM = "requestStream";
  public static final String REQUEST_CHANNEL = "requestChannel";

  public static final String SERVICE_NAME_KEY = "srv.name";
  public static final String ORIGINATOR_TYPE = "call.origin";
  public static final String ORIGIN_CLIENT = "origin.client";
  public static final String ORIGIN_SERVER = "origin.server";
  public static final String RSOCKET_METHOD_KEY = "rsocket.method";
  private static final String REQUEST_KEY = "request.";

  String rpcTagKey;
  String rpcTagValue;

  public RpcTag(String rpcTagKey, String rpcTagValue) {
    requireNonNull(rpcTagKey);
    requireNonNull(rpcTagValue);
    this.rpcTagKey = rpcTagKey;
    this.rpcTagValue = rpcTagValue;
  }

  // Tag -> [ Actual Method: RsocketType ]
  // Example: [greet : requestReply]
  public static Tag[] getClientTags(String serviceName, Map<String, String> methodMapping) {
    List<RpcTag> rpcTags =
        methodMapping.entrySet().stream()
            .map(e -> new RpcTag(REQUEST_KEY + e.getValue(), e.getKey()))
            .collect(Collectors.toList());

    rpcTags.add(new RpcTag(SERVICE_NAME_KEY, serviceName));
    rpcTags.add(new RpcTag(ORIGINATOR_TYPE, ORIGIN_CLIENT));
    return rpcTags.toArray(new Tag[0]);
  }

  public static Tag[] getServerTags(String serviceName, Map<String, String> methodMapping) {
    List<RpcTag> rpcTags =
        methodMapping.entrySet().stream()
            .map(e -> new RpcTag(REQUEST_KEY + e.getValue(),e.getKey()))
            .collect(Collectors.toList());

    rpcTags.add(new RpcTag(SERVICE_NAME_KEY, serviceName));
    rpcTags.add(new RpcTag(ORIGINATOR_TYPE, ORIGIN_SERVER));
    return rpcTags.toArray(new Tag[0]);
  }

  @Override
  public String getKey() {
    return rpcTagKey;
  }

  @Override
  public String getValue() {
    return rpcTagValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RpcTag rpcTag = (RpcTag) o;
    return rpcTagKey.equals(rpcTag.rpcTagKey) && rpcTagValue.equals(rpcTag.rpcTagValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rpcTagKey, rpcTagValue);
  }

  @Override
  public String toString() {
    return "RpcTag{"
        + "rpcTagKey='"
        + rpcTagKey
        + '\''
        + ", rpcTagValue='"
        + rpcTagValue
        + '\''
        + '}';
  }
}
