package net.lamgc.jsonrpc.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;

public class GsonParameterSerializer extends AbstractParameterSerializer {

    private final Gson gson;

    public GsonParameterSerializer(Gson gson) {
        super(false);
        this.gson = gson;
    }

    public GsonParameterSerializer(Gson gson, boolean enableNamedParameter) {
        super(enableNamedParameter);
        this.gson = gson;
    }

    @Override
    protected JsonElement serializerParameter(Type expectType, Object value) {
        return gson.toJsonTree(value);
    }
}
