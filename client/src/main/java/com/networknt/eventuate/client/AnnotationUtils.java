package com.networknt.eventuate.client;

import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * General utility methods for working with annotations, handling meta-annotations,
 * bridge methods (which the compiler generates for generic declarations) as well
 */
public class AnnotationUtils {
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        if (annotationType == null) {
            return null;
        }
        A result = null;
        ClassInfo classInfo = EventuateClientStartupHookProvider.classNameToClassInfo.get(clazz.getName());
        // get all interfaces that this class implements.
        List<String> interfaces =
                classInfo == null
                        ? Collections.emptyList()
                        : classInfo.getNamesOfImplementedInterfaces();
        Iterator<String> iterator = interfaces.iterator();
        if (interfaces != null && interfaces.size() > 0) {
            while (iterator.hasNext()) {
                String iName = iterator.next();
                classInfo = EventuateClientStartupHookProvider.classNameToClassInfo.get(iName);
                if (classInfo.hasDirectAnnotation(annotationType.getName())) {
                    try {
                        Class c = Class.forName(iName);
                        result = (A) c.getAnnotation(annotationType);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

        } else {
            if (classInfo.hasDirectAnnotation(annotationType.getName())) {
                try {
                    Class c = Class.forName(classInfo.getClassName());
                    result = (A) c.getAnnotation(annotationType);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }
}
