package com.sakurawald.command.commands;

import com.sakurawald.command.RobotCommand;
import com.sakurawald.command.RobotCommandChatType;
import com.sakurawald.command.RobotCommandUser;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import com.sakurawald.framework.BotManager;
import com.sakurawald.framework.MessageManager;
//从本地重新加载配置文件到内存
public class ReloadCommand extends RobotCommand {

	public ReloadCommand(String rule) {
		super(rule);
		getRange().add(RobotCommandChatType.FRIEND_CHAT);
		getRange().add(RobotCommandChatType.GROUP_TEMP_CHAT);
		getRange().add(RobotCommandChatType.GROUP_CHAT);
		getUser().add(RobotCommandUser.BOT_ADMINISTRATOR);
	}

	@Override
	public void runCommand(int subType, int msgId, long fromQQ, String msg,
			int font, long fromGroup, String fromAnonymous) {

		try {
			FileManager.applicationConfig_File.reloadFile();
		} catch (IllegalArgumentException e) {
			LoggerManager.reportException(e);
		}

		MessageManager.sendMessageBySituation(fromGroup, fromQQ, "Reload Configs Successfully!");
	}
}
