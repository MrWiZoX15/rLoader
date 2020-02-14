package club.reaper.loader.accessor;

import java.lang.reflect.Field;

public class FieldAccess {

    private Field field;

    public FieldAccess(Class<?> target, String name) {
        try {
            this.field = target.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        this.field.setAccessible(true);
    }

    public <T> T read(Object instance) {
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> void set(Object instance, T value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}