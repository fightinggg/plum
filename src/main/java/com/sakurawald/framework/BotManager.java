package com.sakurawald.framework;

import com.sakurawald.PluginMain;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.ApplicationConfig_Data;
import com.sakurawald.files.FileManager;
import net.mamoe.mirai.contact.*;
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

	public static ContactList<Stranger> getAllStrangers() {
		return PluginMain.getCurrentBot().getStrangers();
	}



}
