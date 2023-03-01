package net.lamgc.jsonrpc;

public final class JsonRpcConst {

    public final static String JSON_RPC_VERSION_FIELD = "jsonrpc";
    public final static String JSON_RPC_VERSION_VALUE = "2.0";

    public final static String JSON_RPC_REQUEST_METHOD_FIELD = "method";
    public final static String JSON_RPC_REQUEST_PARAMS_FIELD = "params";
    public final static String JSON_RPC_RESPONSE_RESULT_FIELD = "result";
    public final static String JSON_RPC_RESPONSE_ERROR_FIELD = "error";
    public final static String JSON_RPC_ERROR_CODE_FIELD = "code";
    public final static String JSON_RPC_ERROR_MESSAGE_FIELD = "message";
    public final static String JSON_RPC_ERROR_DATA_FIELD = "data";
    public final static String JSON_RPC_ID_FIELD = "id";

    private JsonRpcConst() {
        throw new RuntimeException("The class does not allow instantiation.");
    }

}
