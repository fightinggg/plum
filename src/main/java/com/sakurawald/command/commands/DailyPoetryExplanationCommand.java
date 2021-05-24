package com.sakurawald.command.commands;

import java.io.IOException;


import com.sakurawald.command.RobotCommand;
import com.sakurawald.command.RobotCommandChatType;
import com.sakurawald.command.RobotCommandUser;
import com.sakurawald.files.FileManager;
import com.sakurawald.utils.DateUtil;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.framework.MessageManager;
import com.sakurawald.utils.NetworkUtil;
import com.sakurawald.timer.timers.DailyPoetry_Timer;

public class DailyPoetryExplanationCommand extends RobotCommand {

	public static String sendMsg = null;
	public static int lastGetDay = 0;


	public DailyPoetryExplanationCommand(String rule) {
		super(rule);
		getRange().add(RobotCommandChatType.FRIEND_CHAT);
		getRange().add(RobotCommandChatType.GROUP_TEMP_CHAT);
		getRange().add(RobotCommandChatType.GROUP_CHAT);

		getUser().add(RobotCommandUser.NORMAL_USER);
		getUser().add(RobotCommandUser.GROUP_ADMINISTRATOR);
		getUser().add(RobotCommandUser.GROUP_OWNER);
		getUser().add(RobotCommandUser.BOT_ADMINISTRATOR);
	}

	public boolean hasCache() {

		int nowDay = DateUtil.getNowDay();

		// 如果今天和上次获取文章时上同一天，则不需要再重复从网络上获取
		if (nowDay == lastGetDay) {
			return true;
		}

		// 若之前没获取过，则记录一下，然后获取
		lastGetDay = nowDay;
		return false;
	}

	// 要求21点后，也就是机器人发送完每日一诗后，才可以使用解读
	public boolean canUse() {
		int nowHour = DateUtil.getNowHour();
		return nowHour >= 21;
	}

	@Override
	public void runCommand(int subType, int msgId, long fromQQ, String msg,
			int font, long fromGroup, String fromAnonymous) {

		if (!FileManager.applicationConfig_File.getSpecificDataInstance().Functions.DailyPoetry.explanation_Enable) {
			MessageManager.sendMessageBySituation(fromGroup, fromQQ,
					"很抱歉，诗歌解读功能已暂停使用~");
			return;
		}

		// 判断是否可以使用
		if (!canUse()) {
			MessageManager.sendMessageBySituation(fromGroup, fromQQ,
					"很抱歉，这个时间暂时还不能使用该指令~");
			return;
		}

		// 判断是否使用之前缓存好的数据
		String sendMsg = null;

		/** 准备sendMsg **/
		if (hasCache() == false) {

			String HTTP = DailyPoetry_Timer.getInstance().todayPoetryHTML;

			/**
			 * [!] 若机器人在21点之后，也就是22点，23点重启，则用户使用@诗歌解读，会NPE报错
			 * 原因是古代史Timer的todayHTTP为null，没有被prepare赋值。
			 * 即使重新prepare，也会因时间不同，而导致获取到的诗歌解析也不同。 因此，若遇到这种情况，就暂时禁止使用诗歌解读
			 * **/
			if (HTTP == null) {

//					String keySentence = (String) TempData_Data
//							.getTempData("EveryDayPoetry.PoetryTypes.AncientPoetry.KeySentence");

				String keySentence = null;

				String URL = DailyPoetry_Timer.getInstance()
						.getBaiduHanYuURLByKeySentence(keySentence);

				LoggerManager
						.logDebug("[命令系统]",
								"@诗歌解读：发现古代诗Timer对象的HTTP异常，尝试从本地硬盘获取关键句子，并恢复上次获取的HTTP！");
				LoggerManager.logDebug("[命令系统]", "@诗歌解读：恢复诗歌关键诗句："
						+ keySentence);

				HTTP = NetworkUtil.getDynamicHTML(URL);

				DailyPoetry_Timer.getInstance().todayPoetryHTML = HTTP;
				/** 若本程序在21点后，因重启而错过古代诗的prepareStage，则在诗歌解读里，重新获取HTTP **/
			}

			DailyPoetryExplanationCommand.sendMsg = "诗歌解读，"
					+ DateUtil.getNowYear()
					+ "年"
					+ DateUtil.getNowMonth()
					+ "月"
					+ DateUtil.getNowDay()
					+ "日！\n\n"
					+ "●每日一诗\n"
					+ "〖标题〗"
					+ DailyPoetry_Timer.getInstance().getTitle(
					HTTP)
					+ "\n"
					+ "〖作者〗"
					+ "（"
					+ DailyPoetry_Timer.getInstance()
					.getDynasty(HTTP)
					+ "） "
					+ DailyPoetry_Timer.getInstance().getAuthor(
					HTTP)
					+ "\n"
					+ "〖作者简介〗\n"
					+ DailyPoetry_Timer.getInstance()
					.getAuthorIntroduction(HTTP)
					+ "\n"
					+ "〖译文〗\n"
					+ DailyPoetry_Timer.getInstance()
					.getExplanation(HTTP)
					+ "\n"
					+ "〖注释〗\n"
					+ DailyPoetry_Timer.getInstance().getNotes(
					HTTP) + "\n";
			;

			DailyPoetryExplanationCommand.sendMsg = DailyPoetryExplanationCommand.sendMsg.trim();

		}

		sendMsg = DailyPoetryExplanationCommand.sendMsg;

		/** 字数检测 **/
		String defaultMsg = "诗歌解读，" + DateUtil.getNowYear() + "年"
				+ DateUtil.getNowMonth() + "月" + DateUtil.getNowDay()
				+ "日！\n\n" + "●每日一诗\n" + "〖链接〗由于本次诗歌解读文本过长，请直接点击链接查看："
				+ "\n" + "https://hanyu.baidu.com/";

		/** 发送sendMsg **/
		MessageManager.sendMessageBySituation(fromGroup, fromQQ, sendMsg);
	}

}
