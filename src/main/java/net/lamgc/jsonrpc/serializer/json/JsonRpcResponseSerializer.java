package net.lamgc.jsonrpc.serializer.json;

import com.google.gson.*;
import net.lamgc.jsonrpc.JsonRpcError;
import net.lamgc.jsonrpc.JsonRpcResponse;

import java.lang.reflect.Type;

import static net.lamgc.jsonrpc.JsonRpcConst.*;

/**
 * JsonRpcResponse 序列化器.
 * <p>
 * 该序列化器用于序列化 {@link JsonRpcResponse}.
 * <p>
 * Gson 必须启用对 null 的序列化({@link GsonBuilder#serializeNulls()}),
 * 否则将无法正确地序列化对错误请求的响应 (会缺少 id 字段).
 * <p>
 * 关于 RPC 通知:
 * 该序列化器不阻止序列化无 id 字段的响应对象,
 * 但开发者需要清楚: JsonRpc 规范要求服务端不得回复无 id 请求.
 * <p>
 * 对于 id 为 null 的响应对象, 序列化器将不会在 Json 对象中添加 id 字段.
 */
public class JsonRpcResponseSerializer implements JsonSerializer<JsonRpcResponse>, JsonDeserializer<JsonRpcResponse> {

    @Override
    public JsonElement serialize(JsonRpcResponse src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty(JSON_RPC_VERSION_FIELD, JSON_RPC_VERSION_VALUE);
        if (src.getResult() == null) {
            responseJson.add(JSON_RPC_RESPONSE_RESULT_FIELD, JsonNull.INSTANCE);
        } else if (src.getResult() instanceof JsonRpcError) {
            responseJson.add(JSON_RPC_RESPONSE_ERROR_FIELD, context.serialize(src.getResult()));
        } else {
            responseJson.add(JSON_RPC_RESPONSE_RESULT_FIELD, context.serialize(src.getResult()));
        }
        if (src.getId() != null) {
            if (src.getId().isJsonNull()) {
                responseJson.add(JSON_RPC_ID_FIELD, JsonNull.INSTANCE);
            } else if (src.getId().isJsonPrimitive()) {
                responseJson.add(JSON_RPC_ID_FIELD, src.getId());
            } else {
                throw new IllegalArgumentException("The id field does not support the structure type (JsonArray or JsonObject).");
            }
        }

        return responseJson;
    }

    @Override
    public JsonRpcResponse deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Not a JsonObject.");
        }
        JsonObject jsonObject = json.getAsJsonObject();

        if (jsonObject.has(JSON_RPC_VERSION_FIELD)) {
            JsonElement jsonrpcElement = jsonObject.get(JSON_RPC_VERSION_FIELD);
            if (!jsonrpcElement.isJsonPrimitive()) {
                throw new JsonParseException("jsonrpc is not a JsonPrimitive.");
            }
            JsonPrimitive jsonrpcPrimitive = jsonrpcElement.getAsJsonPrimitive();
            if (!jsonrpcPrimitive.isString()) {
                throw new JsonParseException("jsonrpc is not a String.");
            }
            String jsonrpc = jsonrpcPrimitive.getAsString();
            if (!jsonrpc.equals(JSON_RPC_VERSION_VALUE)) {
                throw new JsonParseException("jsonrpc is not 2.0.");
            }
        } else {
            throw new JsonParseException("jsonrpc is not exist.");
        }

        JsonElement id;
        if (!jsonObject.has(JSON_RPC_ID_FIELD)) {
            throw new JsonParseException("The response must contain an `id` field.");
        }
        id = jsonObject.get(JSON_RPC_ID_FIELD);
        if (id.isJsonPrimitive()) {
            JsonPrimitive idPrimitive = id.getAsJsonPrimitive();
            if (!idPrimitive.isNumber() && !idPrimitive.isString()) {
                throw new JsonParseException("The `id` field must be a String or Number.");
            }
        } else if (!id.isJsonNull()) {
            throw new JsonParseException("The id field does not support the structure type (JsonArray or JsonObject).");
        }

        Object result;
        if (jsonObject.has(JSON_RPC_RESPONSE_RESULT_FIELD)
                && jsonObject.has(JSON_RPC_RESPONSE_ERROR_FIELD)) {
            throw new JsonParseException("The response contains both result and error fields.");
        } else if (!jsonObject.has(JSON_RPC_RESPONSE_RESULT_FIELD)
                && !jsonObject.has(JSON_RPC_RESPONSE_ERROR_FIELD)) {
            throw new JsonParseException("The response does not contain a result or error field.");
        } else if (jsonObject.has(JSON_RPC_RESPONSE_RESULT_FIELD)) {
            result = jsonObject.get(JSON_RPC_RESPONSE_RESULT_FIELD);
        } else {
            JsonElement errorElement = jsonObject.get(JSON_RPC_RESPONSE_ERROR_FIELD);
            if (!errorElement.isJsonObject()) {
                throw new JsonParseException("error is not a JsonObject.");
            }
            result = context.deserialize(errorElement, JsonRpcError.class);
        }

        return new JsonRpcResponse(result, id);
    }

}
