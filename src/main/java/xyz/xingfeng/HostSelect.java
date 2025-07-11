package xyz.xingfeng;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.xingfeng.Tool.CookieManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HostSelect{
    public static final String HOST = "https://jm18c-twie.club";

    public HostSelect() {
        // 1. 配置代理（需确认代理服务是否运行）
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897));

        // 2. 创建OkHttpClient（添加超时和代理设置）
        OkHttpClient client = new OkHttpClient.Builder()
//                .proxy(proxy)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        // 3. 构建请求（添加User-Agent模拟浏览器）
        Request request = new Request.Builder()
                .url(HOST)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("请求失败: HTTP " + response.code());
                return;
            }

            // 4. 处理Set-Cookie头部
            List<String> cookies = response.headers().values("Set-Cookie");
            if (cookies.isEmpty()) {
                System.out.println("未收到任何Cookie");
                return;
            }

            for (String cookie : cookies) {
                System.out.println("原始Set-Cookie: " + cookie);
                CookieManager.parseAndStoreCookie(cookie, "jm18c-twie.club"); // 使用域名而非URL
            }

        } catch (IOException e) {
            System.err.println("网络请求异常:");
            e.printStackTrace();

            // 5. 诊断DNS问题
            checkDnsResolution("jm18c-twie.club");
        }
    }

    private static void checkDnsResolution(String host) {
        try {
            System.out.println("\nDNS诊断结果:");
            java.net.InetAddress[] addresses = java.net.InetAddress.getAllByName(host);
            for (java.net.InetAddress addr : addresses) {
                System.out.println(host + " -> " + addr.getHostAddress());
            }
        } catch (Exception e) {
            System.err.println("DNS解析失败: " + e.getMessage());
        }
    }


}
