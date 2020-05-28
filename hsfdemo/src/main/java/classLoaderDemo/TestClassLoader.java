package classLoaderDemo;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author shicai.xsc 2019/8/4 下午3:43
 * @desc
 * @since 5.0.0.0
 */
public class TestClassLoader extends ClassLoader {
    private String path;

    public TestClassLoader(String path) {
        this.path = path;
    }

    @Override
    public Class findClass(String name) {
        return loadClass(name);
    }

    @Override
    public Class<?> loadClass(String name) {
        try {
            // 自己不加载自己，TestClassLoader 本身由 AppClassLoader 加载
            if (StringUtils.equals(name, this.getClass().getName())) {
                return getParent().loadClass(name);
            }

            // 目的：替换 AppClassLoader 为 TestClassLoader，原本由 AppClassLoader 加载的类（用户自己定义的类）改成由 TestClassLoader 加载
            // 其他系统类（如 Object, String) 仍由 ExtClassLoader 和 BootstrapClassLoader 加载
            // 默认情况下，当前 classLoader 的 parent 是 AppClassLoader，
            // 而当前 classLoader 的 parent 的 parent 才是 ExtClassLoader
            return getParent().getParent().loadClass(name);
        } catch (Exception e) {
            System.out.println("class " + name + " is not loaded by parent");
        }

        byte[] b = null;
        try {
            b = loadClassData(name);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Class clazz = defineClass(name, b, 0, b.length);
        return clazz;
    }

    private byte[] loadClassData(String name) throws IOException {
        String tmpPath = "";
        for (String tmp: name.split("\\.")) {
            tmpPath += "/" + tmp;
        }
        String namePath = path + tmpPath + ".class";

        InputStream in = null;
        ByteArrayOutputStream out = null;

        try {
            in = new FileInputStream(new File(namePath));
            out = new ByteArrayOutputStream();
            int i = 0;
            while ((i = in.read()) != -1) {
                out.write(i);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            in.close();
            out.close();
        }
        return out.toByteArray();
    }
}


