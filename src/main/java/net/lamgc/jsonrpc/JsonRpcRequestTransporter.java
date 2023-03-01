package net.lamgc.jsonrpc;

/**
 * JsonRpc 请求传输器.
 * <p>
 * 由于本依赖库不实现任何传输过程, 因此开发者在使用 {@link JsonRpcProxyGenerator} 时需要实现该接口,
 * 用于传递 JsonRpc 请求到服务端, 并从服务端接受 JsonRpc 响应后返回到 {@link JsonRpcProxyGenerator} 进行处理.
 */
public interface JsonRpcRequestTransporter {

    /**
     * 传输 JsonRpc 请求并接收服务端传回的 JsonRpc 响应.
     *
     * @param request 需要发送的 JsonRpc 请求对象.
     * @return 返回服务端传回的 JsonRpc 响应对象.
     * @throws Exception 当发生异常无法发送请求或接收请求时, 将抛出异常.
     */
    JsonRpcResponse transportRequest(JsonRpcRequest request) throws Exception;

}
