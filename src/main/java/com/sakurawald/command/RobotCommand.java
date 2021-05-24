package com.sakurawald.command;

import com.sakurawald.framework.BotManager;
import com.sakurawald.framework.MessageManager;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//用于描述一条命令
public abstract class RobotCommand {

	// 表示该指令的使用范围，允许什么类型的聊天使用
	private ArrayList<RobotCommandChatType> ranges = new ArrayList<RobotCommandChatType>();
	private ArrayList<RobotCommandUser> users = new ArrayList<RobotCommandUser>();
	// 表示匹配到该指令的正则表达式
	private String rule;
	private Pattern pattern;

	public RobotCommand(String rule) {
		this.rule = rule;
		this.pattern = Pattern.compile(rule);

	}

	public ArrayList<RobotCommandChatType> getRange() {
		return ranges;
	}

	public ArrayList<RobotCommandUser> getUser() {
		return users;
	}

	// 判断用户使用的是不是这个指令
	public boolean isThisCommand(String msg) {
		Matcher m = pattern.matcher(msg);

		return m.matches();
	}

	// 在执行该指令前，先检查该指令是否符合规定
	public boolean runCheckUp(int subType, int msgId, long fromQQ, String msg,
			int font, long fromGroup, String fromAnonymous) {

		/** 指令的使用范围检测 **/

		// 检查是否符合该指令的使用范围（聊天类型）
		boolean flag = false;
		for (RobotCommandChatType type : ranges) {

			if (subType == type.getType()) {
				flag = true;
				break;
			}
		}

		if (flag == false) {
			MessageManager.sendMessageBySituation(fromGroup, fromQQ,
					"很抱歉，该指令不能在当前聊天类型中使用");
			return false;
		}

		// 开始下一项检测前，重置Flag
		flag = false;

		/** 指令的使用权限检测 **/

		// 检查该指令的使用者是否有权限（注意：权限是看该用户在QQ群里的地位）
		// 定义权限，再通过QQ号赋值权限
		int authority = RobotCommandUser.getAuthority(fromGroup, fromQQ);
		for (RobotCommandUser user : users) {

			if (authority == user.getUser()) {

				flag = true;
				break;
			}

		}
		if (flag == false) {
			MessageManager.sendMessageBySituation(fromGroup, fromQQ,
					"很抱歉，您没有权限使用该指令~");
			return false;
		}

		return true;
	}

	// 正式执行该指令
	public abstract void runCommand(int subType, int msgId, long fromQQ,
			String msg, int font, long fromGroup, String fromAnonymous);
}
