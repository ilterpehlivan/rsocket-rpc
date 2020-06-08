package io.rsocket.rpc.core.extension.metadata;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.CompositeMetadata.Entry;
import io.rsocket.metadata.CompositeMetadataFlyweight;
import io.rsocket.rpc.core.extension.error.ServiceNotFound;
import io.rsocket.rpc.core.extension.metadata.RpcMetadata.RpcHeadersMetadata;
import io.rsocket.rpc.core.extension.metadata.RpcMetadata.RpcMethodMetadata;
import io.rsocket.rpc.core.extension.metadata.RpcMetadata.RpcServiceMetadata;
import io.rsocket.util.NumberUtils;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetaDataUtil {
  // Version
  public static final short VERSION = 1;

  // composite key values
  public static final String MESSAGING_X_SERVICE_NAME = "messaging/x.service";
  public static final String MESSAGING_X_METHOD_NAME = "messaging/x.method";
  public static final String MESSAGING_X_HEADERS = "messaging/x.headers";
  public static final String MESSAGING_X_TRACING = "messaging/x.tracing";

  public static ByteBuf encodeRpcComposite(String service, String method, ByteBuf headers) {
    RSocketCompositeMetadata compositeMetadata = new RSocketCompositeMetadata();
    RpcServiceMetadata serviceMetadata = new RpcServiceMetadata(service);
    RpcMethodMetadata methodMetadata = new RpcMethodMetadata(method);
    RpcHeadersMetadata headersMetadata = new RpcHeadersMetadata(headers);
    compositeMetadata.addMetadata(serviceMetadata);
    compositeMetadata.addMetadata(methodMetadata);
    compositeMetadata.addMetadata(headersMetadata);

    return compositeMetadata.getContent();
  }

  public static ByteBuf encodeRpcComposite(
      String service, String method, ByteBuf headers, MapBasedTracingMetadata tracingMetadata) {
    RSocketCompositeMetadata compositeMetadata = new RSocketCompositeMetadata();
    RpcServiceMetadata serviceMetadata = new RpcServiceMetadata(service);
    RpcMethodMetadata methodMetadata = new RpcMethodMetadata(method);
    RpcHeadersMetadata headersMetadata = new RpcHeadersMetadata(headers);
    compositeMetadata.addMetadata(serviceMetadata);
    compositeMetadata.addMetadata(methodMetadata);
    compositeMetadata.addMetadata(headersMetadata);
    compositeMetadata.addMetadata(tracingMetadata);

    return compositeMetadata.getContent();
  }

  public static RsocketRpcRequest getRpcRequest(ByteBuf rpcComposite) {
    RSocketCompositeMetadata compositeMetadata = RSocketCompositeMetadata.from(rpcComposite);
    RsocketRpcRequest rpcRequest = new RsocketRpcRequest();
    rpcRequest.setService(compositeMetadata.getServiceMetadata().getServiceName());
    rpcRequest.setMethod(compositeMetadata.getMethodMetadata().getMethodName());
    rpcRequest.setHeaders(compositeMetadata.getHeadersMetadata().getHeaders());

    return rpcRequest;
  }

  public static Map<String, String> getRpcTracingContextMap(ByteBuf rpcComposite) {
    RSocketCompositeMetadata compositeMetadata = RSocketCompositeMetadata.from(rpcComposite);
    return compositeMetadata.getTracingMetadata().getTracingMap();
  }

  public static ByteBuf addTracing2RpcComposite(
      ByteBuf rpcComposite, MapBasedTracingMetadata tracing) {
    RSocketCompositeMetadata compositeMetadata =
        RSocketCompositeMetadata.from(Unpooled.copiedBuffer(rpcComposite));
    compositeMetadata.addMetadata(tracing);
    // no need this anymore as we copied
    rpcComposite.release();
    // TODO:Why is this retain needed ? Otherwise it gives release error
    return compositeMetadata.getContent();
  }

  public static void printRpcCompositeMetadata(ByteBuf rpcByteBuf) {
    if (log.isDebugEnabled()) {
      RSocketCompositeMetadata compositeMetadata = RSocketCompositeMetadata.from(rpcByteBuf);
      log.debug(compositeMetadata.toString());
    }
  }

  public static CompositeByteBuf encodeComposite(String service, String method, ByteBuf metadata) {
    CompositeByteBuf metadataByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();

    // Adding the service to the composite metadata
    CompositeMetadataFlyweight.encodeAndAddMetadata(
        metadataByteBuf,
        ByteBufAllocator.DEFAULT,
        MESSAGING_X_SERVICE_NAME,
        ByteBufAllocator.DEFAULT.buffer().writeBytes(service.getBytes()));

    // Adding the method name to the composite metadata
    CompositeMetadataFlyweight.encodeAndAddMetadata(
        metadataByteBuf,
        ByteBufAllocator.DEFAULT,
        MESSAGING_X_METHOD_NAME,
        ByteBufAllocator.DEFAULT.buffer().writeBytes(method.getBytes()));

    if (metadata != null) {
      // Adding the custom headers to the composite metadata
      CompositeMetadataFlyweight.encodeAndAddMetadata(
          metadataByteBuf,
          ByteBufAllocator.DEFAULT,
          MESSAGING_X_HEADERS,
          ByteBufAllocator.DEFAULT.buffer().writeBytes(metadata.array()));
    }

    return metadataByteBuf;
  }

  // metadata must be composite
  public static CompositeByteBuf addCompositeTracing(ByteBuf metadata, ByteBuf tracing) {
    // TODO: right now we are parsing and recrating the composite tracing but there may be more
    // efficient way!!
    RsocketRpcRequest request = parseComposite(metadata);
    CompositeByteBuf compositeByteBuf =
        encodeComposite(request.getService(), request.getMethod(), request.getHeaders());

    // Adding the tracing to the composite metadata
    CompositeMetadataFlyweight.encodeAndAddMetadata(
        compositeByteBuf,
        ByteBufAllocator.DEFAULT,
        MESSAGING_X_TRACING,
        ByteBufAllocator.DEFAULT.buffer().writeBytes(tracing));

    // no more need previous one
    metadata.release();

    return compositeByteBuf;
  }

  public static RsocketRpcRequest parseComposite(Payload payload) {
    ByteBuf metadata = payload.metadata();
    return getRsocketRpcRequest(metadata);
  }

  public static RsocketRpcRequest parseComposite(ByteBuf metadata) {
    return getRsocketRpcRequest(metadata);
  }

  public static ByteBuf getCompositeTracing(ByteBuf metadata) {
    CompositeMetadata compositeMetadata = new CompositeMetadata(metadata, true);
    Optional<Entry> tracingEntry =
        compositeMetadata.stream()
            .filter(entry -> MESSAGING_X_TRACING.equalsIgnoreCase(entry.getMimeType()))
            .findFirst();
    if (tracingEntry.isPresent()) return tracingEntry.get().getContent();

    // default
    return Unpooled.EMPTY_BUFFER;
  }

  public static void printCompositeMeta(ByteBuf metadata) {
    if (log.isDebugEnabled()) {
      CompositeMetadata compositeMetadata = new CompositeMetadata(metadata, true);
      compositeMetadata.forEach(
          entry -> {
            log.debug(
                "composite mime {} utf8Data {}",
                entry.getMimeType(),
                new String(ByteBufUtil.getBytes(entry.getContent()), StandardCharsets.UTF_8));
          });
    }
  }

  public static String getCompositeService(ByteBuf metadata) {
    CompositeMetadata compositeMetadata = new CompositeMetadata(metadata, true);
    Optional<Entry> serviceEntry =
        compositeMetadata.stream()
            .filter(entry -> MESSAGING_X_SERVICE_NAME.equalsIgnoreCase(entry.getMimeType()))
            .findFirst();
    if (serviceEntry.isPresent()) {
      byte[] bytes = new byte[serviceEntry.get().getContent().readableBytes()];
      serviceEntry.get().getContent().readBytes(bytes);
      return new String(bytes, StandardCharsets.UTF_8);
    }

    // default
    throw new ServiceNotFound("service could not be found");
  }

  public static String getCompositeMethod(ByteBuf metadata) {
    CompositeMetadata compositeMetadata = new CompositeMetadata(metadata, true);
    Optional<Entry> methodEntry =
        compositeMetadata.stream()
            .filter(entry -> MESSAGING_X_METHOD_NAME.equalsIgnoreCase(entry.getMimeType()))
            .findFirst();
    if (methodEntry.isPresent()) {
      byte[] bytes = new byte[methodEntry.get().getContent().readableBytes()];
      methodEntry.get().getContent().readBytes(bytes);
      return new String(bytes, StandardCharsets.UTF_8);
    }

    // default
    throw new ServiceNotFound("method could not be found");
  }

  private static RsocketRpcRequest getRsocketRpcRequest(ByteBuf metadata) {
    RsocketRpcRequest request = new RsocketRpcRequest();
    CompositeMetadata compositeMetadata = new CompositeMetadata(metadata, true);
    compositeMetadata.forEach(
        entry -> {
          if (MESSAGING_X_SERVICE_NAME.equalsIgnoreCase(entry.getMimeType())) {
            byte[] bytes = new byte[entry.getContent().readableBytes()];
            entry.getContent().readBytes(bytes);
            request.setService(new String(bytes, StandardCharsets.UTF_8));
          } else if (MESSAGING_X_METHOD_NAME.equalsIgnoreCase(entry.getMimeType())) {
            byte[] bytes = new byte[entry.getContent().readableBytes()];
            entry.getContent().readBytes(bytes);
            request.setMethod(new String(bytes, StandardCharsets.UTF_8));
          } else if (MESSAGING_X_HEADERS.equalsIgnoreCase(entry.getMimeType())) {
            request.setHeaders(entry.getContent());
          }
        });

    return request;
  }

  // Standard order is changed from Rpc core
  // RPC Core Order:   service - method - tracing - metadata
  // This extension Order: service - method - metadata - tracing

  public static ByteBuf encode(
      ByteBufAllocator allocator, String service, String method, ByteBuf metadata) {
    return encode(allocator, service, method, Unpooled.EMPTY_BUFFER, metadata);
  }

  // Order: service - method - metadata - tracing
  public static ByteBuf encode(
      ByteBufAllocator allocator,
      String service,
      String method,
      ByteBuf tracing,
      ByteBuf metadata) {
    ByteBuf byteBuf = allocator.buffer().writeShort(VERSION);

    int serviceLength = NumberUtils.requireUnsignedShort(ByteBufUtil.utf8Bytes(service));
    byteBuf.writeShort(serviceLength);
    ByteBufUtil.reserveAndWriteUtf8(byteBuf, service, serviceLength);

    int methodLength = NumberUtils.requireUnsignedShort(ByteBufUtil.utf8Bytes(method));
    byteBuf.writeShort(methodLength);
    ByteBufUtil.reserveAndWriteUtf8(byteBuf, method, methodLength);

    byteBuf.writeShort(metadata.readableBytes());
    byteBuf.writeBytes(metadata, metadata.readerIndex(), metadata.readableBytes());

    byteBuf.writeBytes(tracing, tracing.readerIndex(), tracing.readableBytes());

    return byteBuf;
  }

  public static int getVersion(ByteBuf byteBuf) {
    return byteBuf.getShort(0) & 0x7FFF;
  }

  public static String getService(ByteBuf byteBuf) {
    int offset = Short.BYTES;

    int serviceLength = byteBuf.getShort(offset);
    offset += Short.BYTES;

    return byteBuf.toString(offset, serviceLength, StandardCharsets.UTF_8);
  }

  public static String getMethod(ByteBuf byteBuf) {
    int offset = Short.BYTES;

    int serviceLength = byteBuf.getShort(offset);
    offset += Short.BYTES + serviceLength;

    int methodLength = byteBuf.getShort(offset);
    offset += Short.BYTES;

    return byteBuf.toString(offset, methodLength, StandardCharsets.UTF_8);
  }

  public static ByteBuf getMetadata(ByteBuf byteBuf) {
    int offset = Short.BYTES;

    int serviceLength = byteBuf.getShort(offset);
    offset += Short.BYTES + serviceLength;

    int methodLength = byteBuf.getShort(offset);
    offset += Short.BYTES + methodLength;

    int metaDataLength = byteBuf.getShort(offset);
    offset += Short.BYTES;

    return metaDataLength > 0 ? byteBuf.slice(offset, metaDataLength) : Unpooled.EMPTY_BUFFER;
  }

  public static ByteBuf getTracing(ByteBuf byteBuf) {
    int offset = Short.BYTES;

    int serviceLength = byteBuf.getShort(offset);
    offset += Short.BYTES + serviceLength;

    int methodLength = byteBuf.getShort(offset);
    offset += Short.BYTES + methodLength;

    int metadataLength = byteBuf.getShort(offset);
    offset += Short.BYTES + metadataLength;

    int tracingLength = byteBuf.readableBytes() - offset;
    return tracingLength > 0 ? byteBuf.slice(offset, tracingLength) : Unpooled.EMPTY_BUFFER;
  }

  // TODO: merge this with RsocketRpcRequest class
  public static class RsocketRpcRequest {

    private String service;
    private String method;
    private ByteBuf headers;

    private RsocketRpcRequest() {}

    private void setService(String service) {
      this.service = service;
    }

    private void setMethod(String method) {
      this.method = method;
    }

    private void setHeaders(ByteBuf content) {
      this.headers = content;
    }

    public String getService() {
      return service;
    }

    public String getMethod() {
      return method;
    }

    public ByteBuf getHeaders() {
      return headers;
    }
  }
}
