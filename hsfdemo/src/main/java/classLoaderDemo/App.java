package classLoaderDemo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;

/**
 * @author shicai.xsc 2019/8/4 下午3:50
 * @desc
 * @since 5.0.0.0
 */
public class App {
    private String path = System.getProperty("user.dir") + "/target/classes";
    private String className = "classLoaderDemo.Robot";

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.errorLoad3();
    }

    public void errorLoad1() throws Exception {
        // 默认为 Launcher$AppClassLoader 加载
        // sun.misc.Launcher$AppClassLoader@18b4aac2
        System.out.println(new Robot().getClass().getClassLoader());

        TestClassLoader loader = new TestClassLoader(path);
        Class clazz = loader.loadClass(className);

        // classLoaderDemo.TestClassLoader@1761e840
        System.out.println(clazz.getClassLoader());

        // classLoaderDemo.TestClassLoader@6c629d6e
        System.out.println(clazz.newInstance().getClass().getClassLoader());
        // sun.misc.Launcher$AppClassLoader@18b4aac2
        System.out.println(new Robot().getClass().getClassLoader());

        // false
        // 一个的 classLoader 为 classLoaderDemo.TestClassLoader，另一个为 sun.misc.Launcher$AppClassLoader
        System.out.println(clazz.newInstance() instanceof Robot);
    }

    public void errorLoad2() throws Exception {
        // 默认为 Launcher$AppClassLoader 加载
        // sun.misc.Launcher$AppClassLoader@18b4aac2
        System.out.println("111111: Robot:" + new Robot().getClass().getClassLoader());

        TestClassLoader loader = new TestClassLoader(path);
        Class clazz = loader.loadClass(className);

        // classLoaderDemo.TestClassLoader@1761e840
        System.out.println("2222222:" + clazz.getClassLoader());

        // classLoaderDemo.TestClassLoader@6c629d6e
        System.out.println("3333333: Robot:" + clazz.newInstance().getClass().getClassLoader());

        // 设置当前进程的 ClassLoader
        Thread.currentThread().setContextClassLoader(loader);
        // sun.misc.Launcher$AppClassLoader@18b4aac2
        System.out.println("44444: Robot:" + new Robot().getClass().getClassLoader());

        Robot a = (Robot)clazz.newInstance();
    }

    public void errorLoad3() throws Exception {
        TestClassLoader loader = new TestClassLoader(path);
        Thread.currentThread().setContextClassLoader(loader);

        Class clazz = loader.loadClass(className);

        // classLoaderDemo.TestClassLoader@1761e840
        System.out.println("2222222:" + clazz.getClassLoader());

        // classLoaderDemo.TestClassLoader@6c629d6e
        System.out.println("3333333: Robot:" + clazz.newInstance().getClass().getClassLoader());

        Robot robot = new Robot();
        System.out.println("4444444: Robot:" + robot.getClass().getClassLoader());

        System.out.println("JSON classloader: " + JSON.class.getClassLoader());
        System.out.println("SerializeConfig classloader: " + SerializeConfig.globalInstance.getClass().getClassLoader());
        JSON.toJSONString(clazz.newInstance());
        JSON.toJSONString(robot);
    }

    public void errorLoad0() throws Exception {
        TestClassLoader loader = new TestClassLoader(path);
        Class clazz = loader.loadClass(className);

        // classLoaderDemo.TestClassLoader@6c629d6e
        System.out.println(clazz.newInstance().getClass().getClassLoader());

        // sun.misc.Launcher$AppClassLoader@18b4aac2
        // 已经用 TestClassLoader 加载 Robot 类了，为什么 new Robot() 还是使用的 AppClassLoader 加载的那一个？
        // 因为根据类加载的原理，创建一个类时，默认使用的是创建该类的类的 ClassLoader。
        // 就本例而言，new Robot() 使用的是 App 的 ClassLoader（即 AppClassLoader）
        // 要解决这个问题，参考下面的 okLoad()，即用 TestClassLoader 将 App 这个类重新 load 并创建实例一遍。
        System.out.println(new Robot().getClass().getClassLoader());
    }

    public void okLoad() throws Exception {
        TestClassLoader loader = new TestClassLoader(path);
        if (!(this.getClass().getClassLoader() instanceof TestClassLoader)) {
            Class<?> mainClass = loader.loadClass(this.getClass().getName());
            Object main = mainClass.newInstance();
            mainClass.getDeclaredMethod("okLoad").invoke(main);
            return;
        }

        // classLoaderDemo.TestClassLoader@29444d75
        System.out.println(new Robot().getClass().getClassLoader());
    }
}
