package com.sakurawald.function;

import com.sakurawald.PluginMain;
import com.sakurawald.api.HitoKoto_API;
import com.sakurawald.files.FileManager;
import com.sakurawald.framework.MessageManager;
import net.mamoe.mirai.event.events.NudgeEvent;

public class NudgeFunction {

    public static void handleEvent(NudgeEvent event) {

        if (!FileManager.applicationConfig_File.getSpecificDataInstance().Functions.NudgeFunction.enable) {
            return;
        }

        long fromGroup = event.getSubject().getId();
        long fromQQ = event.getFrom().getId();
        long targetQQ = event.getTarget().getId();

        // Has Nudge Bot ?
        if (targetQQ == PluginMain.getCurrentBot().getId()) {
            String sendMsg = HitoKoto_API.getRandomSentence().getFormatedString();
            MessageManager.sendMessageBySituation(fromGroup, fromQQ, sendMsg);
        }

    }

}
