package com.sakurawald.command;

import com.sakurawald.PluginMain;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.FileManager;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;


//用于描述一个指令支持的使用者的地位
//地位的判断是依据本地config.yml决定的。判断标准是QQ号码

/*	本机器人的权限系统：
 * 
 * 	1. 普通用户（NORMAL_USER）
 * 	2. 管理（ADMINISTRATOR）
 * 		2.1 群主
 * 		2.2 管理员
 * 	3. 超级管理 （SUPER_ADMINISTRATOR）
 * 
 * 
 *  注意：
 *  1. 在群聊中，权限分为 普通用户，管理，超级管理
 *  2. 在私聊中，权限分为 普通用户，超级管理
 * 
 *  
 *  */

public enum RobotCommandUser {
	NORMAL_USER(1), GROUP_ADMINISTRATOR(2), GROUP_OWNER(3), BOT_ADMINISTRATOR(4);

	/** 判断用户的权限. **/
	public static int getAuthority(long fromGroup, long fromQQ) {

		int authority = 0;

		// 先判断是否为群消息
		if (fromGroup != 0) {
			authority = RobotCommandUser.getAuthorityByQQ(fromGroup, fromQQ);
		} else {
			// 再判断是否是私聊消息
			authority = RobotCommandUser.getAuthorityByQQ(fromQQ);
		}

		LoggerManager.logDebug("[Permission]", "fromGroup = " + fromGroup + ", fromQQ = " + fromQQ + ", authority："
				+ authority);

		return authority;
	}

	/** 单纯通过QQ，判断对方是不是超级管理 **/
	public static int getAuthorityByQQ(long QQ) {

		for (Long botAdministrator : FileManager.applicationConfig_File.getSpecificDataInstance().Admin.botAdministrators) {
			if (botAdministrator.equals(QQ)) {
				return BOT_ADMINISTRATOR.getUser();
			}
		}

		return NORMAL_USER.getUser();
	}

	/** 判断群聊中，对方是不是管理（管理员或群主） **/
	public static int getAuthorityByQQ(long fromGroup, long fromQQ) {

		Member m = PluginMain.getCurrentBot().getGroup(fromGroup)
				.get(fromQQ);

		// [!] 首先判断是不是管理员
		// 防止自己是超级管理员，但又是普通群员
		if (getAuthorityByQQ(fromQQ) == RobotCommandUser.BOT_ADMINISTRATOR
				.getUser()) {
			return RobotCommandUser.BOT_ADMINISTRATOR.getUser();
		}

		// 首先判断是不是普通群员，以提高性能
		if (m.getPermission() == MemberPermission.MEMBER) {
			return RobotCommandUser.NORMAL_USER.getUser();
		}

		if (m.getPermission() == MemberPermission.OWNER) {
			return RobotCommandUser.GROUP_OWNER.getUser();
		}

		if (m.getPermission() == MemberPermission.ADMINISTRATOR) {
			return RobotCommandUser.GROUP_ADMINISTRATOR.getUser();
		}

		return RobotCommandUser.NORMAL_USER.getUser();
	}

	int user = 0;

	RobotCommandUser(int user) {
		this.user = user;
	}

	public int getUser() {
		return user;
	}

}
