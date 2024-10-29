package com.example.spring_boot.net;

import com.example.spring_boot.model.InstantSerializer;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ApiRequestParamsConverter {
    private static final Gson GSON =
            new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapterFactory(new HasEmptyEnumTypeAdapterFactory())
                    .registerTypeAdapterFactory(new NullValuesInMapsTypeAdapterFactory())
                    .registerTypeAdapter(Instant.class, new InstantSerializer())
                    .create();



    private static class HasEmptyEnumTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!ApiRequestParams.EnumParam.class.isAssignableFrom(type.getRawType())) {
                return null;
            }

            TypeAdapter<ApiRequestParams.EnumParam> paramEnum =
                    new TypeAdapter<ApiRequestParams.EnumParam>() {
                        @Override
                        public void write(JsonWriter out, ApiRequestParams.EnumParam value) throws IOException {
                            if (value.getValue().equals("")) {
                                // need to restore serialize null setting
                                // not to affect other fields
                                boolean previousSetting = out.getSerializeNulls();
                                out.setSerializeNulls(true);
                                out.nullValue();
                                out.setSerializeNulls(previousSetting);
                            } else {
                                out.value(value.getValue());
                            }
                        }

                        @Override
                        public ApiRequestParams.EnumParam read(JsonReader in) {
                            throw new UnsupportedOperationException(
                                    "No deserialization is expected from this private type adapter for enum param.");
                        }
                    };
            return (TypeAdapter<T>) paramEnum.nullSafe();
        }
    }

    private static class NullValuesInMapsTypeAdapterFactory implements TypeAdapterFactory {
        TypeAdapter<?> getValueAdapter(Gson gson, TypeToken<?> type) {
            Type valueType;
            if (type.getType() instanceof ParameterizedType) {
                ParameterizedType mapParameterizedType = (ParameterizedType) type.getType();
                valueType = mapParameterizedType.getActualTypeArguments()[1];
            } else {
                valueType = Object.class;
            }

            return gson.getAdapter(TypeToken.get(valueType));
        }

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!Map.class.isAssignableFrom(type.getRawType())) {
                return null;
            }

            final TypeAdapter<?> valueAdapter = getValueAdapter(gson, type);
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            final TypeAdapter<T> typeAdapter = new MapAdapter(valueAdapter, delegate);

            return typeAdapter.nullSafe();
        }
    }

    private static class MapAdapter<V> extends TypeAdapter<Map<String, V>> {
        private TypeAdapter<V> valueTypeAdapter;
        private TypeAdapter<Map<String, V>> mapTypeAdapter;

        public MapAdapter(TypeAdapter<V> valueTypeAdapter, TypeAdapter<Map<String, V>> mapTypeAdapter) {
            this.valueTypeAdapter = valueTypeAdapter;
            this.mapTypeAdapter = mapTypeAdapter;
        }

        @Override
        public void write(JsonWriter out, Map<String, V> value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginObject();
            for (Map.Entry<String, V> entry : value.entrySet()) {
                out.name(entry.getKey());
                V entryValue = entry.getValue();
                if (entryValue == null) {
                    boolean oldSerializeNullsValue = out.getSerializeNulls();
                    try {
                        out.setSerializeNulls(true);
                        out.nullValue();
                    } finally {
                        out.setSerializeNulls(oldSerializeNullsValue);
                    }
                } else {
                    valueTypeAdapter.write(out, entryValue);
                }
            }
            out.endObject();
        }

        @Override
        public Map<String, V> read(JsonReader in) throws IOException {
            return mapTypeAdapter.read(in);
        }
    }


    Map<String, Object> convert(ApiRequestParams apiRequestParams) {
        JsonObject jsonParams = GSON.toJsonTree(apiRequestParams).getAsJsonObject();
        return new Gson().fromJson(jsonParams, Map.class);
    }
}
