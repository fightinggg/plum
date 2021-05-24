package com.sakurawald.command;

import com.sakurawald.command.commands.*;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;

import java.util.ArrayList;

//用于管理机器人的所有指令，包括指令的注册，指令的执行分配
public class RobotCommandManager {

	public ArrayList<RobotCommand> commands = new ArrayList<RobotCommand>();

	public RobotCommandManager() {

		// 普通用户功能
		commands.add(new HelpCommand("@帮助.*"));
		commands.add(new DailyPoetryExplanationCommand("@诗歌解读.*"));
		commands.add(new ReloadCommand("@重载配置.*"));

	}

	// 判断某条信息是否为指令，快速判断，防止网络攻击，优化性能
	public boolean isCommand(String msg) {

		if (msg.charAt(0) == '@') {
			return true;
		}

		return false;
	}

	// 机器人收到的信息，无论私聊还是群聊，都调用这个函数执行
	public void receiveMessage(int subType, int msgId, long fromQQ, String msg,
			int font, long fromGroup, String fromAnonymous) {

		// [!] 验证接收到的东西是否为文本，防止对方发送 文件过来，导致报错
		if (msg.length() == 0) {
			LoggerManager.logDebug("CommandSystem", "接收-拦截长度为0的异常文本！！！");
			return;
		}

		if (FileManager.applicationConfig_File.getSpecificDataInstance().Debug.enable) {
			LoggerManager.logDebug("CommandSystem", "Receive Msg -> {" + msg + "}");
		}

		// [!] 不处理非命令的消息，优化性能
		// 不处理正常的聊天信息
		// [!] 注意！当@人的时候，电脑QQ发送收到的msg是 “@某人”，但手机发送收到的msg会转成CQ码
		if (!isCommand(msg)) {
			return;
		}

		// 循环遍历每个已注册的命令
		for (RobotCommand command : commands) {

			// 判断用户输入的是什么命令
			if (!command.isThisCommand(msg)) {
				continue;
			}

			// 判断该用户是否可以执行该命令
			if (command.runCheckUp(subType, msgId, fromQQ, msg, font,
					fromGroup, fromAnonymous)) {
				// 正式执行命令
				command.runCommand(subType, msgId, fromQQ, msg, font,
						fromGroup, fromAnonymous);
			}

			return;
		}

	}

}
