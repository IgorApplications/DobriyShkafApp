package com.iapp.iapp_messenger.test_client;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;


/**
 * field.setAccessible(true)
 * Java 17+ запрещает это для JDK internal classes.
 * */
public class GsonFactory {

    public static Gson create() {

        return new GsonBuilder()

                .registerTypeAdapter(LocalDate.class,
                        new JsonSerializer<LocalDate>() {
                            @Override
                            public JsonElement serialize(LocalDate src,
                                                         Type typeOfSrc,
                                                         JsonSerializationContext context) {

                                return new JsonPrimitive(src.toString());
                            }
                        })

                .registerTypeAdapter(LocalDate.class,
                        new JsonDeserializer<LocalDate>() {
                            @Override
                            public LocalDate deserialize(JsonElement json,
                                                         Type typeOfT,
                                                         JsonDeserializationContext context)
                                    throws JsonParseException {

                                return LocalDate.parse(json.getAsString());
                            }
                        })

                .registerTypeAdapter(Instant.class,
                        new JsonSerializer<Instant>() {
                            @Override
                            public JsonElement serialize(Instant src,
                                                         Type typeOfSrc,
                                                         JsonSerializationContext context) {

                                return new JsonPrimitive(src.toString());
                            }
                        })

                .registerTypeAdapter(Instant.class,
                        new JsonDeserializer<Instant>() {
                            @Override
                            public Instant deserialize(JsonElement json,
                                                       Type typeOfT,
                                                       JsonDeserializationContext context)
                                    throws JsonParseException {

                                return Instant.parse(json.getAsString());
                            }
                        })

                .create();
    }
}