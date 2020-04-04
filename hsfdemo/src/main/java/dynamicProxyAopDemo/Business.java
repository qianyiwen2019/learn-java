package dynamicProxyAopDemo;

/**
 * @author shicai.xsc 2019/9/30 上午12:41
 * @desc
 * @since 5.0.0.0
 */
public class Business implements IBusiness, IBusiness2 {

    public boolean doSomeThing() {
        System.out.println("执行业务逻辑");
        return true;
    }

    public void doSomeThing2() {
        System.out.println("执行业务逻辑2");
    }

}
