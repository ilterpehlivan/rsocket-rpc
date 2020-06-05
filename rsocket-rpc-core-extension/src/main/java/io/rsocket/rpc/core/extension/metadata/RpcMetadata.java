package io.rsocket.rpc.core.extension.metadata;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.rsocket.util.NumberUtils;
import java.nio.charset.StandardCharsets;

public class RpcMetadata {

  public static final int SERVICE_NAME_LENGTH = 1;
  public static final int METHOD_NAME_LENGTH = 1;
  public static final int HEADER_LENGTH = 1;

  public static class RpcServiceMetadata implements MetadataAware {
    String serviceName;

    public RpcServiceMetadata(String service) {
      this.serviceName = service;
    }

    public RpcServiceMetadata() {}

    public String getServiceName() {
      return serviceName;
    }

    @Override
    public RSocketMimeType rsocketMimeType() {
      return RSocketMimeType.RpcMessageService;
    }

    @Override
    public String getMimeType() {
      return RSocketMimeType.RpcMessageService.getType();
    }

    @Override
    public ByteBuf getContent() {
      ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer().writeShort(SERVICE_NAME_LENGTH);
      int serviceLength = NumberUtils.requireUnsignedShort(ByteBufUtil.utf8Bytes(serviceName));
      byteBuf.writeShort(serviceLength);
      ByteBufUtil.reserveAndWriteUtf8(byteBuf, serviceName, serviceLength);
      return byteBuf;
    }

    @Override
    public void load(ByteBuf byteBuf) {
      if (byteBuf.readableBytes() == 0) {
        serviceName = null;
      }

      int offset = Short.BYTES;

      int serviceLength = byteBuf.getShort(offset);
      offset += Short.BYTES;

      serviceName = byteBuf.toString(offset, serviceLength, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
      return "RpcServiceMetadata{" +
          "serviceName='" + serviceName + '\'' +
          '}';
    }
  }

  public static class RpcMethodMetadata implements MetadataAware {
    String methodName;

    public RpcMethodMetadata(String method) {
      this.methodName = method;
    }

    public RpcMethodMetadata() {}

    public String getMethodName() {
      return methodName;
    }

    @Override
    public RSocketMimeType rsocketMimeType() {
      return RSocketMimeType.RpcMessageMethod;
    }

    @Override
    public String getMimeType() {
      return RSocketMimeType.RpcMessageMethod.getType();
    }

    @Override
    public ByteBuf getContent() {
      ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer().writeShort(METHOD_NAME_LENGTH);
      int methodLength = NumberUtils.requireUnsignedShort(ByteBufUtil.utf8Bytes(methodName));
      byteBuf.writeShort(methodLength);
      ByteBufUtil.reserveAndWriteUtf8(byteBuf, methodName, methodLength);
      return byteBuf;
    }

    @Override
    public void load(ByteBuf byteBuf) {
      if (byteBuf.readableBytes() == 0) {
        methodName = null;
      }

      int offset = Short.BYTES;

      int methodLength = byteBuf.getShort(offset);
      offset += Short.BYTES;

      methodName = byteBuf.toString(offset, methodLength, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
      return "RpcMethodMetadata{" +
          "methodName='" + methodName + '\'' +
          '}';
    }
  }

  public static class RpcHeadersMetadata implements MetadataAware {
    ByteBuf headers;

    public RpcHeadersMetadata(ByteBuf headers) {
      this.headers = headers;
    }

    public RpcHeadersMetadata() {}

    public ByteBuf getHeaders() {
      return headers;
    }

    @Override
    public RSocketMimeType rsocketMimeType() {
      return RSocketMimeType.RpcMessageHeaders;
    }

    @Override
    public String getMimeType() {
      return RSocketMimeType.RpcMessageHeaders.getType();
    }

    @Override
    public ByteBuf getContent() {
      ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer().writeShort(HEADER_LENGTH);
      if (headers.readableBytes() == 0) {
        headers = Unpooled.EMPTY_BUFFER;
      }
      byteBuf.writeShort(headers.readableBytes());
      byteBuf.writeBytes(headers, headers.readerIndex(), headers.readableBytes());

      return byteBuf;
    }

    @Override
    public void load(ByteBuf byteBuf) {
      int offset = Short.BYTES;

      int metadataLength = byteBuf.readableBytes() - offset;
      headers = metadataLength > 0 ? byteBuf.slice(offset, metadataLength) : Unpooled.EMPTY_BUFFER;
    }

    @Override
    public String toString() {
      return "RpcHeadersMetadata{" +
          "headers=" + headers.toString() +
          '}';
    }
  }
}
