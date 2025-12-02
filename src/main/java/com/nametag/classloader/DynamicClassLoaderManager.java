package com.nametag.classloader;

import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class DynamicClassLoaderManager {

    private volatile URLClassLoader moduleClassLoader;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void loadModule() throws Exception {
        lock.writeLock().lock();
        try {
            if (moduleClassLoader != null) {
                moduleClassLoader.close(); // unload previous
            }

            File jar = new File("dynamic/modules/module.jar");
            moduleClassLoader = new URLClassLoader(
                    new URL[]{jar.toURI().toURL()},
                    this.getClass().getClassLoader()
            );

        } finally {
            lock.writeLock().unlock();
        }
    }

    public Object invoke(String className, String method) throws Exception {
        lock.readLock().lock();
        try {
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
}
