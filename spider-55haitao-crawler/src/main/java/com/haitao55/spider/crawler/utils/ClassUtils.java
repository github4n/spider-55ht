package com.haitao55.spider.crawler.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 功能：class 操作utils 类
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午11:10:22
 * @version 1.0
 */
public class ClassUtils {

    private static final Logger logger = LoggerFactory
            .getLogger(Constants.LOGGER_NAME_SYSTEM);

    /**
     * 按字段名返回其“add方法”的名字
     * 
     * @param fieldName
     * @return
     */
    public static String toAdderName(final String fieldName) {
        return "add" + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1);
    }

    /**
     * 按字段名返回其“set方法”的名字
     * 
     * @param fieldName
     * @return
     */
    public static String toSetterName(String fieldName) {
        return "set" + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1);
    }

    /**
     * <pre>
     * 根据对象的Class和字段的Class获取这个字段的“add方法”<br>
     * 
     * 注意: 
     * 当前类找不到合适的“add方法“时，<br>
     * 向上查找父类以及接口，直到找到合适的“add方法“<br>
     * 查找时优先匹配父类，而后匹配接口<br>
     * 如果最后还是找不到，则返回null
     * </pre>
     * 
     * @param clazz
     * @param fieldClass
     * @return
     */
    public static Method getAdderIgnoreCase(Class<?> clazz, Class<?> fieldClazz) {
        Method method = findMethodIgnoreCase(clazz,
                toAdderName(fieldClazz.getSimpleName()), fieldClazz);
        if (method != null) {
            return method;
        }
        // 从父类中查找
        List<Class<?>> superClasses = org.apache.commons.lang3.ClassUtils
                .getAllSuperclasses(fieldClazz);
        if (CollectionUtils.isNotEmpty(superClasses)) {
            for (Class<?> c : superClasses) {
                method = findMethodIgnoreCase(clazz,
                        toAdderName(c.getSimpleName()), fieldClazz);
                if (method != null) {
                    return method;
                }
            }
        }
        // 从接口中查找
        List<Class<?>> interfaces = org.apache.commons.lang3.ClassUtils
                .getAllInterfaces(fieldClazz);
        if (CollectionUtils.isNotEmpty(interfaces)) {
            for (Class<?> c : interfaces) {
                method = findMethodIgnoreCase(clazz,
                        toAdderName(c.getSimpleName()), fieldClazz);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * <pre>
     * 根据对象的Class和字段的Class获取这个字段的“set方法”，将忽略方法名字的大小写，并且返回的方法所使用的参数可能不是精确匹配：
     * 这个参数的类型可能是指定字段类型的父类 <br>
     * 
     * 注意: 
     * 当前类找不到合适的“set方法“时，<br>
     * 向上查找父类以及接口，直到找到合适的“set方法“<br>
     * 查找时优先匹配父类，而后匹配接口<br>
     * 如果最后还是找不到，则返回null
     * </pre>
     * 
     * @param clazz
     * @param fieldClass
     * @return
     */
    public static Method getSetterIgnoreCase(Class<?> clazz, Class<?> fieldClass) {
        Method method = findMethodIgnoreCase(clazz,
                toSetterName(fieldClass.getSimpleName()), fieldClass);
        if (method != null) {
            return method;
        }
        // 从父类中查找
        List<Class<?>> superClasses = org.apache.commons.lang3.ClassUtils
                .getAllSuperclasses(fieldClass);
        if (CollectionUtils.isNotEmpty(superClasses)) {
            for (Class<?> c : superClasses) {
                method = findMethodIgnoreCase(clazz,
                        toSetterName(c.getSimpleName()), fieldClass);
                if (method != null) {
                    return method;
                }
            }
        }
        // 从接口中查找
        List<Class<?>> interfaces = org.apache.commons.lang3.ClassUtils
                .getAllInterfaces(fieldClass);
        if (CollectionUtils.isNotEmpty(interfaces)) {
            for (Class<?> c : interfaces) {
                method = findMethodIgnoreCase(clazz,
                        toSetterName(c.getSimpleName()), fieldClass);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param clazz
     * @param expectedMethod
     *            期望的方法名
     * @param expectedArgs
     *            期望的方法参数
     * @return
     */
    public static Method findMethodIgnoreCase(Class<?> clazz,
            String expectedMethod, Class<?>... expectedArgs) {
        Method[] methods = clazz.getMethods();
        Method ret = null;
        for (Method method : methods) {
            String name = method.getName();
            // 判断参数长度
            Class<?>[] argTypes = method.getParameterTypes();
            if (expectedArgs.length != argTypes.length) {
                continue;
            }
            // 判断方法名
            if (name.equalsIgnoreCase(expectedMethod)) {
                // 判断方法参数
                if (Arrays.equals(argTypes, expectedArgs)) {
                    return method;
                }

                // 判断方法参数父类
                int assignableCount = 0;
                for (int index = 0; index < argTypes.length; index++) {
                    if (argTypes[index].isAssignableFrom(expectedArgs[index])) {
                        assignableCount++;
                    } else {
                        break;
                    }
                }
                if (assignableCount == argTypes.length) {
                    ret = method;
                }
            }
        }
        return ret;
    }

    /**
     * 
     * @param clazz
     * @param expectedMethod
     *            期望的方法名
     * @return
     */
    public static Method[] findMethodsIgnoreCase(Class<?> clazz,
            String expectedMethod) {
        Method[] methods = clazz.getMethods();
        List<Method> list = new ArrayList<Method>();
        for (Method _method : methods) {
            if (_method.getName().equalsIgnoreCase(expectedMethod)) {
                list.add(_method);
            }
        }
        return list.toArray(new Method[list.size()]);
    }

    /**
     * 
     * @param clazz
     * @param expectedMethod
     *            期望的方法名
     * @param expectedArgsLength
     *            期望的参数长度
     * @return
     */
    public static Method[] findMethodsIgnoreCase(Class<?> clazz,
            String expectedMethod, int expectedArgsLength) {
        Method[] methods = clazz.getMethods();
        List<Method> list = new ArrayList<Method>();
        for (Method _method : methods) {
            if (_method.getName().equalsIgnoreCase(expectedMethod)
                    && _method.getParameterTypes().length == expectedArgsLength) {
                list.add(_method);
            }
        }
        return list.toArray(new Method[list.size()]);
    }

    /**
     * 获取某个指定的单参数方法
     * 
     * @param clazz
     * @param argClass
     * @param methodName
     */
    public static Method findMethod(Class<?> clazz, String methodName,
            Class<?> argClass) {
        try {
            return clazz.getMethod(methodName, argClass);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 尝试反射一个实例
     * 
     * @param className
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className) {
        try {
            // class.forName 会触发静态块
            return (T) Class.forName(className).newInstance();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 尝试向instance中放入field<br>
     * 首先尝试查找setter<br>
     * 其次尝试查找adder<br>
     * 如果都没有则不做任何事情
     * 
     * @param object
     * @param field
     */
    public static void setField(Object instance, Object field) {
        Class<?> parentClass = instance.getClass();
        Class<?> childClass = field.getClass();
        Method setter = getSetterIgnoreCase(parentClass, childClass);
        if (setter != null) {
            invokeMethod(instance, setter, field);
            return;
        }
        Method adder = getAdderIgnoreCase(parentClass, childClass);
        if (adder != null) {
            invokeMethod(instance, adder, field);
        }
    }

    /**
     * 尝试调用instance的method方法，入参为value
     * 
     * @param instance
     * @param method
     * @param value
     */
    public static void invokeMethod(Object instance, Method method,
            Object... value) {
        try {
            method.invoke(instance, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}