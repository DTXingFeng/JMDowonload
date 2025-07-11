package xyz.xingfeng.Tool;

import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;

/**
 * @author Administrator
 */
public class NetRequest {

    /**
     * Get请求
     * @param url url链接
     * @throws IOException io错误
     */
    public String get(String url) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = okHttpClient.newCall(request).execute();
        String string = response.body().string();
//        System.out.println(string);
        return string;
    }
    /**
     * Get请求，带参
     * @param url url链接
     * @param paramMap 请求参数
     * @throws IOException io错误
     */
    public String get(String url, Map<String, Object> paramMap) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder requestbuilder = new Request.Builder()
                .get();

        StringBuilder urlbuilder = new StringBuilder(url);
        if (Objects.nonNull(paramMap)) {
            urlbuilder.append("?");
            paramMap.forEach((key, value) -> {
                try {
                    urlbuilder.append(URLEncoder.encode(key, "utf-8"))
                            .append("=")
                            .append(URLEncoder.encode(String.valueOf(value), "utf-8"))
                            .append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
            urlbuilder.deleteCharAt(urlbuilder.length() - 1);
        }

        Request request = requestbuilder.url(urlbuilder.toString()).build();
        Response response = okHttpClient.newCall(request).execute();
        String string = response.body().string();
//        System.out.println(string);
        return string;
    }

    /**
     * Get请求，带参，带头
     * @param url url链接
     * @param paramMap 请求参数
     * @param heardMap 请求头内容
     * @throws IOException io错误
     */
    public String get(String url, Map<String, Object> paramMap,Map<String, String> heardMap) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder requestbuilder = new Request.Builder()
                .get();

        //增加参数
        StringBuilder urlbuilder = new StringBuilder(url);
        if (Objects.nonNull(paramMap)) {
            urlbuilder.append("?");
            paramMap.forEach((key, value) -> {
                try {
                    urlbuilder.append(URLEncoder.encode(key, "utf-8"))
                            .append("=")
                            .append(URLEncoder.encode(String.valueOf(value), "utf-8"))
                            .append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
            urlbuilder.deleteCharAt(urlbuilder.length() - 1);
        }
        //增加请求头
        Request.Builder heardBuilder = requestbuilder.url(urlbuilder.toString());
        for (Map.Entry<String, String> stringObjectEntry : heardMap.entrySet()) {
            heardBuilder.addHeader(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }

        Request request = heardBuilder.build();
        Response response = okHttpClient.newCall(request).execute();
        String s = response.body().string();
//        System.out.println(s);
//        System.out.println(response.message());
//        System.out.println(response.code());
        return s;
    }

    /**
     * post请求
     * @param url 请求的url
     * @param json json格式字符串
     * @param heardMap 请求头内容
     * @throws IOException io错误
     */
    public String post(String url, String json, Map<String, String> heardMap) throws IOException {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String requestBody = json;
        Request.Builder requestbuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, requestBody));
        //增加请求头
        for (Map.Entry<String, String> stringObjectEntry : heardMap.entrySet()) {
            requestbuilder.addHeader(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }

        Request request = requestbuilder.build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = okHttpClient.newCall(request).execute();
        String re = response.body().string();
//        System.out.println(re);
//        System.out.println(response.message());
//        System.out.println(response.code());
        return re;
    }

    public void setProperty(String Host,String Port){
        System.setProperty("http.proxyHost", Host);
        System.setProperty("http.proxyPort", Port);

        // 对https也开启代理
        System.setProperty("https.proxyHost", Host);
        System.setProperty("https.proxyPort", Port);
    }
}
