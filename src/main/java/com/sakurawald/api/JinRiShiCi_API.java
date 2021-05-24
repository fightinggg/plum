package com.sakurawald.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import java.io.IOException;

public class JinRiShiCi_API {



    // 通过token访问今日诗词的官网，直接在今日诗词官方API获取关键诗句
    public static String getKeySentenceByToken() {

        String user_agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";

        String URL = "https://v2.jinrishici.com/sentence";

        Connection conn = Jsoup.connect(URL);

        // 修改http包中的header,伪装成浏览器进行抓取
        conn.header("User-Agent", user_agent);
        String token = FileManager.applicationConfig_File.getSpecificDataInstance().Functions.DailyPoetry.JinRiShiCi.token;
        conn.header("X-User-Token", token);

        Response response_JSON = null;

        try {
            response_JSON = conn.ignoreContentType(true).execute();
        } catch (IOException e) {
            LoggerManager.reportException(e);
            return "GET KEY SENTENCE FAILED.";
        }

        /** 进行JSON解析 **/
        String result_JSON = response_JSON.body();
        JsonParser jParser = new JsonParser();
        JsonObject jo = (JsonObject) jParser.parse(result_JSON);// 构造JsonObject对象

        String result = jo.get("data").getAsJsonObject().get("content")
                .getAsString();

        /** 判断warning是否为null **/
        String warning = null;
        JsonElement je = jo.get("warning");
        if (je instanceof JsonNull) {
            warning = "NO WARNING.";
        } else {
            warning = je.getAsString();
        }

        LoggerManager.logDebug("今日诗词 - 发送附带token的get请求：token = " + token);
        LoggerManager.logDebug("今日诗词 - 发送附带token的get请求：诗句 = " + result);
        LoggerManager.logDebug("今日诗词 - 发送附带token的get请求：warning = "
                + warning);

        return result;
    }

}
