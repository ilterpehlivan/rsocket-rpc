package io.rsocket.rpc.core.extension;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.rsocket.rpc.core.extension.metadata.MapBasedTracingMetadata;
import io.rsocket.rpc.core.extension.metadata.MetaDataUtil;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MetadataTest {

  @Test
  public void testAddingTracing2Metas() {
    ByteBuf encodeRpcComposite =
        MetaDataUtil.encodeRpcComposite("testservice", "testMethod", Unpooled.EMPTY_BUFFER);
    Map<String, String> tracingContextMap = new HashMap<>();
    tracingContextMap.put("testContext", "12345");
    MapBasedTracingMetadata map = new MapBasedTracingMetadata(tracingContextMap);
    ByteBuf addTracing2RpcComposite = MetaDataUtil.addTracing2RpcComposite(encodeRpcComposite, map);

    MetaDataUtil.printRpcCompositeMetadata(addTracing2RpcComposite);
    ReferenceCountUtil.safeRelease(addTracing2RpcComposite);
  }
}
