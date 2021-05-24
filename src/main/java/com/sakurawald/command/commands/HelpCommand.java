package com.sakurawald.command.commands;

import com.sakurawald.command.RobotCommand;
import com.sakurawald.command.RobotCommandChatType;
import com.sakurawald.command.RobotCommandUser;
import com.sakurawald.framework.BotManager;
import com.sakurawald.framework.MessageManager;

//@帮助 指令
public class HelpCommand extends RobotCommand {

	public HelpCommand(String rule) {
		super(rule);
		getRange().add(RobotCommandChatType.FRIEND_CHAT);
		getRange().add(RobotCommandChatType.GROUP_TEMP_CHAT);
		getRange().add(RobotCommandChatType.GROUP_CHAT);

		getUser().add(RobotCommandUser.NORMAL_USER);
		getUser().add(RobotCommandUser.GROUP_ADMINISTRATOR);
		getUser().add(RobotCommandUser.GROUP_OWNER);
		getUser().add(RobotCommandUser.BOT_ADMINISTRATOR);
	}

	public String addAdministratorHelp(String result) {
		return result;
	}

	public String addPersonalHelp(String result) {
		return result;
	}

	public String addSuperAdministratorHelp(String result) {
		result = result + "\n--------------机器人管理--------------\n"
				+ "@重载配置      重新加载配置文件";

		return result;
	}

	@Override
	public void runCommand(int subType, int msgId, long fromQQ, String msg,
			int font, long fromGroup, String fromAnonymous) {

		String result = "----------------帮助----------------\n"
				+ "@帮助      查看使用帮助\n"
				+ "@诗词解读      查看今天的每日诗歌的解读";

		int authority = RobotCommandUser.getAuthority(fromGroup, fromQQ);

		// 判断用户是非超级管理，且在群中使用帮助，则隐藏关于个人独立配置的命令
		if (fromGroup == 0) {
			result = addPersonalHelp(result);
		} else {

			if (authority == RobotCommandUser.BOT_ADMINISTRATOR.getUser()) {
				result = addPersonalHelp(result);
			}

		}

		// 首先判断是否为超级管理QQ
		if (authority == RobotCommandUser.BOT_ADMINISTRATOR.getUser()) {
			result = addAdministratorHelp(result);
			result = addSuperAdministratorHelp(result);
		} else {

			// 判断是否为管理QQ(群主+管理员)
			if (authority == RobotCommandUser.GROUP_ADMINISTRATOR.getUser()
					|| authority == RobotCommandUser.GROUP_OWNER.getUser()) {
				result = addAdministratorHelp(result);
			}

		}

		// 处理完文本后，最后发送文本
		MessageManager.sendMessageBySituation(fromGroup, fromQQ, result);

	}

}
