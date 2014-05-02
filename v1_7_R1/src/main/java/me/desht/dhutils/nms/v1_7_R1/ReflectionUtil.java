package me.desht.dhutils.nms.v1_7_R1;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionUtil {
    public static void setProtectedValue(Object o, String field, Object newValue) {
        setProtectedValue(o.getClass(), o, field, newValue);
    }

    public static void setProtectedValue(Class c, String field, Object newValue) {
        setProtectedValue(c, null, field, newValue);
    }

    public static void setProtectedValue(Class c, Object o, String field, Object newValue) {
        try {

            Field f = c.getDeclaredField(field);

            f.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

            f.set(o, newValue);
        } catch (NoSuchFieldException ex) {
            System.out.println("*** " + c.getName() + ":" + ex);
        } catch (IllegalAccessException ex) {
            System.out.println("*** " + c.getName() + ":" + ex);
        }
    }

    public static <T> T getProtectedValue(Object obj, String fieldName) {
        try {
            Class c = obj.getClass();
            while(c != Object.class) {
                Field[] fields = c.getDeclaredFields();
                for(Field f : fields) {
                    if (f.getName() == fieldName) {
                        f.setAccessible(true);
                        return (T) f.get(obj);
                    }
                }
                c = c.getSuperclass();
            }
            System.out.println("*** " + obj.getClass().getName() + ":No such field");
            return null;
        } catch (Exception ex) {
            System.out.println("*** " + obj.getClass().getName() + ":" + ex);
            return null;
        }
    }

    public static <T> T getProtectedValue(Class c, String field) {
        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            return (T) f.get(c);
        } catch (Exception ex) {
            System.out.println("*** " + c.getName() + ":" + ex);
            return null;
        }
    }

    public static Object invokeProtectedMethod(Class c, String method, Object... args) {
        return invokeProtectedMethod(c, null, method, args);
    }

    public static Object invokeProtectedMethod(Object o, String method, Object... args) {
        return invokeProtectedMethod(o.getClass(), o, method, args);
    }

    public static Object invokeProtectedMethod(Class c, Object o, String method, Object... args) {
        try {
            Class[] pTypes = new Class[args.length];
            for(int i = 0; i < args.length; i++) {
                if (args[i] instanceof Integer) {
                    pTypes[i] = int.class;
                } else {
                    pTypes[i] = args[i].getClass();
                }
            }

            Method m = c.getDeclaredMethod(method, pTypes);
            m.setAccessible(true);
            return m.invoke(o, args);
        }
        catch (InvocationTargetException ex) {
            System.out.println("*** " + c.getName() + "." + method + "(): " + ex.getTargetException());
            return null;
        }
        catch (Exception ex) {
            System.out.println("*** " + c.getName() + "." + method + "(): " + ex);
            return null;
        }
    }
}

