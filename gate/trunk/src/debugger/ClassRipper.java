package debugger;

import java.lang.reflect.Field;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * Contains helper static methods to access private class fields
 * @author Andrey Shafirin
 */

public class ClassRipper {
    private static int DebugLevel = 4;

    private ClassRipper() {
    }

    /** Obtains field from given class instance
     * regardless either this field private or not
     * @param classInstance class instance which value to extract
     * @param fieldName field name
     * @return fileld
     * */
    private static Field getField(Object classInstance, String fieldName) {
        if (DebugLevel >= 5) {
            System.out.print("DEBUG [" + ClassRipper.class.getName() + "]:");
            System.out.print(" getField: ");
            System.out.print(" classInstance = [" + classInstance.getClass().getName() + "]");
            System.out.print(" fieldName = [" + fieldName + "]");
            System.out.print("\n");
        }
        Field field = null;
        Class cls = classInstance.getClass();
        while (!cls.getName().equals("java.lang.Object")) {
            try {
                field = cls.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                // will try to search in superclass
                //cls = classInstance.getClass().getSuperclass();
                cls = cls.getSuperclass();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /** Obtains field value from given class instance
     * regardless either this field private or not
     * @param classInstance class instance which value to extract
     * @param fieldName field name
     * @return fileld value
     * */
    public static Object getFieldValue(Object classInstance, String fieldName) throws IllegalAccessException {
        Field field = getField(classInstance, fieldName);
        Object obj = field.get(classInstance);
        field.setAccessible(false);
        return obj;
    }

    public static boolean getBoolean(Object classInstance, String fieldName) throws IllegalAccessException {
        Field field = getField(classInstance, fieldName);
        boolean b = field.getBoolean(classInstance);
        field.setAccessible(false);
        return b;
    }

    public static byte getByte(Object classInstance, String fieldName) throws IllegalAccessException {
        Field field = getField(classInstance, fieldName);
        byte b = field.getByte(classInstance);
        field.setAccessible(false);
        return b;
    }

    public static char getChar(Object classInstance, String fieldName) throws IllegalAccessException {
        Field field = getField(classInstance, fieldName);
        char c = field.getChar(classInstance);
        field.setAccessible(false);
        return c;
    }

    public static int getInt(Object classInstance, String fieldName) throws IllegalAccessException {
        Field field = getField(classInstance, fieldName);
        int i = field.getInt(classInstance);
        field.setAccessible(false);
        return i;
    }

    public static float getFloat(Object classInstance, String fieldName) throws IllegalAccessException {
        Field field = getField(classInstance, fieldName);
        float f = field.getFloat(classInstance);
        field.setAccessible(false);
        return f;
    }

    public static long getLong(Object classInstance, String fieldName) throws IllegalAccessException {
        Field field = getField(classInstance, fieldName);
        long l = field.getLong(classInstance);
        field.setAccessible(false);
        return l;
    }

    public static double getDouble(Object classInstance, String fieldName) throws IllegalAccessException {
        Field field = getField(classInstance, fieldName);
        double d = field.getDouble(classInstance);
        field.setAccessible(false);
        return d;
    }
}
