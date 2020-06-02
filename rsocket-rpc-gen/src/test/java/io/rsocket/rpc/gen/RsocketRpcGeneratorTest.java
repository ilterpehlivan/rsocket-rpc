package io.rsocket.rpc.gen;

import com.salesforce.jprotoc.ProtocPlugin;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RsocketRpcGeneratorTest {

  private static final String DUMMY_PATH = "/resources/dummy/descriptor_dump";

  @Test
  @Ignore
  //TODO:fix the path
  public void generateCodeBasedonDummyHellow() {
    ProtocPlugin.debug(new RsocketRpcGenerator(), DUMMY_PATH);
  }
}
