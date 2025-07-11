package xyz.xingfeng;

import okhttp3.*;
import xyz.xingfeng.Tool.CookieManager;
import xyz.xingfeng.Tool.SQLiteConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

public class Login {
    public Login(String account, String password) {
        // 1. 配置代理（需确认代理服务是否运行）
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897));
        OkHttpClient client = new OkHttpClient.Builder()
//                .proxy(proxy)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("username", account)
                .add("password", password)
                .add("id_remember", "on")
                .add("login_remember", "on")
                .add("submit_login", "1")
                .build();
        Request.Builder b = new Request.Builder();
        String cookie = CookieManager.getCookie("/");
        b.addHeader("Cookie", cookie);
        Request request = b.url(HostSelect.HOST+"/login")
                .post(formBody)
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("Accept", "*/*")
                .addHeader("Host", "jm18c-twie.club")
                .addHeader("Connection", "keep-alive")
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                List<String> headers = response.headers("set-cookie");
                for (String header : headers) {
                    CookieManager.parseAndStoreCookie(header, HostSelect.HOST);
                }
                String responseBody = response.body().string();
                System.out.println(responseBody);
            }
        } catch (IOException e) {
            throw new RuntimeException("登录请求失败", e);
        }
    }
}
