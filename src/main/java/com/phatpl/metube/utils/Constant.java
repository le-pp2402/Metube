package com.phatpl.metube.utils;

public class Constant {
    public static Integer VIDEO_SIZE = 10000000;
    public static Integer DEFAULT_SIZE = 100000;

    public static Integer PAGE_SIZE = 3;
    public static Integer PAGE_NUMBER = 0;

    public final static String NOT_NULL = "must not be null";
    public final static String FILE_SIZE_LARGE = "Files are too large to upload";
    public final static String INVALID_FORMAT_FILE = "FILE_UPLOAD_ERR: Invalid format file";
    public final static String POSTER = "00:00:00.001";
    public final static String TS_SECONDS = "30";


    public final static String MINIO_VIDEO_DIR = "http://localhost:8080/video/";
    public final static String SUBTITLE_EN = "/subtitle/en";
    public final static String SUBTITLE_VI = "/subtitle/vi";
    public final static String CT_APPLICATION_JSON = "application/json";

    public final static String ACCOUNT_NOT_FOUND = "Account not found";

    public final static String VIDEO_TRANSCODING_QUEUE = "video-transcoding";
    public final static String SUBTITLE_GENERATE_QUEUE = "video-transcode";
}
