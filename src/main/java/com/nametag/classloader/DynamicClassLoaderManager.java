package com.nametag.classloader;

import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class DynamicClassLoaderManager {

    // Child-first classloader
    private volatile ChildFirstClassLoader moduleClassLoader;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private static final String MODULE_PATH = "/opt/app/modules/module.jar";

    public void loadModule() throws Exception {
        lock.writeLock().lock();
        try {
            if (moduleClassLoader != null) {
                moduleClassLoader.close();
            }

            File jar = new File(MODULE_PATH);
            if (!jar.exists()) {
                throw new IllegalStateException("Module JAR not found at: " + MODULE_PATH);
            }

            moduleClassLoader = new ChildFirstClassLoader(
                    new URL[]{jar.toURI().toURL()}
            );

        } finally {
            lock.writeLock().unlock();
        }
    }

    public Object invoke(String className, String method) throws Exception {
        lock.readLock().lock();
        try {
            if (moduleClassLoader == null) {
                throw new IllegalStateException("Module not loaded yet.");
            }

            Class<?> clazz = moduleClassLoader.loadClass(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method m = clazz.getMethod(method);

            return m.invoke(instance);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void pauseAll() {
        lock.writeLock().lock();
    }

    public void resumeAll() {
        lock.writeLock().unlock();
    }

    /**
     * CHILD-FIRST CLASSLOADER
     */
    public static class ChildFirstClassLoader extends URLClassLoader {

        public ChildFirstClassLoader(URL[] urls) {
            super(urls, null); // no parent = full isolation
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            // Try to load from module jar first
            try {
                return findClass(name);
            } catch (ClassNotFoundException ignore) {
            }

            // Fallback to system/application classloaders
            return super.loadClass(name);
        }
    }
}
