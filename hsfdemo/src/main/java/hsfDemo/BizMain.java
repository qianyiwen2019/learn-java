package hsfDemo;

import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.manager.ManagerListenerAdapter;

/**
 * @author shicai.xsc 2019/7/28 上午1:22
 * @desc
 * @since 5.0.0.0
 */
public class BizMain {
    public static void main(String[] args) throws Exception {
        Diamond.addListener("shicai.test", "DEFAULT_GROUP", new ManagerListenerAdapter() {

            public void receiveConfigInfo(String configInfo) {
                System.out.println("receive config info :" + configInfo);
            }
        });
    }
}
