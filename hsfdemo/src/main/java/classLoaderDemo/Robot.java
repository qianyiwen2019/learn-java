package classLoaderDemo;

import com.alibaba.fastjson.JSON;

/**
 * @author shicai.xsc 2019/8/4 下午3:46
 * @desc
 * @since 5.0.0.0
 */
public class Robot implements RobotInterface {
    private String name;
    private int age;

    public Robot() {
        this.name = "mockName";
        this.age = 10;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String toJSONString() {
        System.out.println("rrrrrrrrrrrrrrrrrrrrrr1: classloader: " + this.getClass().getClassLoader());
        System.out.println("rrrrrrrrrrrrrrrrrrrrrr2: classloader: " + JSON.class.getClassLoader());
        // 如果 this 被 classoader A 加载，这里使用的 JSON 类也应该是被 classloader A 所加载
        System.out.println("rrrrrrrrrrrrrrrrrrrrrr3: toString: " + JSON.toJSONString(this));
        // 在本例中，JSON.class.getClassLoader() 分别为 CustomApplierClassLoader 和 AppClassLoader
        return JSON.toJSONString(this);
    }

    @Override
    public String toString() {
        return toJSONString();
    }
}
