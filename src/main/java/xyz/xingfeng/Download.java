package xyz.xingfeng;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.xingfeng.Tool.CookieManager;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Download {
    String jMMun;
    public Download(String jMMun){
        this.jMMun=jMMun;
        DownloadResource();
    }


    public byte[] DownloadCaptchaImage(){
        OkHttpClient client = new OkHttpClient.Builder().build();

        String cookie = CookieManager.getCookie("/");
        Request request = new Request.Builder()
                .url(HostSelect.HOST + "/captcha/")
                .addHeader("Cookie", cookie)
                .build();
        //下载的是图片
        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("下载失败: HTTP " + response.code());
                return null;
            }
            byte[] imageBytes = response.body().bytes();
            // 这里可以将imageBytes保存为文件或进行其他处理
            System.out.println("下载成功，图片大小: " + imageBytes.length + " bytes");
            return imageBytes;
        } catch (Exception e) {
            System.err.println("下载异常:");
            e.printStackTrace();
        }
        return null;
    }

    public String ImageToBase64(){
        String base64Str = Base64.getEncoder().encodeToString(DownloadCaptchaImage());
        return "data:image/jpeg;base64," + base64Str;
    }

    /**
     * 构建请求消息
     */
    private String buildRequestMessage() throws Exception {
        //构建消息
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("model","deepseek-ai/deepseek-vl2");
        jsonObject.put("stream",false);
        jsonObject.put("max_tokens",2048);
        //创建消息
        JSONArray msgs = new JSONArray();
        msgs.put(new JSONObject("{\n" +
                "      \"role\": \"system\",\n" +
                "      \"content\": [\n" +
                "        {\n" +
                "          \"text\": \"[系统指令] 请作为简单的计算机，算出图片的算数题的答案是多少，请简单回答，比如题目是2*3=，你只需回答“6”，不要再列出原式，你只需要输出数字答案\",\n" +
                "          \"type\": \"text\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }"));
        //创建用户消息
        JSONObject user = new JSONObject();
        user.put("role","user");
        JSONArray content = new JSONArray();
        JSONObject image_url = new JSONObject();
        image_url.put("detail","auto");
        image_url.put("url",ImageToBase64());
        content.put(new JSONObject().put("image_url",image_url).put("type","image_url"));
        user.put("content",content);
        //完成
        msgs.put(user);
        jsonObject.put("messages",msgs);
        return jsonObject.toString();
    }

    /**
     * 通过视觉模型分析验证码
     */
    public String modelAnalysis() throws Exception {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,buildRequestMessage());
        Request request = new Request.Builder()
                .url("https://api.siliconflow.cn/v1/chat/completions")
                .method("POST", body)
                .addHeader("Authorization", "Bearer "+Main.apiKey)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        String string = response.body().string();
        JSONObject jsonObject = new JSONObject(string);
        if (jsonObject.has("choices")) {
            return jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }else {
            throw new Exception("分析失败");
        }
    }

    /**
     * 本子下载
     */
    public void DownloadResource() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        String verification = null;
        try {
            verification = modelAnalysis();
        } catch (Exception e) {
            System.err.println("验证码分析失败:");
            e.printStackTrace();
            return;
        }
        RequestBody formBody = new FormBody.Builder()
                .add("album_id", jMMun)
                .add("verification", verification)
                .build();
        Request request = new Request.Builder()
                .post(formBody)
                .url(HostSelect.HOST + "/album_download/" + jMMun)
                .addHeader("Cookie", CookieManager.getCookie("/"))
                .addHeader("content-type","application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("下载链接获取失败: HTTP " + response.code());
                return;
            }
            Request request1 = response.request();
            //获得set-cookie
            List<String> headers = response.headers("set-cookie");
            for (String header : headers) {
                CookieManager.parseAndStoreCookie(header, HostSelect.HOST);
            }
            try (Response response1 = client.newCall(request1).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("下载失败: HTTP " + response.code());
                    return;
                }
                byte[] data = response1.body().bytes();
                try (OutputStream outputStream = new FileOutputStream(jMMun + ".zip")) {
                    outputStream.write(data);
                    System.out.println("下载成功，文件保存到: " + jMMun + ".zip");
                }
            }catch (Exception e) {
                System.err.println("下载异常:");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("获取下载链接异常:");
            e.printStackTrace();
        }
    }




}
