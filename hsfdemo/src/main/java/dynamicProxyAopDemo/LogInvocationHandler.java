package dynamicProxyAopDemo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author shicai.xsc 2019/9/30 上午12:40
 * @desc
 * @since 5.0.0.0
 */
public class LogInvocationHandler implements InvocationHandler {

    private Object target; //目标对象

    LogInvocationHandler(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //执行原有逻辑
        Object rev = method.invoke(target, args);
        //执行织入的日志，你可以控制哪些方法执行切入逻辑
        if (method.getName().equals("doSomeThing2")) {
            System.out.println("记录日志");
        }
        return rev;
    }
}
