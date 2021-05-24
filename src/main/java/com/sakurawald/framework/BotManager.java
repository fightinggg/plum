package com.sakurawald.framework;

import com.sakurawald.PluginMain;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.ApplicationConfig_Data;
import com.sakurawald.files.FileManager;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;

public class BotManager {

	public static ContactList<Friend> getAllQQFriends() {
		return PluginMain.getCurrentBot().getFriends();
	}

	public static ContactList<Group> getAllQQGroups() {
		return PluginMain.getCurrentBot().getGroups();
	}




}
