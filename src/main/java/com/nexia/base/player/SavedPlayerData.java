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

    public <T> void buildField(Class<T> type, String name, T value) throws NoSuchFieldException {
        data.append(type, name, value, getClass().getField(name));
    }

    public <T> T get(Class<T> type, String name) {
        try {
            return data.retrieve(type, name);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public class Data {
        private final Map<Class<?>, TypedData> storedData = new HashMap<>();
        public Data() {

        }
        public <T> void append(Class<T> type, String name, T value, Field field) {
            TypedData typeData;
            if (!storedData.containsKey(type)) typeData = new TypedData();
            else typeData = storedData.get(type);
            if (typeData.linkedFields.containsKey(name)) {
                try {
                    typeData.linkedFields.get(name).set(SavedPlayerData.this, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (field != null) {
                try {
                    field.set(SavedPlayerData.this, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                typeData.linkedFields.put(name, field);
            } else throw new NullPointerException("The field " + name + " in " + SavedPlayerData.this.getClass().getSimpleName() + " must not be null!");
            storedData.put(type, typeData);
        }
        public <T> T retrieve(Class<T> type, String name) throws IllegalAccessException {
            TypedData typeData;
            if (!storedData.containsKey(type)) typeData = new TypedData();
            else typeData = storedData.get(type);
            if (typeData.linkedFields.containsKey(name))
                return (T) typeData.linkedFields.get(name).get(SavedPlayerData.this);
            return null;
        }
    }
    public static class TypedData {
        private final Map<String, Field> linkedFields = new HashMap<>();
        public TypedData() {

        }
    }
}