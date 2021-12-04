package com.luck.picture.lib.config;

import android.Manifest;

/**
 * @author：luck
 * @data：2017/5/24 1:00
 * @describe : constant
 */
public final class PictureConfig {
    /**
     * 读写权限
     */
    public final static String[] READ_WRITE_EXTERNAL_STORAGE =
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
    /**
     * 写入权限
     */
    public final static String[] WRITE_EXTERNAL_STORAGE = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 相机权限
     */
    public final static String[] CAMERA = new String[]{Manifest.permission.CAMERA};

    /**
     * 录音权限
     */
    public final static String[] RECORD_AUDIO = new String[]{Manifest.permission.RECORD_AUDIO};

    public final static String EXTRA_PICTURE_SELECTOR_CONFIG = "PictureSelectorConfig";

    public final static String CAMERA_FACING = "android.intent.extras.CAMERA_FACING";

    public final static String EXTRA_ALL_FOLDER_SIZE = "all_folder_size";

    public final static String EXTRA_CURRENT_FIRST_PATH = "current_first_path";

    public final static String EXTRA_QUICK_CAPTURE = "android.intent.extra.quickCapture";

    public final static int MAX_PAGE_SIZE = 60;

    public final static int MIN_PAGE_SIZE = 10;

    public final static int CAMERA_BEFORE = 1;

    public final static long MB = 1048576;

    public final static int DEFAULT_SPAN_COUNT = 4;

    public final static int REQUEST_CAMERA = 909;

    public final static int REQUEST_GO_SETTING = 1102;
}
