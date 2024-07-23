package com.nexia.base.player;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class SavedPlayerData {
    private final Data data;
    public SavedPlayerData() {
        this.data = new Data();
    }

    public void incrementInteger(String name) {
        Integer val = get(Integer.class, name);
        set(Integer.class, name, val + 1);
    }

    public void decrementInteger(String name) {
        Integer val = get(Integer.class, name);
        set(Integer.class, name, val - 1);
    }

    public <T> void set(Class<T> type, String name, T value) {
        data.append(type, name, value, null);
    }

    public <T> void buildField(Class<T> type, String name, T value, Class<? extends SavedPlayerData> thisClass) throws NoSuchFieldException {
        data.append(type, name, value, thisClass.getField(name));
    }

    public <T> T get(Class<T> type, String name) {
        return data.retrieve(type, name);
    }
    public class Data {
        private final Map<Class<?>, TypedData<?>> storedData = new HashMap<>();
        public Data() {

        }
        public <T> void append(Class<T> type, String name, T value, Field field) {
            TypedData<T> typeData;
            if (!storedData.containsKey(type)) typeData = new TypedData<>();
            else typeData = (TypedData<T>) storedData.get(type);
            if (typeData.linkedFields.containsKey(name)) {
                try {
                    typeData.linkedFields.get(name).set(SavedPlayerData.this, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (field != null)
                typeData.linkedFields.put(name, field);
            typeData.storedData.put(name, value);
            storedData.put(type, typeData);
        }
        public <T> T retrieve(Class<T> type, String name) {
            TypedData<T> typeData;
            if (!storedData.containsKey(type)) typeData = new TypedData<>();
            else typeData = (TypedData<T>) storedData.get(type);
            if (typeData.storedData.containsKey(name))
                return typeData.storedData.get(name);
            return null;
        }
    }
    public static class TypedData<T> {
        private final Map<String, T> storedData = new HashMap<>();
        private final Map<String, Field> linkedFields = new HashMap<>();
        public TypedData() {

        }
    }
}