package io.rsocket.rpc.core.extension.routing;

import io.grpc.HandlerRegistry;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class ServiceHandlerRegistry extends HandlerRegistry {
  private final List<ServerServiceDefinition> services;
  private final Map<String, ServerMethodDefinition<?, ?>> methods;

  public ServiceHandlerRegistry(
      List<ServerServiceDefinition> services, Map<String, ServerMethodDefinition<?, ?>> methods) {
    this.services = services;
    this.methods = methods;
  }

  /** Returns the service definitions in this registry. */
  @Override
  public List<ServerServiceDefinition> getServices() {
    return services;
  }

  @Nullable
  @Override
  public ServerMethodDefinition<?, ?> lookupMethod(String methodName, @Nullable String authority) {
    return methods.get(methodName);
  }

  public static final class Builder {

    // Store per-service first, to make sure services are added/replaced atomically.
    private final HashMap<String, ServerServiceDefinition> services = new LinkedHashMap<>();

    public Builder addService(ServerServiceDefinition service) {
      services.put(service.getServiceDescriptor().getName(), service);
      return this;
    }

    public ServiceHandlerRegistry build() {
      Map<String, ServerMethodDefinition<?, ?>> map = new HashMap<>();
      for (ServerServiceDefinition service : services.values()) {
        for (ServerMethodDefinition<?, ?> method : service.getMethods()) {
          map.put(method.getMethodDescriptor().getFullMethodName(), method);
        }
      }
      return new ServiceHandlerRegistry(
          Collections.unmodifiableList(new ArrayList<>(services.values())),
          Collections.unmodifiableMap(map));
    }
  }
}
