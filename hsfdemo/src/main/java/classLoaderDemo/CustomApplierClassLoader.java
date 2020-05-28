package classLoaderDemo;

import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author shicai.xsc 2020/5/21 10:59
 * @desc
 * @since 5.0.0.0
 */
public class CustomApplierClassLoader extends URLClassLoader {

    private static final String JINGWEI_EXTERNAL_CLASS_PREFIX = "com.alibaba.middleware.jingwei.";
    private HashMap<String, Class<?>> loadedClasses = new HashMap<String, Class<?>>();
    /**
     * The classes imported from another places like pandora. They will be loaded before local place.
     */
    private HashMap<String, Class<?>> importedClasses = new HashMap<String, Class<?>>();
    private Set<String> ignoreResources = new HashSet<String>();
    private ClassLoader j2seClassLoader;

    public CustomApplierClassLoader(URL[] urls, ClassLoader parent){
        super(urls, parent);

        ClassLoader j = String.class.getClassLoader();
        if (j == null) {
            j = getSystemClassLoader();
            while (j.getParent() != null) {
                j = j.getParent();
            }
        }
        this.j2seClassLoader = j;

        this.ignoreResources.add("META-INF/app/com.taobao.tddl.monitor.sql.SqlMonitor");
        this.ignoreResources.add("META-INF/app/com.taobao.tddl.monitor.unit.TddlRouter");
    }

    private Class<?> findLoadedClass0(String name) {
        return loadedClasses.get(name);
    }

    private Class<?> loadByParent(String name, boolean resolve) {
        System.out.println("  Delegating to parent classloader at end: " + getParent());
        try {
            Class clazz = Class.forName(name, false, getParent());
            if (clazz != null) {
                System.out.println("  Loading class from parent");
                if (resolve) {
                    resolveClass(clazz);
                }
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
        return null;
    }

    private Class<?> loadImportedClass(String name, boolean resolve) {
        System.out.println("  Trying to load from imported classes.");

        Class clazz = importedClasses.get(name);
        if (clazz != null) {
            System.out.println("  Loading class from imported classes.");
            if (resolve) {
                resolveClass(clazz);
            }
            return (clazz);
        }

        return null;
    }

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // CustomApplierClassLoader 和 AppClassLoader 都需要用到 interface
        // 这里 CustomApplierClassLoader 中加载所有 interface 类都用 AppClassLoader
        // 这样可以保证所有 CustomApplierClassLoader 加载出来的类的实例都可以通过 interface 类转换，以便这些实例在 AppClassLoader 空间内使用
        if (name.contains("classLoaderDemo.RobotInterface")) {
            return loadByParent(name, resolve);
        }

        System.out.println("loadClass(" + name + ", " + resolve + ")");
        Class<?> clazz = loadImportedClass(name, resolve);
        if (clazz != null) {
            return clazz;
        }

        // Check our previously loaded local class cache
        clazz = findLoadedClass0(name);
        if (clazz != null) {
            System.out.println("  Returning class from cache");
            if (resolve) {
                resolveClass(clazz);
            }
            return (clazz);
        }

        // Check our previously loaded class cache
        clazz = findLoadedClass(name);
        if (clazz != null) {
            System.out.println("  Returning class from cache");
            if (resolve) {
                resolveClass(clazz);
            }
            return (clazz);
        }

        // Try loading the class with the system class loader, to prevent
        // the webapp from overriding J2SE classes
        try {
            clazz = j2seClassLoader.loadClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        // jingwei class should not be loaded by custom applier classloader
        if (name.startsWith(JINGWEI_EXTERNAL_CLASS_PREFIX)) {
            clazz = loadByParent(name, resolve);
            if (clazz != null) {
                return clazz;
            }
        }

        // Search local repositories
        System.out.println("  Searching local repositories");
        try {
            clazz = findClass(name);
            if (clazz != null) {
                System.out.println("  Loading class from local repository");
                if (resolve) {
                    resolveClass(clazz);
                }
                loadedClasses.put(name, clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }

        // 只代理被应用排除的jingwei的类,不代理其他的class,避免出现接口在应用中,实现类在worker包中,出现LinkError异常
        // 因为历史原因,很多业务已经依赖了精卫的lib库,只能通过记录日志的方式提醒用户,也方便我们自己排查
        clazz = loadByParent(name, resolve);
        if (clazz != null) {
            System.out.println("[MissJar] Loaded class from jingwei3-worker/lib (may be removed in future), please add jar in custom tar.gz for class:"
                + name);
            return clazz;
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        System.out.println("getResource(" + name + ")");
        URL url = findResource(name);
        if (url != null) {
            return (url);
        }

        if (!ignoreResources.contains(name)) {
            url = getParent().getResource(name);
            if (url != null) {
                System.out.println("  --> Returning '" + url.toString() + "'");
                return (url);
            }
        }
        
        System.out.println("  --> Resource not found, returning null");
        return (null);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        System.out.println("getResource(" + name + ")");
        Enumeration<URL> urls = findResources(name);
        if (urls != null && urls.hasMoreElements()) {
            return (urls);
        }

        if (!ignoreResources.contains(name)) {
            urls = getParent().getResources(name);
            System.out.println("  --> Returning '" + ObjectUtils.toString(urls) + "'");
        }
        return (urls);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        System.out.println("getResourceAsStream(" + name + ")");
        InputStream stream = null;
        System.out.println("  Searching local repositories");
        URL url = findResource(name);
        if (url != null) {
            System.out.println("  --> Returning stream from local");
            try {
                if (stream == null) {
                    stream = url.openStream();
                }
            } catch (IOException e) {
                // Ignore
            }
            if (stream != null) {
                return (stream);
            }
        }

        System.out.println("  Delegating to parent classloader unconditionally " + getParent());

        if (!ignoreResources.contains(name)) {
            stream = getParent().getResourceAsStream(name);
            if (stream != null) {
                System.out.println("  --> Returning stream from parent");
                return (stream);
            }
        }
        System.out.println("  --> Resource not found, returning null");
        return null;

    }

    public HashMap<String, Class<?>> getImportedClasses() {
        return importedClasses;
    }
}
