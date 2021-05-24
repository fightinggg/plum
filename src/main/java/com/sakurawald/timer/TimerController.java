package com.sakurawald.timer;

public interface TimerController {

    boolean isPrepareStage();

    boolean isSendStage();

    void prepareStage();

    void sendStage();

    default void autoControlTimer() {
        new Thread(() -> {
            if (isPrepareStage()) {
                prepareStage();
            }

            if (isSendStage()) {
                sendStage();
            }
        }).start();
    }


}
