package com.nexia.base.player;

import java.lang.reflect.Field;

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
        try {
            data.append(type, name, value, getClass().getField(name), this);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T get(Class<T> type, String name) {
        try {
            return data.retrieve(type, name, getClass().getField(name), this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    public static class Data {
        public Data() {

        }
        public <T> void append(Class<T> type, String name, T value, Field field, SavedPlayerData data) {
            if (field != null) {
                try {
                    field.set(data, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else throw new NullPointerException("The field " + name + " in " + data.getClass().getSimpleName() + " must not be null!");
        }
        public <T> T retrieve(Class<T> type, String name, Field field, SavedPlayerData data) throws IllegalAccessException, NoSuchFieldException {
            if (field != null) {
                var val = field.get(data);
                if (type.isInstance(val))
                    return (T) val;
                else throw new ClassCastException("The field " + name + " in " + data.getClass().getSimpleName() + " is not a " + type.getSimpleName());
            } else throw new NullPointerException("The field " + name + " in " + data.getClass().getSimpleName() + " must not be null!");
        }
    }
}