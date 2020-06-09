# what is rsocket-rpc ?
Rsocket based RPC for gradle based on [salesforce reactive-grpc] (https://github.com/salesforce/reactive-grpc) and [rsocket-rpc-java] (https://github.com/rsocket/rsocket-rpc-java)
It provides a layer on top of the Rsocket reactive protocol

## Add-on functionalities:
* Tracing based on the brave
* Metrics based on micrometer.io

## Limitations:
* TCP is the only supported transportation layer
* Proto3 is the supported communication data format
* StreamingRequestSingleResponse call model is currently not supported
* Gradle only building mechanism
* JDK 8

# Usage

## Building with Gradle

To use Rsocket-Rpc with the `protobuf-gradle-plugin`, add the rpc plugin to the protobuf `plugins` section. as below

First add following repositories
```groovy
repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
        maven { url "https://repo.spring.io/snapshot" }
        maven { url "https://repo.spring.io/milestone" }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ilterpehlivan/rsocket-rpc")
            credentials {
                username = project.findProperty("gpr.user") ?: System.getenv("GPR_USER")
                password = project.findProperty("gpr.read.key") ?: System.getenv("GPR_API_KEY")
            }
        }
    }
```

Note: Currently the rsocket-rpc packages are in github package repository and you need token to access the packages
Working on to move to maven central. If anybody wants to use ping me

```groovy
protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
            rpc {
                artifact = 'com.github.ilterpehlivan:rsocket-rpc-gen:${rsocketRpcVersion}'
            }
        }
    
        generateProtoTasks {
            all().each { task ->
                task.plugins {
                    rpc {}
                }
    
            }
        }
}
```

And add the following dependency: `"compile 'com.github.ilterpehlivan:rsocket-rpc-core-extension:${rsocketRpcVersion}'"`


## Implementation

Following is how to create a Server by using the builder;

```
  RpcServer server =
        RsocketServerBuilder.forPort(9090)
            .addService(new SimpleServiceImpl())
            .withMetrics(meterRegistry)
            .interceptor(RSocketTracing.create(tracing).newServerInterceptor())
            .build()
            .start();
    server.awaitTermination();
```

And client;

```
RsocketClientBuilder clientBuilder =
          RsocketClientBuilder.forAddress("localhost", 9090)
              .withMetrics(meterRegistry)
              .interceptor(RSocketTracing.create(tracing).newClientInterceptor());
      simpleServiceStub = RsocketSimpleServiceRpc.newReactorStub(clientBuilder);
```

finally calling a service

```
simpleServiceStub
    .requestReply(SimpleRequest.newBuilder()
                 .setRequestMessage("Hello Client").build())
    .block();
```

#Examples
Spring boot example
https://github.com/ilterpehlivan/rsocket-rpc-spring-boot-sample

