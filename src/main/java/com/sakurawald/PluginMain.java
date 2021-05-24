package com.sakurawald;


import com.sakurawald.command.RobotCommandChatType;
import com.sakurawald.command.RobotCommandManager;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.files.ApplicationConfig_Data;
import com.sakurawald.files.FileManager;
import com.sakurawald.framework.BotManager;
import com.sakurawald.timer.RobotTimerManager;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.*;

import java.io.IOException;

public final class PluginMain extends JavaPlugin {
    private static final PluginMain INSTANCE = new PluginMain();
    private static RobotCommandManager commandManager = null;
    private static Bot CURRENT_BOT = null;

    public static PluginMain getInstance() {
        return INSTANCE;
    }

    public static Bot getCurrentBot() {
        return CURRENT_BOT;
    }

    private PluginMain() {
        super(new JvmPluginDescriptionBuilder("com.sakurawald.PlumRobot", "1.0-SNAPSHOT")
                .name("Plum")
                .author("SakuraWald")
                .build());
    }

    @Override
    public void onEnable() {
        LoggerManager.logDebug("Plum >> Enable.");
        LoggerManager.logDebug("Start Init...");

        // 初始化指令管理系统（事件驱动系统）
        commandManager = new RobotCommandManager();

        // 初始化配置文件系统
        try {
            LoggerManager.logDebug("[FileSystem]", "Init FileSystem.");
            FileManager.getInstance();
        } catch (IllegalArgumentException e) {
            LoggerManager.reportException(e);
        }


        LoggerManager.logDebug("[TimerSystem]", "Start TimerSystem.");
        // 初始化时间任务系统
        RobotTimerManager.getInstance();

        /** 群消息事件 **/
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class,
                event -> {
                    {

                        try {
                            commandManager.receiveMessage(
                                    RobotCommandChatType.GROUP_CHAT.getType(),
                                    -1, event.getSender().getId(), event
                                            .getMessage().contentToString(),
                                    -1, event.getGroup().getId(), null);
                        } catch (Exception e) {
                            LoggerManager.reportException(e);
                        }

                    }
                });


        /** 好友消息事件 **/
        GlobalEventChannel.INSTANCE.subscribeAlways(
                FriendMessageEvent.class,
                event -> {

                    {

                        try {
                            commandManager.receiveMessage(
                                    RobotCommandChatType.FRIEND_CHAT.getType(),
                                    -1, event.getSender().getId(), event
                                            .getMessage().contentToString(),
                                    -1, 0, null);
                        } catch (Exception e) {
                            LoggerManager.reportException(e);
                        }

                    }

                });

        /** 临时消息事件 **/
        GlobalEventChannel.INSTANCE.subscribeAlways(
                TempMessageEvent.class,
                event -> {

                    {

                        try {
                            commandManager.receiveMessage(
                                    RobotCommandChatType.GROUP_TEMP_CHAT
                                            .getType(), -1, event.getSender()
                                            .getId(), event.getMessage()
                                            .contentToString(), -1, event
                                            .getGroup().getId(), null);
                        } catch (Exception e) {
                            LoggerManager.reportException(e);
                        }

                    }

                });

        /** 机器人上线事件 **/
        GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, event -> {

            {
                /** 初始化Bot实例 **/
                tryInitBot(event.getBot());

            }

        });

        /** 好友请求处理事件 **/
        GlobalEventChannel.INSTANCE
                .subscribeAlways(
                        NewFriendRequestEvent.class,
                        event -> {

                            {
                                // 自动处理好友邀请
                                if (FileManager.applicationConfig_File.getSpecificDataInstance().Admin.InvitationManager.QQFriendInvitation.autoAcceptAddQQFriend) {

                                    // 同意好友添加请求
                                    event.accept();

                                    LoggerManager.logDebug(
                                            "[ContactSystem]",
                                            "Accept -> FriendAddRequest: "
                                                    + event.getFromId());


                                } else {
                                    event.reject(true);
                                }

                            }

                        });

        /** 求请求处理事件 **/
        GlobalEventChannel.INSTANCE
                .subscribeAlways(
                        BotInvitedJoinGroupRequestEvent.class,
                        event -> {

                            {

                                if (FileManager.applicationConfig_File.getSpecificDataInstance().Admin.InvitationManager.QQGroupInvitation.autoAcceptAddQQGroup) {
                                    event.accept();
                                    LoggerManager.logDebug(
                                            "[ContactSystem]",
                                            "Accept -> GroupAddRequest: "
                                                    + event.getGroupId());
                                }  // Do nothing.


                            }

                        });

        LoggerManager.logDebug("End Init...");
    }


    public void tryInitBot(Bot bot) {

        if (CURRENT_BOT == null) {
            CURRENT_BOT = bot;
        }

    }


    @Override
    public void onDisable() {
        super.getLogger().info("Plum >> Disable.");
    }

}