package com.sakurawald.timer.timers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sakurawald.api.JinRiShiCi_API;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.framework.BotManager;
import com.sakurawald.framework.MessageManager;
import com.sakurawald.timer.RobotAbstractTimer;
import com.sakurawald.timer.TimerController;
import com.sakurawald.utils.DateUtil;
import com.sakurawald.utils.NetworkUtil;
import sun.rmi.runtime.Log;

public class DailyPoetry_Timer extends RobotAbstractTimer implements TimerController {

	public static final String SINGLE_SENTENCE_PUNCTUATION = ",|，|，|。|、|\\?|？|!|！|；|：";

	public String title = null;
	public String author = null;
	public String content = null;
	public String sendMsg = null;
	public int lastPrepareDay = 0;
	public int lastSendDay = 0;

	// 单例设计
	private static DailyPoetry_Timer instance = new DailyPoetry_Timer(
			"今日诗词", 1000 * 6, 1000 * 60);

	public DailyPoetry_Timer(String timerName, long firstTime, long delayTime) {
		super(timerName, firstTime, delayTime);
	}

	/** 获取诗歌中的第一个句子，以标点符号为标准 **/
	public static String getFirstSentence(String poetry_Content) {

		String[] sentences = null;

		// 限制，split为两部分。第一句 和 除第一句以外的部分
		sentences = poetry_Content.split(
				DailyPoetry_Timer.SINGLE_SENTENCE_PUNCTUATION, 2);

		return sentences[0];
	}



	public static DailyPoetry_Timer getInstance() {
		return instance;
	}

	/** 输入一句诗句，通过标点符号，对诗句进行再一次细分，返回细分部分的最长句子 **/
	public static String getMaxKeySentencePart(String singleSentence) {

		String result = "";

		String[] sentences;

		sentences = singleSentence.split(SINGLE_SENTENCE_PUNCTUATION);

		for (String s : sentences) {

			if (result.length() < s.trim().length()) {
				result = s.trim();
			}

		}

		return result;
	}

	public String dynasty = null;
	public String explanation = null;

	public String authorIntroduction = null;
	// [!] 因为每天的每个不同小时，不同分钟，获取到的诗歌内容都是随机的
	// 所以要把获取到的诗歌内容存起来!!
	public String todayPoetryHTML = null;
	// 中国诗歌网
	public String todayPoetryURL = null;

	public int retryCount = 0;

	
	

	// 对获取到的诗歌注释进行格式美化
	public String formatNotes(String notes) {

		/** 将诗歌网页编者写的一些奇形怪状的索引稍做处理 **/
		notes = notes.replace("⒈", "(1)").replace("⒉", "(2)")
				.replace("⒊", "(3)").replace("⒋", "(4)").replace("⒌", "(5)")
				.replace("⒍", "(6)").replace("⒎", "(7)").replace("⒏", "(8)")
				.replace("⒐", "(9)").replace("⒑", "(0)").replace("⒒", "(11)")
				.replace("⒓", "(12)").replace("⒔", "(13)").replace("⒕", "(14)")
				.replace("⒖", "(15)").replace("⒗", "(16)").replace("⒘", "(17)")
				.replace("⒙", "(18)").replace("⒚", "(19)").replace("⒛", "(20)")
				.replace("⑴", "(1)").replace("⑵", "(2)").replace("⑶", "(3)")
				.replace("⑷", "(4)").replace("⑸", "(5)").replace("⑹", "(6)")
				.replace("⑺", "(7)").replace("⑻", "(8)").replace("⑼", "(9)")
				.replace("⑽", "(10)").replace("⑾", "(11)").replace("⑿", "(12)")
				.replace("⒀", "(13)").replace("⒁", "(14)").replace("⒂", "(15)")
				.replace("⒃", "(16)").replace("⒄", "(17)").replace("⒅", "(18)")
				.replace("⒆", "(19)").replace("⒇", "(20)").replace("①", "(1)")
				.replace("②", "(2)").replace("③", "(3)").replace("④", "(4)")
				.replace("⑤", "(5)").replace("⑥", "(6)").replace("⑦", "(7)")
				.replace("⑧", "(8)").replace("⑨", "(9)").replace("⑩", "(10)")
				.replace("㈠", "(1)").replace("㈡", "(2)").replace("㈢", "(3)")
				.replace("㈣", "(4)").replace("㈤", "(5)").replace("㈥", "(6)")
				.replace("㈦", "(7)").replace("㈧", "(8)").replace("㈨", "(9)")
				.replace("㈩", "(10)");

		/** 开始进行格式化 **/

		int index = 1;

		String[] ns = notes
				.split("\\(\\d{1,3}\\)|\\[\\d{1,3}\\]|〔\\d{1,3}〕|\\d{1,3}\\.|\\d{1,3}、");

		StringBuffer result = new StringBuffer();

		for (String s : ns) {

			// [!] 此处对单条注释进行trim，防止本来百度文库的每条注释，结尾都有换行符，
			// 最终导致换行符过多，格式难看！
			s = s.trim();

			// 如果分割到的单个文本是空的，则忽略
			if (s.equals("")) {
				continue;
			}

			if (index == ns.length) {
				result.append(index + ". " + s);
			} else {
				result.append(index + ". " + s + "\n");
			}

			index++;
		}

		// /** 特殊情况：有些古老的百度汉语页面，直接用换行符来作索引
		// * 则return 的notes只有1条注释，这是不太好看的，要格式化
		// * **/
		// if (index == 2) {
		// String[] arr = notes.split("\n");
		// return HTTPUtil.getStringAndSortByStringArr(arr);
		// }

		return result.toString();
	}


	public String getAuthor(String HTTP) {

		String rule = "<a class=\"poem-detail-header-author\"[\\s\\S]*?>[\\s\\S]*?<span class=\"poem-info-gray\"> 【作者】 </span>([\\s\\S]*?)</a>";
		Pattern p = Pattern.compile(rule, Pattern.DOTALL);

		Matcher m = p.matcher(HTTP);
		// 神奇的Bug，必须要find()，不能用matches

		if (m.find()) {
			String author = m.group(1);
			author = NetworkUtil.deleteHTMLTag(author);
			author = author.trim();

			return author;
		} else {
			return "佚名";
		}

	}

	public String getAuthorIntroduction(String HTTP) {

		String rule = "<div class=\"poem-author-intro\"[\\s\\S]*?>([\\s\\S]*?)</div>";
		Pattern p = Pattern.compile(rule, Pattern.DOTALL);

		Matcher m = p.matcher(HTTP);
		// 神奇的Bug，必须要find()，不能用matches

		String author_introduction = null;

		if (m.find()) {
			author_introduction = NetworkUtil.deleteHTMLTag(NetworkUtil
					.decodeHTML(m.group(1)));
			// 针对处理
			author_introduction = author_introduction.replace("来源：古诗文网", "");
			author_introduction = author_introduction.replace("百科详情", "");
			author_introduction = author_introduction.trim();

			// [!] 防止有些偷懒的小编，直接写个百科详情跳转，什么简介都不写
			if (author_introduction.equals("")) {
				return "无";
			}

			return author_introduction;
		} else {
			return "无";
		}
	}

	public String getBaiduHanYuURLByKeySentence(String keySentence) {

		String result = null;

		// [!] 对关键句子，再次分解，按标点符号
		String keySentencePart = getMaxKeySentencePart(keySentence);
		LoggerManager.logDebug("[TimerSystem]", "每日古代诗（关键句子的关键部分）：" + keySentencePart);

		// keySentencePart = "春蚕食叶响回廊";
		// keySentencePart = "人间四月芳菲尽";
		// keySentencePart = "此日六军同驻马";
		// keySentencePart = "暮投石壕村";
		// keySentencePart = "人家见生男女好";
		// keySentencePart = "日高烟敛";

		// [!] 此处要进行URL转码，否则会获取HTTP失败!
		result = "https://hanyu.baidu.com/s?wd=+"
				+ NetworkUtil.getURLEncoderString(keySentencePart)
				+ NetworkUtil.getURLEncoderString("+诗歌") + "&from=poem";

		LoggerManager.logDebug("[TimerSystem]", "每日古代诗（百度汉语URL）：" + result);

		return result;
	}

	public String getContent(String HTTP) {

		String rule = "<(div|p) class=\"poem-detail-main-text\"[\\s\\S]*?>([\\s\\S]*?)</\\1>";
		Pattern p = Pattern.compile(rule, Pattern.DOTALL);

		Matcher m = p.matcher(HTTP);
		// 神奇的Bug，必须要find()，不能用matches

		String result = "";

		/**
		 * [!] 本处大量运用正则表达式， 警惕繁琐的正则表达式，导致程序卡死，出现奇怪的BUG
		 **/

		while (m.find()) {

			result = result
					+ NetworkUtil.decodeHTML(NetworkUtil.deleteHTMLTag(m
							.group(2)));

			// 每句结束，手动换行
			result = result + "\n";

		}

		// 先替换<br>换行符
		result = NetworkUtil.decodeHTML(result);
		// 再剔除多余的文本
		result = NetworkUtil.deleteHTMLTag(result);
		result = result.trim();
		// 针对百度文库的特别处理
		result = result.replace(" ", "");

		if (result.equals("")) {
			return "每日一诗内容获取失败!";
		}

		return result;
	}

	public String getDynasty(String HTTP) {

		String rule = "<span class=\"poem-detail-header-author\">[\\s\\S]*?<span class=\"poem-info-gray\">[\\s\\S]*?【朝代】[\\s\\S]*?</span>([\\s\\S]*?)</span>";
		Pattern p = Pattern.compile(rule, Pattern.DOTALL);

		Matcher m = p.matcher(HTTP);
		// 神奇的Bug，必须要find()，不能用matches

		if (m.find()) {
			return m.group(1).trim();
		} else {
			return "不详";
		}
	}

	// 获取每日一诗的页面URL
	public String getEveryDayPoetryURL() {

		String result = null;
		String keySentence = null;

		keySentence = JinRiShiCi_API.getKeySentenceByToken();
		LoggerManager.logDebug("[TimerSystem]", "每日古代诗（关键句子）：" + keySentence);

//		/**
//		 * 写到临时配置，让当天21点获取到的关键诗句，被保存下来， 以防程序重启，内存消失，句子再也找不到（随机获取）
//		 **/
//		TempData_Data.setTempData(
//				"EveryDayPoetry.PoetryTypes.AncientPoetry.KeySentence",
//				keySentence);

		result = getBaiduHanYuURLByKeySentence(keySentence);

		return result;
	}

	public String getExplanation(String HTTP) {

		String rule = "<div class=\"poem-detail-item-content means-fold\">([\\s\\S]*?)</div>";
		Pattern p = Pattern.compile(rule, Pattern.DOTALL);

		Matcher m = p.matcher(HTTP);
		// 神奇的Bug，必须要find()，不能用matches

		if (m.find()) {
			return m.group(1).trim();
		} else {
			return "无";
		}
	}

	public String getNotes(String HTTP) {

		String rule = "<b>[\\s\\S]*?注释[\\s\\S]*?</b>[\\s\\S]*?</a>[\\s\\S]*?</div>[\\s\\S]*?<div class=\"poem-detail-separator\">[\\s\\S]*?</div>[\\s\\S]*?<div class=\"poem-detail-item-content\">([\\s\\S]*?)</div>";
		Pattern p = Pattern.compile(rule, Pattern.DOTALL);

		Matcher m = p.matcher(HTTP);
		// 神奇的Bug，必须要find()，不能用matches

		String notes = null;

		if (m.find()) {

			notes = NetworkUtil.deleteHTMLTag(NetworkUtil.decodeHTML(m
					.group(1)));

			// 针对处理
			notes = notes.replace("来源：古诗文网", "");
			notes = formatNotes(notes);

			notes = notes.trim();

			return notes;
		} else {
			return "无";
		}
	}


	public String getTitle(String HTTP) {
		String rule = "<div class=\"poem-detail-item\" id=\"poem-detail-header\">[\\s\\S]*?<h1>([\\s\\S]*?)</h1>";
		Pattern p = Pattern.compile(rule, Pattern.DOTALL);

		Matcher m = p.matcher(HTTP);

		// 神奇的Bug，必须要find()，不能用matches

		if (m.find()) {
			return m.group(1).trim();
		} else {
			return "每日一诗标题获取失败!";
		}

	}

	// 判断是否是准备阶段
	@Override
	public boolean isPrepareStage() {

		int nowDay = DateUtil.getNowDay();

		if (nowDay != lastPrepareDay) {

			int nowHour = DateUtil.getNowHour();

			if (nowHour == 20) {

				int nowMinute = DateUtil.getNowMinute();

				if (55 <= nowMinute && nowMinute <= 59) {
					lastPrepareDay = nowDay;
					return true;
				}
			}

			if (nowHour == 21) {

				lastPrepareDay = nowDay;

				return true;
			}

		}

		return false;
	}

	// 判断是否是发送阶段
	@Override
	public boolean isSendStage() {

		int nowDay = DateUtil.getNowDay();

		if (nowDay != lastSendDay) {

			int nowHour = DateUtil.getNowHour();

			if (nowHour == 21) {
				lastSendDay = nowDay;
				return true;
			}

		}

		return false;
	}

	// 输入百度汉语的HTTP,判断该HTTP是否为某一个具体诗歌的详细页面
	public boolean isValiedHTTP(String HTTP) {

		if (HTTP == null) {
			return false;
		}

		if (HTTP.indexOf("poem-detail-item-content") != -1) {
			return true;
		}

		return false;
	}

	@Override
	public void prepareStage() {

		LoggerManager.logDebug("[TimerSystem]", "每日一诗(古代诗)计时器：preparePoetry");

//		/** 预防错误的解决方案 **/
//		defaultMethod();

		/** 准备sendMsg **/

		String HTTP;
		String URL;

		URL = getEveryDayPoetryURL();
		HTTP = NetworkUtil.getDynamicHTML(URL);

		/** 正常运行 **/
		// [!] 存一份备份，方便@诗歌解读使用
		todayPoetryHTML = HTTP;
		todayPoetryURL = URL;

		title = getTitle(HTTP);
		author = getAuthor(HTTP);
		dynasty = getDynasty(HTTP);
		content = getContent(HTTP);

		sendMsg = "晚安，" + DateUtil.getNowYear() + "年"
				+ DateUtil.getNowMonth() + "月" + DateUtil.getNowDay()
				+ "日~\n\n" + "●每日一诗\n" + "〖标题〗" + title + "\n" + "〖作者〗"
				+ "（" + getDynasty(HTTP) + "） " + author + "\n" + "〖诗歌〗\n"
				+ content;

		LoggerManager.logDebug("[TimerSystem]", "每日一诗：\n" + sendMsg);

	}

	@Override
	public void run() {

		logDebugTimerState();

		// 新建一个线程去执行
		new Thread() {
			@Override
			public void run() {

				// 判断是否为准备阶段
				if (isPrepareStage()) {
					prepareStage();
				}

				// 判断是否为发送阶段
				if (isSendStage()) {
					sendStage();
				}

			}

		}.start();

	}

	@Override
	public void sendStage() {

		LoggerManager.logDebug("[TimerSystem]", "每日一诗(古代诗)计时器：sendPoetry");

		try {
			MessageManager.sendMessageToAllQQFriends(sendMsg);
		} catch (IOException e) {
			LoggerManager.reportException(e);
		}

		MessageManager.sendMessageToAllQQGroups(sendMsg);
	}
}
