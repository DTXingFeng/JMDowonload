package xyz.xingfeng;

public class Main {
    public static final String apiKey = "这里填硅基流动的apiKey";
    public static void main(String[] args) {
        String JM号 = "1195897";
        new Download(JM号);
        new WebpToPdfConverter(JM号);
    }


}
class fist{
    /**
     * 第一次用启动这个
     */
    public static void main(String[] args) {
        new HostSelect();
        new Login("账号", "密码");
    }
}