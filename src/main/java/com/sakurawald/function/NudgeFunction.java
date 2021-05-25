package com.sakurawald.function;

import com.sakurawald.PluginMain;
import com.sakurawald.api.HitoKoto_API;
import com.sakurawald.api.ThirdPartyRandomImage_API;
import com.sakurawald.files.FileManager;
import com.sakurawald.framework.MessageManager;
import com.sakurawald.utils.NetworkUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

public class NudgeFunction {

    public static void handleEvent(NudgeEvent event) {

        if (!FileManager.applicationConfig_File.getSpecificDataInstance().Functions.NudgeFunction.enable) {
            return;
        }

        long fromGroup = -1;
        if (event.getSubject() instanceof Group) {
            fromGroup = ((Group) event.getSubject()).getId();
        }

        long fromQQ = event.getFrom().getId();
        long targetQQ = event.getTarget().getId();

        // Has Nudge Bot ?
        if (targetQQ == PluginMain.getCurrentBot().getId()) {
            String sendMsg = HitoKoto_API.getRandomSentence().getFormatedString();

            try {
                // Add RandomImage.
                String randomImageURL = ThirdPartyRandomImage_API.getInstance().getRandomImageURL();
                Image uploadImage = ExternalResource.uploadAsImage(NetworkUtil.getInputStream(randomImageURL),
                        PluginMain.getCurrentBot().getGroups().stream().findAny().get());
                sendMsg = sendMsg + "\n" + "[mirai:image:" + uploadImage.getImageId() + "]";
            } catch (Exception e) {
                // Do nothing.
            }

            MessageManager.sendMessageBySituation(fromGroup, fromQQ, sendMsg);
        }

    }

}
