package com.sakurawald.framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.sakurawald.PluginMain;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.ApplicationConfig_Data;
import com.sakurawald.files.FileManager;
import net.mamoe.mirai.contact.BotIsBeingMutedException;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.*;

//用于管理Message的类
public class MessageManager {

	// 对sendMsg进行字数上限检测
	public static String checkSendMsgLength(String sendMsg) {
		return checkSendMsgLength(sendMsg, "很抱歉，本次发送的字数超过上限，已取消发送！\n字数："
				+ sendMsg.length());
	}

	// 对sendMsg进行字数上限检测
	public static String checkSendMsgLength(String sendMsg, String defaultMsg) {

		// 防NPE
		if (sendMsg == null) {
			return null;
		}

		LoggerManager.logDebug("[SendSystem]",
				"checkSendMsgLength : length = " + sendMsg.length());

		if (sendMsg.length() >= FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.sendMsgMaxLength) {
			return defaultMsg;
		} else {
			return sendMsg;
		}

	}

	// 转换sendMsg中的特殊代码
	public static String transSpecialCode(String message) {
		String newMessage = message.replace("#SPACE", " ")
				.replace("#space", " ").replace("#ENTER", "\n")
				.replace("#enter", "\n");
		return newMessage;
	}

	// 通过传入一个群名，判断该群是否为0，从而知道该消息是好友消息，还是QQ群消息。从而分情况回复
	// 本方法可以智能回复 好友信息，群聊信息，群临时会话
	public static int sendMessageBySituation(long fromGroup, long fromQQ,
											 String msg) {

		LoggerManager.logDebug("SendSystem", "sendBySituation: fromGroup = " + fromGroup
				+ ", fromQQ = " + fromQQ);

		if (fromGroup == 0) {
			return MessageManager.sendMessageToQQFriend(fromGroup, fromQQ,
					MessageUtils.newChain(new PlainText(msg)));
		} else {
			Member member = PluginMain.getCurrentBot().getGroup(fromGroup)
					.get(fromQQ);
			MessageManager.sendMessageToQQGroup(fromGroup, MessageUtils
					.newChain(new At(member.getId())).plus("\n" + msg));
		}

		return -1;
	}

	public static int sendMessageToQQFriend(long fromGroup, long QQ,
											MessageChain messageChain) {

		LoggerManager.logDebug("SendSystem", "sendMessageToQQFriend: " + QQ);

		Friend friend = PluginMain.getCurrentBot().getFriend(QQ);
		if (friend == null) {
			Member member = PluginMain.getCurrentBot().getGroup(fromGroup)
					.get(QQ);
		}
		friend.sendMessage(messageChain);

		return 200;
	}

	public static int sendMessageToQQFriend(long fromGroup, long QQ,
											String msg) {
		/** 对发送的文本进行字数检测 **/
		msg = checkSendMsgLength(msg);

		return sendMessageToQQFriend(fromGroup, QQ, MessageUtils.newChain(new PlainText(msg)));
	}


	public static void sendDelay(boolean isSendToGroups) {

		long delayTimeMS = 0;

		if (isSendToGroups) {
			if (FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.SendDelay.SendToGroups.enable) {
				delayTimeMS = FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.SendDelay.SendToGroups.delayTimeMS;
			}
		} else {
			if (FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.SendDelay.SendToFriends.enable) {
				delayTimeMS = FileManager.applicationConfig_File.getSpecificDataInstance().Systems.SendSystem.SendDelay.SendToFriends.delayTimeMS;
			}
		}

		LoggerManager.logDebug("GuardSystem", "Send Delay" + delayTimeMS + "毫秒~");
		try {
			Thread.sleep(delayTimeMS);
		} catch (InterruptedException e) {
			LoggerManager.reportException(e);
		}

	}

	// 给所有的QQ好友发送消息
	public static void sendMessageToAllQQFriends(String msg) throws IOException {

		// [!] 使用for，不用foreach，避免便利数据时删除或增加数据，导致报错
		// [!] 在删除ArrayList时注意倒叙遍历，以防陷入陷阱！！！
		LoggerManager.logDebug("SendSystem", "sendMessageToAllQQFriends: totally"
				+ BotManager.getAllQQFriends().getSize() + " friends.");

		/** 机器人行为核心控制 **/
		if (FileManager.applicationConfig_File.getSpecificDataInstance().Admin.RobotControl.forceCancel_FriendMessage) {
			LoggerManager.logDebug("SendSystem", "Force Cancel Send!");
			return;
		}

		ContactList<Friend> friends = BotManager.getAllQQFriends();
		for (Friend friend : friends) {

			sendDelay(false);



			friend.sendMessage(msg);

			int code = -1;

			LoggerManager.logDebug("[SendSystem]", "本次发送返回的code：" + code);

		}
	}

	// 给所有的群发送消息
	public static void sendMessageToAllQQGroups(String msg) {

		ContactList<Group> groups = PluginMain.getCurrentBot().getGroups();

		LoggerManager.logDebug("[SendSystem]", "sendMessageToAllQQGroups: totally " + groups.size()
				+ " groups.");


		/** 机器人行为核心控制 **/
		if (FileManager.applicationConfig_File.getSpecificDataInstance().Admin.RobotControl.forceCancel_GroupMessage == true) {
			LoggerManager.logDebug("[SendSystem]", "Force Cancel Send!");
			return;
		}

		/** 对发送的文本进行字数检测 **/
		 msg = checkSendMsgLength(msg);

		for (Group group : groups) {

			sendDelay(true);

			try {
				group.sendMessage(msg);
			} catch (BotIsBeingMutedException e) {
				LoggerManager.logDebug("SendSystem",
						"机器人在该群中被禁言: Group = " + group.getId());
			}
		}

	}

	public static void sendMessageToQQGroup(long group, MessageChain messageChain) {
		LoggerManager.logDebug("[SendSystem]", "给某个QQ群发送信息-QQ群的号码为：" + group);

		PluginMain.getCurrentBot().getGroup(group).sendMessage(messageChain);
	}

	public static void sendMessageToQQGroup(long group, String msg) {
		/** 对发送的文本进行字数检测 **/
		msg = checkSendMsgLength(msg);

		sendMessageToQQGroup(group, MessageUtils.newChain(new PlainText(msg)));
	}


}
