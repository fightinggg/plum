package com.sakurawald.files;

import java.util.ArrayList;

public class ApplicationConfig_Data {

	public Debug Debug = new Debug();
	public class Debug {
		public boolean enable = true;
	}

	public Admin Admin = new Admin();
	public class Admin {

		public String RobotName = "Plum";

		// 设置机器人要如何处理群添加邀请和好友添加邀请
		// [!] 是决定是否自动处理请求。若为false，则机器人会直接拒绝邀请
		public InvitationManager InvitationManager = new InvitationManager();
		public class InvitationManager {

			public QQFriendInvitation QQFriendInvitation = new QQFriendInvitation();
			public class QQFriendInvitation {
				public boolean autoAcceptAddQQFriend = false;
			}

			public QQGroupInvitation QQGroupInvitation = new QQGroupInvitation();
			public class QQGroupInvitation {
				public boolean autoAcceptAddQQGroup = false;
			}
		}

		// 机器人的核心控制配置
		public RobotControl RobotControl = new RobotControl();
		public class RobotControl {
			public boolean forceCancel_GroupMessage = false;
			public boolean forceCancel_FriendMessage = true;
		}

		// 管理员的QQ
		// [!] 在config.yml文件中，如果只有1个QQ号位管理员，则YAML文档里看，和长整数差不多
		// 但是要记住，administors在YAML中，是按String存储的，必要时，要加上双引号，防止报错
		public ArrayList<Long> botAdministrators = new ArrayList<Long>() {
			{
				this.add(3172906506L);
				this.add(1839395230L);
				this.add(526026058L);
				this.add(1966914133L);
			}
		};

	}

	public Functions Functions = new Functions();
	public class Functions {

		public DailyCountdown DailyCountdown = new DailyCountdown();
		public class DailyCountdown {
			public boolean enable = true;
			public String countdown_commands = "◆距离2021年高考还有$diff_days天！|1623027804000|高考加油！&高考加油！&2021年高考已结束~[DIV]◆距离2020年考研还有$diff_days天！|1608512604000|考研加油&考研加油&考研加油&2020年考研已结束~[DIV]◆距离2020年四六级考试还有$diff_days天！|1600477404000|四六级考试加油！&四六级考试已结束~";
		}

		public DailyPoetry DailyPoetry = new DailyPoetry();
		public class DailyPoetry {
			public JinRiShiCi JinRiShiCi = new JinRiShiCi();
			public class JinRiShiCi {
				public String token = "paOa0DqOdpLn4FVVHNtEDgU5Imk89kXZ";

			}
			public boolean explanation_Enable = true;
		}

	}

	public Systems Systems = new Systems();
	public class Systems {

		public SendSystem SendSystem = new SendSystem();
		public class SendSystem {

			public SendDelay SendDelay = new SendDelay();
			public class SendDelay {

				public SendToFriends SendToFriends = new SendToFriends();
				public class SendToFriends {
					public boolean enable = false;
					public long delayTimeMS = 0;
				}

				public SendToGroups SendToGroups = new SendToGroups();
				public class SendToGroups {
					public boolean enable = true;
					public long delayTimeMS = 1000;
				}

			}

			public int sendMsgMaxLength = 4500;
		}

	}

}
