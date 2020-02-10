package org.grapple.reflect;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.reflect.ClassLiteral.classLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.grapple.schema.DefinitionImportException;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple.utils.UnexpectedException;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public final class ReflectUtils {

    private static final Map<Class<?>, Class<?>> primitiveWrappers = new NoDuplicatesMap<>();

    private ReflectUtils() {

    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> wrapPrimitiveTypeIfNecessary(Class<T> clazz) {
        requireNonNull(clazz, "clazz");
        return (clazz.isPrimitive() ? (Class<T>) primitiveWrappers.get(clazz) : clazz);
    }

    public static Class<?> getRawTypeFor(Type type) {
        requireNonNull(type, "type");
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        throw new UnexpectedException(format("Unexpected type: %s", type));
    }

    public static Type getGenericTypeArgument(Type type, int index) {
        requireNonNull(type, "type");
        if (!(type instanceof ParameterizedType)) {
            throw new UnexpectedException(format("Unexpected type: %s", type));
        }
        final ParameterizedType parameterizedType = (ParameterizedType) type;
        if (index > parameterizedType.getActualTypeArguments().length) {
            throw new UnexpectedException(format("Type parameter: %d not in range for type: %s", index, type));
        }
        return parameterizedType.getActualTypeArguments()[index];
    }

    public static Set<Class<?>> getAllSuperClassesOf(Class<?> clazz) {
        requireNonNull(clazz, "clazz");
        return getAllSuperClassesOf(new LinkedHashSet<>(), clazz);
    }

    public static Set<Class<?>> getAllSuperClassesOf(Set<Class<?>> allClasses, Class<?> clazz) {
        // Returns the class, all superclasses, and all superinterfaces
        allClasses.add(clazz);
        if (clazz.getSuperclass() != null) {
            getAllSuperClassesOf(allClasses, clazz.getSuperclass());
        }
        for (Class<?> child: clazz.getInterfaces()) {
            getAllSuperClassesOf(allClasses, child);
        }
        return allClasses;
    }

    public static Set<Method> getAllDefinitionsOf(Method method) {
        requireNonNull(method, "method");
        // Returns the method and all of it's declarations (in all superclasses and interfaces)
        final Set<Method> allMethods = new LinkedHashSet<>();
        for (Class<?> clazz: getAllSuperClassesOf(method.getDeclaringClass())) {
            lookupDeclaredMethod(clazz, method.getName(), method.getParameterTypes()).ifPresent(allMethods::add);
        }
        return allMethods;
    }

    public static Optional<Method> lookupDeclaredMethod(Class<?> clazz, String name, Class<?>[] parameterTypes) {
        requireNonNull(clazz, "clazz");
        requireNonNull(name, "name");
        requireNonNull(parameterTypes, "parameterTypes");
        try {
            return Optional.of(clazz.getMethod(name, parameterTypes));
        }
        catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public static <A extends Annotation> Optional<A> searchMethodAnnotation(Method initial, Class<A> aClazz) {
        requireNonNull(initial, "initial");
        requireNonNull(aClazz, "aClazz");
        for (Method method: getAllDefinitionsOf(initial)) {
            final A annotation = method.getDeclaredAnnotation(aClazz);
            if (annotation != null) {
                return Optional.of(annotation);
            }
        }
        return Optional.empty();
    }

    public static <A extends Annotation> Optional<A> searchParameterAnnotation(Method initial, int index, Class<A> aClazz) {
        requireNonNull(initial, "initial");
        requireNonNull(aClazz, "aClazz");
        for (Method method: getAllDefinitionsOf(initial)) {
            final A annotation = method.getParameters()[index].getDeclaredAnnotation(aClazz);
            if (annotation != null) {
                return Optional.of(annotation);
            }
        }
        return Optional.empty();
    }

    public static Set<Class<?>> getAllTypesAnnotatedWith(Set<String> packageNames, Class<? extends Annotation> annotation, boolean includeInherited) {
        requireNonNull(packageNames, "packageNames");
        requireNonNull(annotation, "annotation");
        if (packageNames.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<URL> urlsToScan = new HashSet<>();
        packageNames.forEach(packageName -> urlsToScan.addAll(ClasspathHelper.forPackage(packageName)));
        final ConfigurationBuilder configurationBuilder = ConfigurationBuilder.build();
        configurationBuilder.setUrls(urlsToScan);
        configurationBuilder.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());
        configurationBuilder.useParallelExecutor();
        try {
            return new Reflections(configurationBuilder).getTypesAnnotatedWith(annotation, includeInherited);
        }
        catch (ReflectionsException e) {
            // Library is buggy - sometimes if no results found, throws exception
            return Collections.emptySet();
        }
    }

    public static Class<?> parseEntityFromGenericType(Field source, Type entityFieldOrJoin) {
        requireNonNull(source, "source");
        requireNonNull(entityFieldOrJoin, "entityFieldOrJoin");
        // We assume in an entity declaration, the first type parameter is the entity
        if (!(entityFieldOrJoin instanceof ParameterizedType)) {
            throw new DefinitionImportException(format("Unexpected type for: %s", source));
        }
        final ParameterizedType parameterizedType = (ParameterizedType) entityFieldOrJoin;
        if (parameterizedType.getActualTypeArguments().length != 2) {
            throw new DefinitionImportException(format("Unexpected type arguments for: %s", source));
        }
        final Type firstTypeArgument = parameterizedType.getActualTypeArguments()[0];
        if (!(firstTypeArgument instanceof Class<?>)) {
            throw new DefinitionImportException(format("Unexpected type arguments for: %s", source));
        }
        return (Class<?>) firstTypeArgument;
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<T> typeLiteral(Type type) {
        requireNonNull(type, "type");
        if (type instanceof Class<?>) {
            return classLiteral((Class<T>) type);
        }
        return new TypeLiteral<T>(){

            @Override
            public Type getType() {
                return type;
            }

            @Override
            public boolean isPrimitiveType() {
                return false; // Only classes can be primitive and we are not a class
            }

            @Override
            public TypeLiteral<T> wrapPrimitiveTypeIfNecessary() {
                return this;
            }

            @Override
            public boolean isSubtypeOf(Class<?> clazz) {
                return false;
            }
        };
    }

    static {
        primitiveWrappers.put(boolean.class, Boolean.class);
        primitiveWrappers.put(byte.class, Byte.class);
        primitiveWrappers.put(char.class, Character.class);
        primitiveWrappers.put(short.class, Short.class);
        primitiveWrappers.put(int.class, Integer.class);
        primitiveWrappers.put(long.class, Long.class);
        primitiveWrappers.put(float.class, Float.class);
        primitiveWrappers.put(double.class, Double.class);
    }
}

