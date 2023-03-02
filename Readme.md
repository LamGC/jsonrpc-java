# jsonrpc-java

## 简介

本库仅用于实现 JsonRpc 的基本对象组成、序列化和便于使用所需的少量工具类，并不实现任何传输细节。

简单来讲，这个库只负责帮你序列化和反序列化 RPC 报文，至于怎么传输，就看你的喜好了。

> JSON-RPC 2.0 没有定义任何特定于传输的问题，因为传输和 RPC 是独立的。 ——Roland Koebler

你可以通过这个库将某个 JsonRpc请求序列化成符合规范的 JsonRpc 请求报文，
然后通过你实现的数据传输链路将报文传递给 RPC 服务端，然后通过本库将 JsonRpc 请求报文反序列化成一样的 JsonRpc 请求对象。  
反过来，你也可以通过本库将 JsonRpc 响应序列化，然后通过你实现的数据传输链路将报文传递给 RPC 客户端。

这么做可以确保 RPC 跟传输的独立性，让你无需担心规范问题的同时，可以自由选择使用何种传输链路传输 RPC 报文，
比如网络、蓝牙、二维码甚至是让人帮你传话！

本依赖库以确保按 [JsonRpc 2.0 规范](https://www.jsonrpc.org/specification) 进行开发，并使用足够的测试项目以确保符合规范。

## 安装

依赖库中有关序列化的操作基本使用了 Gson，这么做主要是作者的习惯（我是 Gson 爱好者 :P）。  
首先，引入依赖库作为你项目的依赖项：

```kotlin
dependencies {
    implementation("net.lamgc:jsonrpc-java:0.1.0-RC1")
}
```

或者用 Groovy 的方式导入：

```groovy
dependencies {
    implementation 'net.lamgc:jsonrpc-java:0.1.0-RC1'
}
```

Maven 的话可以这样：

```xml

<dependency>
    <groupId>net.lamgc</groupId>
    <artifactId>jsonrpc-java</artifactId>
    <version>0.1.0-RC1</version>
</dependency>
```

由于还没正式发布 1.0.0 版本, 因此你需要将我的 Gitea
服务器加入到你的项目仓库列表中，具体看这里：[net.lamgc:jsonrpc-java 软件包](https://git.lamgc.me/LamGC/-/packages/maven/net.lamgc-jsonrpc-java/0.1.0-rc1)

## 使用

创建 Gson，将 `JsonRpcRequestSerializer` 和 `JsonRpcResponseSerializer`
注册为 `JsonRpcRequest` 和 `JsonRpcResponse` 的类型适配器，然后启用 Null 序列化：

```java
Gson gson=new GsonBuilder()
        .registerTypeAdapter(JsonRpcRequest.class,new JsonRpcRequestSerializer())
        .registerTypeAdapter(JsonRpcResponse.class,new JsonRpcResponseSerializer())
        .serializeNulls()
        .create();

// 或者，使用依赖库提供的工具类快速创建一个, 
// 如果你需要定制, 只需要调用 gson.newGsonBuilder() 即可.
        Gson gson=JsonRpcUtils.createGsonForJsonRpc();
```

> 注意：如果不启用 Null 序列化，那么 JsonRpcResponse 将无法对错误的 JsonRpcRequest
> 生成正确的响应。（[相关规范](https://www.jsonrpc.org/specification#:~:text=If%20there%20was%20an%20error%20in%20detecting%20the%20id%20in%20the%20Request%20object%20(e.g.%20Parse%20error/Invalid%20Request)%2C%20it%20MUST%20be%20Null.)）

如果有需要，将你所需要的类型适配器也注册进去。

然后将 RPC 接口的实现绑定到一个 `JsonRpcExecutor` 中：

```java
JsonRpcExecutor rpcExecutor=new SimpleJsonRpcExecutor(new RemoteInterfaceImpl(),gson);
```

然后尝试接收一个请求，并传入 JsonRpcExecutor 进行处理，并返回给调用方：

```java
JsonRpcResponse response=rpcExecutor.execute(jsonRpcRequst);
        String json=gson.toJson(response);
// 然后传回给客户端.
        request.sendResponse(json);
```

## 许可证

```text
Copyright 2023 LamGC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

