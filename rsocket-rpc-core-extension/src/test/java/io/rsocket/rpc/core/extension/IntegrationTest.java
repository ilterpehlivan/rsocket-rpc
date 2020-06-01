package io.rsocket.rpc.core.extension;

import brave.Tracing;
import io.rsocket.rpc.core.extension.RsocketClientBuilder.Client;
import io.rsocket.rpc.core.extension.tracing.RSocketTracing;
import io.rsocket.rpc.extension.sample.SimpleServiceClient;
import io.rsocket.rpc.extension.sample.SimpleServiceImpl;
import io.rsocket.rpc.extension.sample.proto.SimpleRequest;
import org.junit.Before;
import org.junit.Test;

public class IntegrationTest {

  Tracing tracing;

  @Before
  public void setUp() throws Exception {
    tracing = Tracing.newBuilder().localServiceName("test-service").build();
    RpcServer server =
        RsocketServerBuilder.forPort(9090)
            .addService(new SimpleServiceImpl())
            .interceptor(RSocketTracing.create(tracing).newServerInterceptor())
            .build()
            .start();

    System.out.println("server started in port 9090..thread:" + Thread.currentThread().getName());
    server.awaitTermination();
  }

  @Test
  public void shouldRequestResponseAndValidate() throws InterruptedException {
    System.out.println("starting the test");
    Client simpleClient = RsocketClientBuilder.forAddress("localhost", 9090).build();
    SimpleServiceClient simpleServiceClient = SimpleServiceClient.newReactiveStub(simpleClient);
    simpleServiceClient
        .requestReply(SimpleRequest.newBuilder().setRequestMessage("hello").build())
        .doOnNext(r -> System.out.println("response received:" + r.getResponseMessage()))
        .block();
  }

  @Test
  public void shouldRequestResponseWithLoadBalancingAndValidate() {
    System.out.println("starting the test");
    Client simpleClient =
        RsocketClientBuilder.forAddress("localhost", 9090).withLoadBalancing().build();
    SimpleServiceClient simpleServiceClient = SimpleServiceClient.newReactiveStub(simpleClient);
    simpleServiceClient
        .requestReply(SimpleRequest.newBuilder().setRequestMessage("hello").build())
        .doOnNext(r -> System.out.println("response received:" + r.getResponseMessage()))
        .block();
  }

  @Test
  public void shouldRequestResponseWithTracingAndValidate() {
    System.out.println("starting the test");
    Client simpleClient =
        RsocketClientBuilder.forAddress("localhost", 9090)
            .withLoadBalancing()
            .interceptor(RSocketTracing.create(tracing).newClientInterceptor())
            .build();

    SimpleServiceClient simpleServiceClient = SimpleServiceClient.newReactiveStub(simpleClient);
    simpleServiceClient
        .requestReply(SimpleRequest.newBuilder().setRequestMessage("hello").build())
        .doOnNext(r -> System.out.println("response received:" + r.getResponseMessage()))
        .block();
  }
}
