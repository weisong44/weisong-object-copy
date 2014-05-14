package com.weisong.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

import com.weisong.common.data.DataObject;
import com.weisong.common.value.ValueObject;

public class ReflectionUtil {

    final private static Set<Class<?>> PRIMITIVE_TYPES;
    final private static Set<Class<?>> PRIMITIVE_WRAPPER_TYPES;
    final private static Set<Class<?>> PRIMITIVE_ARRAY_TYPES;
    final private static Set<Class<?>> PRIMITIVE_WRAPPER_ARRAY_TYPES;

    static {
        PRIMITIVE_TYPES = new HashSet<Class<?>>(8);
        PRIMITIVE_TYPES.add(Boolean.TYPE);
        PRIMITIVE_TYPES.add(Character.TYPE);
        PRIMITIVE_TYPES.add(Byte.TYPE);
        PRIMITIVE_TYPES.add(Short.TYPE);
        PRIMITIVE_TYPES.add(Integer.TYPE);
        PRIMITIVE_TYPES.add(Long.TYPE);
        PRIMITIVE_TYPES.add(Float.TYPE);
        PRIMITIVE_TYPES.add(Double.TYPE);
        PRIMITIVE_TYPES.add(Void.TYPE);

        PRIMITIVE_WRAPPER_TYPES = new HashSet<Class<?>>(8);
        PRIMITIVE_WRAPPER_TYPES.add(Boolean.class);
        PRIMITIVE_WRAPPER_TYPES.add(Character.class);
        PRIMITIVE_WRAPPER_TYPES.add(Byte.class);
        PRIMITIVE_WRAPPER_TYPES.add(Short.class);
        PRIMITIVE_WRAPPER_TYPES.add(Integer.class);
        PRIMITIVE_WRAPPER_TYPES.add(Long.class);
        PRIMITIVE_WRAPPER_TYPES.add(Float.class);
        PRIMITIVE_WRAPPER_TYPES.add(Double.class);
        PRIMITIVE_WRAPPER_TYPES.add(Void.class);

        PRIMITIVE_ARRAY_TYPES = new HashSet<Class<?>>(8);
        PRIMITIVE_ARRAY_TYPES.add(boolean[].class);
        PRIMITIVE_ARRAY_TYPES.add(char[].class);
        PRIMITIVE_ARRAY_TYPES.add(byte[].class);
        PRIMITIVE_ARRAY_TYPES.add(short[].class);
        PRIMITIVE_ARRAY_TYPES.add(int[].class);
        PRIMITIVE_ARRAY_TYPES.add(long[].class);
        PRIMITIVE_ARRAY_TYPES.add(float[].class);
        PRIMITIVE_ARRAY_TYPES.add(double[].class);

        PRIMITIVE_WRAPPER_ARRAY_TYPES = new HashSet<Class<?>>(8);
        PRIMITIVE_WRAPPER_ARRAY_TYPES.add(Boolean[].class);
        PRIMITIVE_WRAPPER_ARRAY_TYPES.add(Character[].class);
        PRIMITIVE_WRAPPER_ARRAY_TYPES.add(Byte[].class);
        PRIMITIVE_WRAPPER_ARRAY_TYPES.add(Short[].class);
        PRIMITIVE_WRAPPER_ARRAY_TYPES.add(Integer[].class);
        PRIMITIVE_WRAPPER_ARRAY_TYPES.add(Long[].class);
        PRIMITIVE_WRAPPER_ARRAY_TYPES.add(Float[].class);
        PRIMITIVE_WRAPPER_ARRAY_TYPES.add(Double[].class);
    }

    public static List<Field> getAllFields(Class<?> clazz) {

        if (clazz == null)
            return Collections.emptyList();

        Field[] fields = clazz.getDeclaredFields();
        List<Field> list = new LinkedList<Field>();
        list.addAll(Arrays.asList(fields));

        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != Object.class) {
            list.addAll(getAllFields(superClazz));
        }

        return list;
    }

    static public Map<String, Field> getAllFieldsAsMap(Class<?> clazz) {

        if (clazz == null)
            return Collections.emptyMap();

        Field[] fields = clazz.getDeclaredFields();
        Map<String, Field> map = new HashMap<String, Field>();
        for (Field f : fields) {
            map.put(f.getName(), f);
        }

        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != Object.class) {
            map.putAll(getAllFieldsAsMap(superClazz));
        }

        return map;
    }

    static public Object invokeGet(Object o, String getMethodName) throws Exception {
        Method getter = getMethod(o.getClass(), getMethodName, null);
        if (getter == null) {
            throw new Exception(String.format("Method %s() not found in class %s", getMethodName, o.getClass()
                    .getName()));
        }

        return invokeGet(o, getter);
    }

    static public Object invokeGet(Object o, Method getter) throws Exception {
        return invokeMethod(o, getter, null);
    }

    static void invokeSet(Object o, Method setter, Object param) throws Exception {
        try {
            invokeMethod(o, setter, new Object[] { param });
        }
        catch (Exception ex) {
            throw new Exception(String.format("Failed to invoke method %s.%s(%s)", o.getClass().getSimpleName(),
                    setter.getName(), param != null ? param.getClass().getName() : ""), ex);
        }
    }

    static public Object invokeMethod(Object o, Method m, Object[] params) throws Exception {
        Assert.notNull(o, "Object can not be null");
        try {
            return m.invoke(o, params);
        }
        catch (InvocationTargetException ex) {

            Throwable theEx = ex;

            if (ex.getTargetException() != null) {
                theEx = ex.getTargetException();
                if (theEx instanceof Exception) {
                    throw (Exception) theEx;
                }
            }

            throw new Exception(String.format("Failed to invoke method %s.%s()", o.getClass().getSimpleName(),
                    m.getName()), theEx);

        }
        catch (IllegalAccessException ex) {

            Throwable theEx = ex;
            if (ex.getCause() != null) {
                theEx = ex.getCause();
                if (theEx instanceof Exception) {
                    throw (Exception) theEx;
                }
            }

            throw new Exception(String.format("Failed to invoke method %s.%s()", o.getClass().getSimpleName(),
                    m.getName()), theEx);
        }
    }

    static public Object invokeMethod(Object o, String methodName) throws Exception {
        return invokeMethod(o, methodName, null);
    }

    static public Object invokeMethod(Object o, String methodName, Object[] params) throws Exception {

        Class<?>[] paramDef = null;
        if (params != null) {
            paramDef = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                paramDef[i] = params[i].getClass();
            }
        }

        Method m = getMethodIncludeSuperclass(o.getClass(), methodName, paramDef, true);
        if (m == null) {
            throw new Exception(String.format("Method not found: %s.%s()", o.getClass().getSimpleName(), methodName));
        }

        boolean originalAccessibility = m.isAccessible();
        try {
            m.setAccessible(true);
            return invokeMethod(o, m, params);
        }
        finally {
            m.setAccessible(originalAccessibility);
        }
    }

    static public Object invokeMethod(Object o, String methodName, Class<?>[] paramDefs, Object[] params)
            throws Exception {

        Method m = getMethodIncludeSuperclass(o.getClass(), methodName, paramDefs, true);
        if (m == null) {
            String errMsg = String.format("Method not found: %s.%s()", o.getClass().getSimpleName(), methodName);
            if (paramDefs != null) {
                for (int i = 0; i < paramDefs.length; i++) {
                    errMsg += "\n param " + i + ": " + paramDefs[i].getName();
                }
            }
            throw new Exception(errMsg);
        }

        boolean originalAccessibility = m.isAccessible();
        try {
            m.setAccessible(true);
            return invokeMethod(o, m, params);
        }
        finally {
            m.setAccessible(originalAccessibility);
        }
    }

    static public Method getMethod(Object o, String methodName, Class<?>[] args) {
        return getMethod(o.getClass(), methodName, args);
    }

    static public Method getMethod(Class<?> clazz, String methodName, Class<?>[] args) {
        return getMethod(clazz, methodName, args, false);
    }

    static public Method getMethodIncludeSuperclass(Class<?> clazz, String methodName, Class<?>[] args,
            boolean includePrivateMethods) {
        Class<?> curClass = clazz;
        while (curClass != Object.class) {
            Method m = getMethod(curClass, methodName, args, true);
            if (m != null) {
                return m;
            }
            else {
                curClass = curClass.getSuperclass();
            }
        }
        return null;
    }

    static public Method getMethod(Class<?> clazz, String methodName, Class<?>[] args, boolean includePrivateMethods) {
        Method[] methods = null;

        // with Class.getDeclaredMethods(), private methods of the class are
        // also returned.
        // but the inherited methods are not returned which we get with
        // getMethods()
        if (includePrivateMethods)
            methods = clazz.getDeclaredMethods();
        else
            methods = clazz.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName) == false)
                continue;

            Class<?>[] declaredArgs = methods[i].getParameterTypes();
            if (declaredArgs == null || declaredArgs.length == 0) {
                if (args == null)
                    return methods[i];
                continue;
            }

            if (args == null || declaredArgs.length != args.length)
                continue;

            boolean allArgsAssignable = true;
            for (int j = 0; j < declaredArgs.length; j++) {
                if (declaredArgs[j].isAssignableFrom(args[j]) == false) {
                    allArgsAssignable = false;
                    break;
                }
            }

            if (allArgsAssignable)
                return methods[i];
        }

        return null;
    }

    static public List<Field> getFieldsOfType(Class<?> clazz, Class<?> type) {

        if (clazz == null)
            return null;

        List<Field> result = new LinkedList<Field>();
        for (Field f : getAllFields(clazz)) {
            if (type.isAssignableFrom(f.getType())) {
                result.add(f);
            }
        }

        return result;
    }

    static public Field getField(Class<?> clazz, String name) {

        if (clazz == null)
            return null;

        Field field;
        try {
            field = clazz.getDeclaredField(name);
            if (field != null)
                return field;
        }
        catch (Exception e) {
            // Ignore
        }

        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != Object.class) {
            field = getField(superClazz, name);
            if (field != null)
                return field;
        }

        return null;
    }

    static public Object getFieldValue(Object o, String f) {
        o = CommonUtil.unboxProxy(o);
        Field field = getField(o.getClass(), f);
        try {
            field.setAccessible(true);
            return CommonUtil.unboxProxy(field.get(o));
        }
        catch (Exception e) {
            return null;
        }
        finally {
            if (field != null)
                field.setAccessible(false);
        }
    }

    static public void setFieldValue(Object o, String strField, Object value) throws Exception {
        Field field = getField(o.getClass(), strField);
        if (field == null) {
            throw new Exception(String.format("Field not found: %s.%s", o.getClass().getSimpleName(), strField));
        }
        setFieldValue(o, field, value);
    }

    static public void setFieldValue(Object o, Field field, Object value) throws Exception {

        try {
            field.setAccessible(true);
            if (value == null) {
                field.set(o, null);
                return;
            }

            String convertedValue;
            if (value instanceof byte[]) {
                convertedValue = new String((byte[]) value, "UTF-8");
            }
            else if (value instanceof String) {
                convertedValue = (String) value;
            }
            else {
                convertedValue = value.toString();
            }

            if (field.getType() == int.class) {
                field.setInt(o, Integer.parseInt(convertedValue));
            }
            else if (field.getType() == long.class) {
                field.setLong(o, Long.parseLong(convertedValue));
            }
            else if (field.getType() == float.class) {
                field.setFloat(o, Float.parseFloat(convertedValue));
            }
            else if (field.getType() == double.class) {
                field.setDouble(o, Double.parseDouble(convertedValue));
            }
            else if (field.getType() == boolean.class) {
                field.set(o, Boolean.parseBoolean(convertedValue));
            }
            else {
                if (field.getType() == Integer.class) {
                    value = Integer.valueOf(convertedValue);
                    field.set(o, value);
                }
                else if (field.getType() == Long.class) {
                    value = Long.valueOf(convertedValue);
                    field.set(o, value);
                }
                else if (field.getType() == BigInteger.class) {
                    value = BigInteger.valueOf(Long.valueOf(convertedValue));
                    field.set(o, value);
                }
                else if (field.getType() == Float.class) {
                    value = Float.valueOf(convertedValue);
                    field.set(o, value);
                }
                else if (field.getType() == Double.class) {
                    value = Double.valueOf(convertedValue);
                    field.set(o, value);
                }
                else if (field.getType() == Boolean.class) {
                    value = Boolean.valueOf(convertedValue);
                    field.set(o, value);
                }
                else if (field.getType() == Date.class) {
                    if (Date.class.isAssignableFrom(value.getClass())) {
                        field.set(o, value);
                    }
                    else {
                        throw new RuntimeException("Can't copy Date type");
                    }
                }
                else if (field.getType() == String.class) {
                    field.set(o, convertedValue);
                }
                else if (field.getType().isAssignableFrom(value.getClass())) {
                    field.set(o, value);
                }
                else {
                    System.err.println("Failed to set value for field " + field.getName() + " on class "
                            + o.getClass().getSimpleName());
                }
            }
        }
        finally {
            field.setAccessible(false);
        }
    }

    static public boolean isPrimitiveType(Field f) {
        return PRIMITIVE_TYPES.contains(f.getType());
    }

    static public boolean isPrimitiveType(Class<?> clazz) {
        return PRIMITIVE_TYPES.contains(clazz);
    }

    static public boolean isPrimitiveArrayType(Class<?> clazz) {
        return PRIMITIVE_ARRAY_TYPES.contains(clazz);
    }

    static public boolean isPrimitiveWrapperType(Class<?> clazz) {
        return PRIMITIVE_WRAPPER_TYPES.contains(clazz);
    }

    static public boolean isPrimitiveWrapperArrayType(Class<?> clazz) {
        return PRIMITIVE_WRAPPER_ARRAY_TYPES.contains(clazz);
    }

    static public <T> T newInstance(Class<T> clazz) {
        Constructor<T> ctor = null;
        try {
            ctor = clazz.getDeclaredConstructor(new Class<?>[0]);
            ctor.setAccessible(true);
            T o = ctor.newInstance(new Object[0]);
            return o;
        }
        catch (Throwable t) {
            return null;
        }
        finally {
            if (ctor != null) {
                ctor.setAccessible(false);
            }
        }
    }

    static public Class<?> getClassGenericType(Class<?> parentClass) {
        return getClassGenericType(parentClass, 0);
    }

    /**
     * @param position
     *            position of the parameter in class definition, e.g. For
     *            Class<A, B>, position = 0 for A, 1 for B
     */
    static public Class<?> getClassGenericType(Class<?> parentClass, int position) {
        return (Class<?>) ((ParameterizedType) parentClass.getGenericSuperclass()).getActualTypeArguments()[position];
    }

    static public boolean isDoClass(Class<?> clazz) {
        return DataObject.class.isAssignableFrom(clazz);
    }

    static public boolean isVoClass(Class<?> clazz) {
        return ValueObject.class.isAssignableFrom(clazz);
    }

    static public boolean isModelClass(Class<?> clazz) {
        return isDoClass(clazz) || isVoClass(clazz);
    }

    static public boolean isAnnotationPresent(Class<?> targetClass, Class<? extends Annotation> annoClass) {
        do {
            try {
                if (targetClass.isAnnotationPresent(annoClass)) {
                    return true;
                }
            }
            catch (Exception e) {
                return false;
            }
        }
        while ((targetClass = targetClass.getSuperclass()) != Object.class);
        return false;
    }

    static public boolean isAnnotationPresent(Class<?> targetClass, String name, Class<?>[] params,
            Class<? extends Annotation> annoClass) {
        do {
            try {
                Method method = targetClass.getMethod(name, params);
                if (method.isAnnotationPresent(annoClass)) {
                    return true;
                }
            }
            catch (Exception e) {
                return false;
            }
        }
        while ((targetClass = targetClass.getSuperclass()) != Object.class);
        return false;
    }

    /**
     * Retrieve the parameterized type of a field, e.g. List<String> -> String
     */
    static public Class<?> getFieldGenericType(Field field) {
        ParameterizedType elemType = (ParameterizedType) field.getGenericType();
        Type type = elemType.getActualTypeArguments()[0];
        Class<?> elemClass;
        if (type instanceof Class<?>) {
            elemClass = (Class<?>) type;
        }
        else if (type instanceof ParameterizedType) {
            elemClass = (Class<?>) ((ParameterizedType) type).getRawType();
        }
        else {
            throw new RuntimeException("To be implemented.");
        }
        return elemClass;
    }

}
