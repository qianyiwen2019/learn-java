package classLoaderDemo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shicai.xsc 2020/5/21 11:04
 * @desc
 * @since 5.0.0.0
 */
public class App2 {
    private String path = System.getProperty("user.dir") + "/target";
    private String robotClassName = "classLoaderDemo.Robot";
    private String robotInterfaceClassName = "classLoaderDemo.RobotInterface";
    private RobotInterface customRobot;

    public static void main(String[] args) throws Exception {
        App2 app = new App2();
        System.out.println("Ext: " + app.getClass().getClassLoader().getParent());
        app.rightLoad3();
    }

    public void errorLoad3() throws Exception {
        String jar = path + "/hsf-demo-1.0-SNAPSHOT.jar";
        URL url = new File(jar).toURI().toURL();
        CustomApplierClassLoader loader = new CustomApplierClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
        Class clazz = loader.loadClass(robotClassName);

        System.out.println("custom classloader:" + clazz.getClassLoader());
        System.out.println("Robot loaded by: " + clazz.newInstance().getClass().getClassLoader());
        Robot robot = new Robot();
        System.out.println("Robot:" + robot.getClass().getClassLoader());

        // 这个必须加上，然后就重现
        // Exception in thread "main" java.lang.ClassCastException: classLoaderDemo.Robot cannot be cast to classLoaderDemo.Robot
        Thread.currentThread().setContextClassLoader(loader);

        System.out.println("FASTJSON's classloader: " + JSON.class.getClassLoader());
        System.out.println("FASTJSON's SerializeConfig classloader: " + SerializeConfig.globalInstance.getClass().getClassLoader());

        System.out.println("FASTJSON toJSONString of a robot loaded by " + clazz.newInstance().getClass().getClassLoader());
        System.out.println(JSON.toJSONString(clazz.newInstance()));

        System.out.println("FASTJSON toJSONString of a robot loaded by " + robot.getClass().getClassLoader());
        System.out.println(JSON.toJSONString(robot));
    }

    public void rightLoad3() throws Exception {
        // 把 jingwei 依赖的包也放入用户空间，让用户空间内 CustomApplierClassLoader 自行加载所有包，
        // 而不是依赖 CustomApplierClassLoader 的 parent，即 AppClassLoader
        String jingweiLibFolder = "/Users/shicai.xsc/Documents/Work/jingwei3/worker/target/jingwei3-worker.standalone/lib";
        List<URL> urls = new ArrayList<URL>();
        File file = new File(jingweiLibFolder);
        File[] array = file.listFiles();
        for(int i=0;i<array.length;i++) {
            if(array[i].isFile()) {
                urls.add(array[i].toURI().toURL());
            }
        }

        // 每次改完代码必须重新 mvn package 打包，否则 IDEA build 不会更新 hsf-demo-1.0-SNAPSHOT.jar
        String jar = path + "/hsf-demo-1.0-SNAPSHOT.jar";
        urls.add(new File(jar).toURI().toURL());

        CustomApplierClassLoader applierClassLoader = new CustomApplierClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader());

        Thread.currentThread().setContextClassLoader(applierClassLoader);

        Class applierRobotClass = applierClassLoader.loadClass(robotClassName);

        // CustomApplierClassLoader 里对 RobotInterface 做了特殊处理，
        // 保证 CustomApplierClassLoader 加载 RobotInterface 时委托给 AppClassLoader，
        // 这样 (RobotInterface)applierRobotClass.newInstance() 就不会爆类型转换错误
        customRobot = (RobotInterface)applierRobotClass.newInstance();
        customRobot.toJSONString();

        // 实际上这样在用户空间和 default 空间的 JSON 类是不同的，是被不同的 classLoader 所加载的
        // 但是 fastjson 里边有坑，它自己创建了一个 serializerClassLoader，继承自 Thread.currentThread().getContextClassLoader()
        // 然后所有的 serializer 都用这个 serializerClassLoader 加载
        // 如果 JSON 做到了严格的类隔离，就不会有问题，但是不同 classLoader 加载的 JSON 类居然用的是同一个 serializerClassLoader !!!!!!!!!!!!!!!!
        // Exception in thread "main" java.lang.ClassCastException: classLoaderDemo.Robot cannot be cast to
        // classLoaderDemo.Robot
        //	at com.alibaba.fastjson.serializer.ASMSerializer_2_Robot.write(Unknown Source)
        //	at com.alibaba.fastjson.serializer.JSONSerializer.write(JSONSerializer.java:285)
        new Robot().toJSONString();
    }
}