package io.rsocket.rpc.core.extension.metadata;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.metadata.TaggingMetadata;
import io.rsocket.metadata.TaggingMetadataFlyweight;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapBasedTracingMetadata implements MetadataAware {

  private Map<String, String> tracingMap;

  public MapBasedTracingMetadata(Map<String, String> tracingContext) {
    this.tracingMap = tracingContext;
  }

  public MapBasedTracingMetadata() {
    tracingMap = new HashMap<>();
  }

  public Map<String, String> getTracingMap() {
    return tracingMap;
  }

  @Override
  public RSocketMimeType rsocketMimeType() {
    return RSocketMimeType.Tracing;
  }

  @Override
  public String getMimeType() {
    return RSocketMimeType.Tracing.getType();
  }

  @Override
  public ByteBuf getContent() {
    List<String> temp = new ArrayList<>();
    for (Map.Entry<String, String> entry : tracingMap.entrySet()) {
      temp.add(entry.getKey() + "=" + entry.getValue());
    }
    return TaggingMetadataFlyweight.createTaggingContent(ByteBufAllocator.DEFAULT, temp);
  }

  /**
   * format routing as "r:%s,s:%s" style
   *
   * @return data format
   */
  private String formatData() {
    return this.tracingMap.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("\n"));
  }

  @Override
  public String toString() {
    return formatData();
  }

  /**
   * parse data
   *
   * @param byteBuf byte buffer
   */
  public void load(ByteBuf byteBuf) {
    TaggingMetadata taggingMetadata =
        new TaggingMetadata(RSocketMimeType.MessageTags.getType(), byteBuf);
    taggingMetadata.forEach(
        pair -> {
          int start = pair.indexOf("=");
          String name = pair.substring(0, start);
          String value = pair.substring(start + 1);
          tracingMap.put(name, value);
        });
  }

  public static MapBasedTracingMetadata from(ByteBuf content) {
    MapBasedTracingMetadata temp = new MapBasedTracingMetadata();
    temp.load(content);
    return temp;
  }

}
