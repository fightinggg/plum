package com.sakurawald.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.meterware.httpunit.WebClient;
import com.sakurawald.debug.LoggerManager;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class NetworkUtil {

	// [!] 这里设置与URL有关的变量，ENCODE表示目标网页的编码
	private final static String ENCODE = "utf-8";

	// 去除HTML标签
	public static String deleteHTMLTag(String htmlStr) {
		final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
		final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
		final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
		// final String regEx_space = "\\s*|\t|\r|\n";// 定义空格回车换行符
		final String regEx_w = "<w[^>]*?>[\\s\\S]*?<\\/w[^>]*?>";// 定义所有w标签

		Pattern p_w = Pattern.compile(regEx_w, Pattern.CASE_INSENSITIVE);
		Matcher m_w = p_w.matcher(htmlStr);
		htmlStr = m_w.replaceAll(""); // 过滤script标签

		Pattern p_script = Pattern.compile(regEx_script,
				Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(htmlStr);
		htmlStr = m_script.replaceAll(""); // 过滤script标签

		Pattern p_style = Pattern
				.compile(regEx_style, Pattern.CASE_INSENSITIVE);
		Matcher m_style = p_style.matcher(htmlStr);
		htmlStr = m_style.replaceAll(""); // 过滤style标签

		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(htmlStr);
		htmlStr = m_html.replaceAll(""); // 过滤html标签

		// 为了不破坏原始数据中的空白和换行，这里要注释掉
		// Pattern p_space = Pattern.compile(regEx_space,
		// Pattern.CASE_INSENSITIVE);
		// Matcher m_space = p_space.matcher(htmlStr);
		// htmlStr = m_space.replaceAll(""); // 过滤空格回车标签
		// htmlStr = htmlStr.replaceAll(" ", ""); // 过滤

		return htmlStr.trim(); // 返回文本字符串
	}

	// 动态获取网页的HTML源码
	public static String getDynamicHTML(String URL) {




		return null;
	}

	public static String getStaticHTML(String URL) {

		String user_agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
		Connection conn = Jsoup.connect(URL);
		// 修改http包中的header,伪装成浏览器进行抓取
		conn.header("User-Agent", user_agent);
		Document doc = null;
		try {
			doc = conn.get();
		} catch (IOException e) {
			LoggerManager.reportException(e);
		}

		return doc.toString();
	}

	public static String betweenString(String text, String left, String right) {
		String result = "";
		int zLen;
		if (left == null || left.isEmpty()) {
			zLen = 0;
		} else {
			zLen = text.indexOf(left);
			if (zLen > -1) {
				zLen += left.length();
			} else {
				zLen = 0;
			}
		}
		int yLen = text.indexOf(right, zLen);
		if (yLen < 0 || right.isEmpty()) {
			yLen = text.length();
		}
		result = text.substring(zLen, yLen);
		return result;
	}


	public static String getURLDecoderString(String str) {
		String result = "";
		if (null == str) {
			return "";
		}
		try {
			result = java.net.URLDecoder.decode(str, ENCODE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getURLEncoderString(String str) {
		String result = "";
		if (null == str) {
			return "";
		}
		try {
			result = java.net.URLEncoder.encode(str, ENCODE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String decodeHTML(String htmlStr) {

		htmlStr = htmlStr.replace("<br>", "\n").replace("&nbsp;", " ")
				.replace("&gt;", "");

		return htmlStr;
	}
}
