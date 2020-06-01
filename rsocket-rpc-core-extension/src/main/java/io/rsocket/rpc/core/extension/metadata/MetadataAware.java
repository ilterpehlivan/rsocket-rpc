package io.rsocket.rpc.core.extension.metadata;

import io.netty.buffer.ByteBuf;
import io.rsocket.metadata.CompositeMetadata;
import reactor.util.annotation.Nullable;

public interface MetadataAware extends CompositeMetadata.Entry {
  /**
   * metadata mime name
   *
   * @return RSocket MIME type
   */
  RSocketMimeType rsocketMimeType();

  @Nullable
  String getMimeType();

  ByteBuf getContent();

  /**
   * load metadata from byte buffer
   *
   * @param byteBuf byte buf
   * @throws Exception exception
   */
  void load(ByteBuf byteBuf) throws Exception;

}