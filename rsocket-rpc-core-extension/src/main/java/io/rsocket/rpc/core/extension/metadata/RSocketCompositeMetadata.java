package io.rsocket.rpc.core.extension.metadata;

import static io.rsocket.metadata.WellKnownMimeType.UNPARSEABLE_MIME_TYPE;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.rpc.core.extension.metadata.RpcMetadata.RpcHeadersMetadata;
import io.rsocket.rpc.core.extension.metadata.RpcMetadata.RpcMethodMetadata;
import io.rsocket.rpc.core.extension.metadata.RpcMetadata.RpcServiceMetadata;
import java.util.HashMap;
import java.util.Map;
import reactor.util.annotation.Nullable;

/** RSocket composite metadata to wrap CompositeMetadata */
public class RSocketCompositeMetadata implements MetadataAware {
  private Map<String, ByteBuf> metadataStore = new HashMap<>();

  private RpcServiceMetadata serviceMetadata;
  private RpcMethodMetadata methodMetadata;
  private RpcHeadersMetadata headersMetada;
  private MapBasedTracingMetadata tracingMetadata;

  public static RSocketCompositeMetadata from(ByteBuf content) {
    RSocketCompositeMetadata temp = new RSocketCompositeMetadata();
    if (content.isReadable()) {
      temp.load(content);
    }
    return temp;
  }

  public static RSocketCompositeMetadata from(MetadataAware... metadataList) {
    RSocketCompositeMetadata temp = new RSocketCompositeMetadata();
    for (MetadataAware metadataAware : metadataList) {
      temp.addMetadata(metadataAware);
    }
    return temp;
  }

  public RSocketCompositeMetadata() {}

  @Override
  public RSocketMimeType rsocketMimeType() {
    return RSocketMimeType.CompositeMetadata;
  }

  @Override
  public String getMimeType() {
    return RSocketMimeType.CompositeMetadata.getType();
  }

  @Override
  public ByteBuf getContent() {
    CompositeByteBuf compositeByteBuf = PooledByteBufAllocator.DEFAULT.compositeBuffer();
    for (Map.Entry<String, ByteBuf> entry : metadataStore.entrySet()) {
      WellKnownMimeType wellKnownMimeType = WellKnownMimeType.fromString(entry.getKey());
      if (wellKnownMimeType != UNPARSEABLE_MIME_TYPE) {
        CompositeMetadataCodec.encodeAndAddMetadata(
            compositeByteBuf, PooledByteBufAllocator.DEFAULT, wellKnownMimeType, entry.getValue());
      } else {
        CompositeMetadataCodec.encodeAndAddMetadata(
            compositeByteBuf, PooledByteBufAllocator.DEFAULT, entry.getKey(), entry.getValue());
      }
    }
    return compositeByteBuf;
  }

  @Override
  public void load(ByteBuf byteBuf) {
    try {
      CompositeMetadata compositeMetadata = new CompositeMetadata(byteBuf, true);
      for (CompositeMetadata.Entry entry : compositeMetadata) {
        metadataStore.put(entry.getMimeType(), entry.getContent());
      }
    } finally {
      byteBuf.release();
    }
  }

  public ByteBuf getMetadata(RSocketMimeType mimeType) {
    return metadataStore.get(mimeType.getType());
  }

  public boolean contains(RSocketMimeType mimeType) {
    return metadataStore.containsKey(mimeType.getType());
  }

  @SuppressWarnings("UnusedReturnValue")
  public RSocketCompositeMetadata addMetadata(MetadataAware metadataSupport) {
    metadataStore.put(metadataSupport.getMimeType(), metadataSupport.getContent());
    return this;
  }

  public RpcServiceMetadata getServiceMetadata() {
    if (this.serviceMetadata == null
        && metadataStore.containsKey(RSocketMimeType.RpcMessageService.getType())) {
      this.serviceMetadata = new RpcServiceMetadata();
      ByteBuf byteBuf = metadataStore.get(RSocketMimeType.RpcMessageService.getType());
      this.serviceMetadata.load(byteBuf);
    }
    return serviceMetadata;
  }

  public RpcMethodMetadata getMethodMetadata() {
    if (this.methodMetadata == null
        && metadataStore.containsKey(RSocketMimeType.RpcMessageMethod.getType())) {
      this.methodMetadata = new RpcMethodMetadata();
      ByteBuf byteBuf = metadataStore.get(RSocketMimeType.RpcMessageMethod.getType());
      this.methodMetadata.load(byteBuf);
    }
    return methodMetadata;
  }

  public RpcHeadersMetadata getHeadersMetadata() {
    if (this.headersMetada == null
        && metadataStore.containsKey(RSocketMimeType.RpcMessageHeaders.getType())) {
      this.headersMetada = new RpcHeadersMetadata();
      ByteBuf byteBuf = metadataStore.get(RSocketMimeType.RpcMessageHeaders.getType());
      this.headersMetada.load(byteBuf);
    }
    return headersMetada;
  }

  @Nullable
  public MapBasedTracingMetadata getTracingMetadata() {
    if (this.tracingMetadata == null
        && metadataStore.containsKey(RSocketMimeType.Tracing.getType())) {
      this.tracingMetadata = new MapBasedTracingMetadata();
      ByteBuf byteBuf = metadataStore.get(RSocketMimeType.Tracing.getType());
      this.tracingMetadata.load(byteBuf);
    }
    return tracingMetadata;
  }

  @Override
  public String toString() {
    return "RSocketCompositeMetadata{" + "metadataStore=" + metadataStore + '}';
  }
}
