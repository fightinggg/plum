package com.sakurawald.files;

import com.sakurawald.debug.LoggerManager;

import java.io.File;
import java.io.IOException;

/**
 * 用于管理所有的本地配置文件，包括每一个配置文件的命名
 **/
public class FileManager {

    /**
     * 单例模式
     */
    private static FileManager instance = null;

    public static FileManager getInstance() {

        if (instance == null) {
            instance = new FileManager();
        }

        return instance;
    }

    private FileManager() {
        // Do nothing.
    }

    /** Config Instances. **/
    public static ApplicationConfig_File applicationConfig_File = null;


    /**
     * 调用本方法来<初始化>配置文件系统.
     */
    public void init() throws IllegalArgumentException,
            IllegalAccessException, IOException {

        LoggerManager.logDebug("FileSystem", "Init All Configs...", true);


        // ApplicationConfig.json
        LoggerManager.logDebug("FileSystem", "Init >> ApplicationConfig.json", true);
        applicationConfig_File = new ApplicationConfig_File(ConfigFile.getApplicationConfigPath(),
                "ApplicationConfig.json", ApplicationConfig_Data.class);
        applicationConfig_File.init();


    }

}
