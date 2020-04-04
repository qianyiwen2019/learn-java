package hsfDemo;

import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.manager.ManagerListenerAdapter;
import com.taobao.hsf.standalone.HSFEasyStarter;

/**
 * @author shicai.xsc 2019/7/27 下午11:30
 * @desc
 * @since 5.0.0.0
 */
public class ErrorMain {

    static {
        String tempDir = "/Users/shicai.xsc/tmp";// System.getProperty("java.io.tmpdir");
        // 会帮你把Sar包自动下载、解压缩到tempDir目录下的 2_5_150420/taobao-hsf.sar
        // 线上使用时，推荐将sar包放置到指定目录，比如/home/admin/appname
        // HSFEasyStarter.start("/home/admin/appname/", "2_5_150420");
        // 而版本可以在 http://ops.jm.taobao.org:9999/pandora-web/index.html 选择推荐的版本号。
        System.setProperty("project.name", "hsf-sample-server");
        HSFEasyStarter.start(tempDir, "2_5_150420");
    }

    public static void main(String[] args) throws Exception {
        int j = 0;

        Diamond.addListener("shicai.test", "DEFAULT_GROUP", new ManagerListenerAdapter() {

            public void receiveConfigInfo(String configInfo) {
                System.out.println("receive config info :" + configInfo);
            }
        });

        synchronized (ErrorMain.class) {
            ErrorMain.class.wait();
        }
    }
}
