package com.sakurawald.timer.timers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;

import com.sakurawald.bean.Countdown;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import com.sakurawald.framework.MessageManager;
import com.sakurawald.timer.RobotAbstractTimer;
import com.sakurawald.timer.TimerController;
import com.sakurawald.utils.DateUtil;
import com.sakurawald.utils.NetworkUtil;


public class DailySentenceTimer extends RobotAbstractTimer implements TimerController {

	public static String generateRequestURL(Calendar c) {
		String URL = "http://open.iciba.com/dsapi/?date="
				+ DateUtil.getDateSimple(c);

		return URL;
	}

	public int lastPrepareDay = 0;
	public int lastSendDay = 0;

	public String sendMsg;

	public DailySentenceTimer(String timerName, long firstTime,
							  long delayTime) {
		super(timerName, firstTime, delayTime);
		// TODO 自动生成的构造函数存根
	}


	public void errorMethod() {

		String content_cn = "不管发生什么，不管今天看起来多么糟糕，生活都会继续，明天会更好。";
		String content_en = "No matter what happens, or how bad it seems today, life does go on, and it will be better tomorrow.";
		String explaination = "小编的话：怀揣着对明天的美好期盼，时刻鼓舞自己笑着面对生活，我们的每一天都会过的阳光灿烂。";

		sendMsg = "早安，" + DateUtil.getNowYear() + "年" + DateUtil.getNowMonth()
				+ "月" + DateUtil.getNowDay() + "日！\n\n" + "●每日一句：\n"
				+ content_cn + "( " + content_en + " )" + "\n\n" + "〖解读〗"
				+ explaination + "\n\n【警告】在获取句子的时候发生了一些预期之外的问题";
	}


	@Override
	public boolean isPrepareStage() {

		int nowDay = DateUtil.getNowDay();

		if (nowDay != lastPrepareDay) {

			int nowHour = DateUtil.getNowHour();

			// 判断是否是4点，即5点之前
			if (nowHour == 4) {

				int nowMinute = DateUtil.getNowMinute();

				if (55 <= nowMinute && nowMinute <= 59) {

					lastPrepareDay = nowDay;
					return true;
				}
			}

			// 判断是否已经5点了，但是自己还没准备。也就是说，程序是在5点的时候临时运行的
			// 那么就赶快return一个true，临时准备，临时发送。两个阶段一起做
			if (nowHour == 5) {
				lastPrepareDay = nowDay;
				return true;
			}

		}

		return false;
	}

	@Override
	public boolean isSendStage() {

		int nowDay = DateUtil.getNowDay();

		if (nowDay != lastSendDay) {

			int nowHour = DateUtil.getNowHour();

			if (nowHour == 5) {
				lastSendDay = nowDay;
				return true;
			}

		}

		return false;
	}

	@Override
	public void prepareStage() {

		LoggerManager.logDebug("[TimerSystem]", "每日一句计时器：PrepareSentence");

		String HTMLSource = null;

		HTMLSource = NetworkUtil
				.getDynamicHTML(
						generateRequestURL(Calendar.getInstance()));
		String content_cn = null;
		String content_en = null;
		String explaination = null;

		sendMsg = "早安，" + DateUtil.getNowYear() + "年" + DateUtil.getNowMonth()
				+ "月" + DateUtil.getNowDay() + "日！";

		/** 倒计时功能 **/
		ArrayList<Countdown> cda = Countdown.getCountdownsByCommands();
		if (FileManager.applicationConfig_File.getSpecificDataInstance().Functions.DailyCountdown.enable
				&& cda.size() != 0) {
			sendMsg = sendMsg + "\n\n●倒计时：";

			/** 逐个添加倒计时文本 **/
			for (Countdown cd : cda) {
				sendMsg = sendMsg + "\n" + cd.getTodayCountdownMsg();
			}

		}

		sendMsg = sendMsg.trim() + "\n\n●每日一句：\n" + content_cn + "( "
				+ content_en + " )";

		// 判断今天的每日一句是否有<解读>
		if (explaination != null) {
			sendMsg = sendMsg + "\n\n" + "〖解读〗" + explaination;
		}

		LoggerManager.logDebug("[TimerSystem]", "每日一句：\n" + sendMsg);

	}

	@Override
	public void sendStage() {
		try {
			MessageManager.sendMessageToAllQQFriends(sendMsg);
		} catch (IOException e) {
			LoggerManager.reportException(e);
		}

		MessageManager.sendMessageToAllQQGroups(sendMsg);
	}

	@Override
	public void logDebugTimerState() {
		LoggerManager.logDebug("[TimerSystem]", "每日一句计时器：Run");
		LoggerManager.logDebug("[TimerSystem]", "每日一句计时器: lastPrepareDay = "
				+ lastPrepareDay);
		LoggerManager.logDebug("[TimerSystem]", "每日一句计时器: lastSendDay = "
				+ lastSendDay);
		LoggerManager.logDebug("[TimerSystem]",
				"每日一句计时器: nowDay = " + DateUtil.getNowDay());
	}

	@Override
	public void run() {
		logDebugTimerState();
		autoControlTimer();
	}


}
