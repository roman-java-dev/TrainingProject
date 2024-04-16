package org.example.lib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Injector is a class responsible for dependency injection.
 * It scans the specified package for classes annotated with @Service and
 * automatically injects dependencies annotated with @Inject.
 */
public class Injector {
    private static final Map<String, Injector> injectors = new HashMap<>();
    private final Map<Class<?>, Object> instanceOfClasses = new HashMap<>();
    private final List<Class<?>> classes = new ArrayList<>();

    /**
     * Constructs an Injector object with the specified main package name.
     * It scans the package for classes upon instantiation.
     *
     * @param mainPackageName The main package name to scan for classes.
     */
    private Injector(String mainPackageName) {
        try {
            classes.addAll(getClasses(mainPackageName));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Can't get information about all classes", e);
        }
    }

    /**
     * Retrieves an instance of the Injector for the specified main package name.
     * If an instance for the package already exists, it returns the existing instance,
     * otherwise, it creates a new one.
     *
     * @param mainPackageName The main package name for which to retrieve the Injector instance.
     * @return An instance of the Injector.
     */
    public static Injector getInstance(String mainPackageName) {
        if (injectors.containsKey(mainPackageName)) {
            return injectors.get(mainPackageName);
        }
        Injector injector = new Injector(mainPackageName);
        injectors.put(mainPackageName, injector);
        return injector;
    }

    /**
     * Retrieves an instance of a class that implements a certain interface.
     * It automatically resolves dependencies annotated with @Inject.
     *
     * @param certainInterface The interface that the class must implement.
     * @return An instance of a class implementing the specified interface.
     */
    public Object getInstance(Class<?> certainInterface) {
        Object newInstanceOfClass = null;
        Class<?> clazz = findClassExtendingInterface(certainInterface);
        Object instanceOfCurrentClass = createInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (isFieldInitialized(field, instanceOfCurrentClass)) {
                continue;
            }
            if (field.getDeclaredAnnotation(Inject.class) != null) {
                Object classToInject = getInstance(field.getType());
                newInstanceOfClass = getNewInstance(clazz);
                setValueToField(field, newInstanceOfClass, classToInject);
            } else {
                throw new RuntimeException("Class " + field.getName() + " in class "
                        + clazz.getName() + " hasn't annotation Inject");
            }
        }
        if (newInstanceOfClass == null) {
            return getNewInstance(clazz);
        }
        return newInstanceOfClass;
    }

    /**
     * Finds a class that implements a specified interface and is annotated with @Service.
     *
     * @param certainInterface The interface to find an implementing class for.
     * @return The class that implements the specified interface and is annotated with @Service.
     * @throws RuntimeException if no such class is found.
     */
    private Class<?> findClassExtendingInterface(Class<?> certainInterface) {
        for (Class<?> clazz : classes) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> singleInterface : interfaces) {
                if (singleInterface.equals(certainInterface)
                        && (clazz.isAnnotationPresent(Service.class))) {
                    return clazz;
                }
            }
        }
        throw new RuntimeException("Can't find class which implements "
                + certainInterface.getName()
                + " interface and has valid annotation (Service)");
    }

    /**
     * Retrieves a new instance of a certain class. If an instance of the class already exists,
     * it returns the existing instance, otherwise, it creates a new one.
     *
     * @param certainClass The class for which to retrieve an instance.
     * @return An instance of the specified class.
     */
    private Object getNewInstance(Class<?> certainClass) {
        if (instanceOfClasses.containsKey(certainClass)) {
            return instanceOfClasses.get(certainClass);
        }
        Object newInstance = createInstance(certainClass);
        instanceOfClasses.put(certainClass, newInstance);
        return newInstance;
    }

    /**
     * Checks if a field in an object instance is initialized.
     *
     * @param field    The field to check.
     * @param instance The object instance containing the field.
     * @return True if the field is initialized, otherwise false.
     * @throws RuntimeException if access to the field is denied.
     */
    private boolean isFieldInitialized(Field field, Object instance) {
        field.setAccessible(true);
        try {
            return field.get(instance) != null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't get access to field");
        }
    }

    /**
     * Creates a new instance of a specified class using its default constructor.
     *
     * @param clazz The class for which to create an instance.
     * @return A new instance of the specified class.
     * @throws RuntimeException if an instance of the class cannot be created.
     */
    private Object createInstance(Class<?> clazz) {
        Object newInstance;
        try {
            Constructor<?> classConstructor = clazz.getConstructor();
            newInstance = classConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create object of the class", e);
        }
        return newInstance;
    }

    /**
     * Sets a value to a field in an object instance.
     *
     * @param field           The field to set the value to.
     * @param instanceOfClass The object instance containing the field.
     * @param classToInject   The value to set to the field.
     * @throws RuntimeException if access to the field is denied or setting the value fails.
     */
    private void setValueToField(Field field, Object instanceOfClass, Object classToInject) {
        try {
            field.setAccessible(true);
            field.set(instanceOfClass, classToInject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't set value to field ", e);
        }
    }

    /**
     * Scans all classes accessible from the context class loader which
     * belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException if the class cannot be located
     * @throws IOException            if I/O errors occur
     */
    private static List<Class<?>> getClasses(String packageName)
            throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new RuntimeException("Class loader is null");
        }
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException if the class cannot be located
     */
    private static List<Class<?>> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().contains(".")) {
                        throw new RuntimeException("File name shouldn't consist point.");
                    }
                    classes.addAll(findClasses(file, packageName + "."
                            + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.'
                            + file.getName().substring(0, file.getName().length() - 6)));
                }
            }
        }
        return classes;
    }
}
