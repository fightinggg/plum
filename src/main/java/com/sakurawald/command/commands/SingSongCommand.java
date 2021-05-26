package com.sakurawald.command.commands;

import com.sakurawald.PluginMain;
import com.sakurawald.api.KugouMusicAPI;
import com.sakurawald.api.MusicPlatAPI;
import com.sakurawald.api.NeteaseCloudMusicAPI;
import com.sakurawald.bean.SongInformation;
import com.sakurawald.command.RobotCommand;
import com.sakurawald.command.RobotCommandChatType;
import com.sakurawald.command.RobotCommandUser;
import com.sakurawald.debug.LoggerManager;
import com.sakurawald.exception.CanNotDownloadFileException;
import com.sakurawald.exception.FileTooBigException;
import com.sakurawald.files.FileManager;
import com.sakurawald.framework.MessageManager;
import com.sakurawald.function.SingManager;

import com.sakurawald.utils.LanguageUtil;
import io.github.mzdluo123.silk4j.AudioUtils;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.Voice;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingSongCommand extends RobotCommand {

	private static final Pattern pattern = Pattern
			.compile("^(?:(?:唱歌)|(?:唱)|(?:点歌)|(?:听歌)|(?:我想听)|(?:来首)|(?:想听)|(?:给我唱))\\s?([\\s\\S]*)$");

	public static final String RANDOM_SING_FLAG = "-random";

	public static void sendMusic(long QQGroup, String voice_file_name) {

		LoggerManager.logDebug("SingSong", "sendMusic() -> voice_file_name = "
				+ voice_file_name, true);


		// MP3 -> Silk
		File uploadVoiceFile = new File(MusicPlatAPI.getVoicesPath() + voice_file_name);
		File silkVoiceFile = null;
		try {
			LoggerManager.logDebug("SingSong", "Start MP3 to Silk: " + voice_file_name);
			silkVoiceFile = AudioUtils.mp3ToSilk(uploadVoiceFile);
			LoggerManager.logDebug("SingSong", "Finish MP3 to Silk: " + voice_file_name);
		} catch (IOException e) {
			LoggerManager.reportException(e);
		}

		// Send SilkVoiceFile.
		LoggerManager.logDebug("SingSong", "Ready to send VoiceFile: voice_file_name = "
				+ voice_file_name, true);
		Voice uploadVoice = PluginMain.getCurrentBot().getGroup(QQGroup).uploadVoice(ExternalResource.create(silkVoiceFile));
		PluginMain.getCurrentBot().getGroup(QQGroup).sendMessage(uploadVoice);
	}

	public SingSongCommand(String rule) {
		super(rule);
		getRange().add(RobotCommandChatType.GROUP_CHAT);

		getUser().add(RobotCommandUser.NORMAL_USER);
		getUser().add(RobotCommandUser.GROUP_ADMINISTRATOR);
		getUser().add(RobotCommandUser.GROUP_OWNER);
		getUser().add(RobotCommandUser.BOT_ADMINISTRATOR);
	}

	@Override
	public void runCommand(int msgType, int time, long fromGroup, long fromQQ, MessageChain messageChain) {

		/** 功能开关判断 **/
		if (! FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.enable) {
			return;
		}

		/** 引导式帮助 **/
		String msg = messageChain.contentToString();
		if (msg
				.matches("^(?:(?:唱歌)|(?:唱)|(?:点歌)|(?:听歌)|(?:我想听)|(?:来首)|(?:想听)|(?:给我唱))[\\s]*$")) {
			String help =  "用法示例：\n"
					+ "\"唱歌 霜雪千年\"" + "\n"
					+ "\"唱歌 霜雪千年 " + RANDOM_SING_FLAG +"\""
					+ "\n\n●注意\n"
					+ "○指令后面必须添加1个空格" + "\n"
					+ "○若搜索到的歌曲不是你想要的版本，则需要手动指定歌手~" + "\n"
					+ "○付费原唱歌曲无法被指定，将自动选择合适的翻唱版本！";

			MessageManager.sendMessageBySituation(fromGroup, fromQQ, help);
			return;
		}

		/** 唱歌指令判断逻辑 **/
		final Matcher matcher = pattern.matcher(msg);

		// 判断 是否符合 唱歌指令要求
		if (matcher.find()) {

			/** Function系统 **/
			new Thread(new Runnable() {

				@Override
				public void run() {

					/** 变量定义 **/
					MusicPlatAPI mpa;
					boolean random_music_flag;

					LoggerManager.logDebug("SingSong", "收到唱歌指令，开始执行核心代码", true);

					/** 判断唱歌间隔是否合法 **/
					if (!SingManager.getInstance().canUse(fromGroup)) {
						MessageManager.sendMessageBySituation(fromGroup, fromQQ, FileManager.applicationConfig_File.getSpecificDataInstance().Functions.FunctionManager.callTooOftenMsg);
						LoggerManager.logDebug("SingSongFunction", "Call too often. Cancel!", true);
						return;
					}

					/**
					 * 更新唱歌间隔 [!] 只要执行了唱歌核心代码，无论最后是否成功发送语音文件，都更新lastSingTime
					 * **/
					SingManager.getInstance().updateUseTime(fromGroup);

					/** SongInformation获取逻辑 **/
					String input_music_name = matcher.group(1);

					LoggerManager.logDebug("SingSong",
							"用户输入的需要唱的歌曲: input_music_name = "
									+ input_music_name, true);

					// [!] 使用用户输入的歌曲名在网络上找歌曲
					SongInformation si = null;
					random_music_flag = SingManager.getInstance().isRandomSing(
							msg);

					// 获得干净的音乐名
					input_music_name = SingManager.getInstance().deleteParams(
							input_music_name);

					/** 尝试第一音库: 网易云音乐 **/
					LoggerManager.logDebug("SingSong",
							"即将尝试第一音库 - 网易云音乐: input_music_name = "
									+ input_music_name, true);
					mpa = NeteaseCloudMusicAPI.getInstance();
					si = mpa.checkAndGetSongInformation(input_music_name,
							random_music_flag);

					/** 尝试第二音库: 酷狗音乐 **/
					if (si == null) {
						LoggerManager.logDebug("SingSong",
								"即将尝试第二音库 - 酷狗音乐: input_music_name = "
										+ input_music_name, true);
						mpa = KugouMusicAPI.getInstance();
						si = mpa.checkAndGetSongInformation(input_music_name,
								random_music_flag);
					}

					/** 搜索不到指定的音乐, 结束代码 **/
					if (si == null) {
						LoggerManager.logDebug("SingSong",
								"所唱的歌曲搜索不到, 结束代码: input_music_name = "
										+ input_music_name, true);
						MessageManager.sendMessageBySituation(fromGroup, fromQQ,
								LanguageUtil
										.transObject_X(
												1,
												FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.not_found_music_msg,
												input_music_name));
						return;
					}

					/** 音乐文件下载逻辑 **/
					// 若音乐文件不存在时，尝试下载音乐
					String path = mpa.getDownloadPath(si.getMusic_Name(),
							si.getMusic_ID());

					try {
						mpa.downloadMusic(si);
					} catch (CanNotDownloadFileException e) {
						MessageManager.sendMessageBySituation(fromGroup, fromQQ, FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.music_need_paid_msg);
						return;
					} catch (FileTooBigException e) {
						MessageManager.sendMessageBySituation(fromGroup, fromQQ, FileManager.applicationConfig_File.getSpecificDataInstance().Functions.SingSongFunction.download_music_file_too_big_msg);
						return;
					}

					/** 音乐发送逻辑 **/
					sendMusic(
							fromGroup,
							mpa.getDownloadFileName(si.getMusic_Name(), si.getMusic_ID()));
				}
			}).start();

		}
	}

}
