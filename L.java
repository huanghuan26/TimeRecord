package com.timerecord;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * 日志库类，提供android.util.log 自带日志的记录及本地文件记录功能 默认开启
 * 记录等级简化为 ERROR  INFO DEBUG VERBOSE 4种,  VERBOSE 不记录到文件中，只打印信息,其他等级同时进行
 * 日志存放与APK的外部存储路径中，获取路径方式：m_context.getExternalFilesDir(DIRECTORY_DOCUMENTS)
 * @author Hugh
 * @Time 2016/12/25
 */

public class L {
    /**
     * 一个将log保存到txt的工具类
     * Created by Hugh on 2016/12/19
     */
        static private String logPath = "/sdcard/LocalLog";
        static private boolean androidLogOn = true;
        static private boolean localLogOn = true;
        static private String defalutTag = "TR";            //默认标签 TR   (TimeRecord)
        static private String fileName = "Log";


        static private String fileType = "txt";
        public static final int ERROR = 2;
        public static final int INFO = 3;
        public static final int DEBUG = 4;
        public static final int VERBOSE = 5;


        /**
         * 修改log的存放路径，如 /sdcard/mylog
         *
         * @param logPath
         */
        public static void setLogPath(String logPath) {
            L.logPath = logPath;
        }

        /**
         * 修改log的文件名前缀
         *
         * @param fileName
         */
        public static void setFileName(String fileName) {
            L.fileName = fileName;
        }

        /**
         * 切换log的保存状态
         * @param androidLogOn Android自带的log开启状态
         * @param localLogOn   txt文件记录状态
         */
        public static void switchLog(boolean androidLogOn, boolean localLogOn) {
            L.androidLogOn = androidLogOn;
            L.localLogOn = localLogOn;
        }

        public static void e(String msg) {
            byte[] buffer = msg.getBytes();
            if (androidLogOn)
                Log.e(defalutTag, msg);
            if (localLogOn)
                printToFile(ERROR, defalutTag, buffer);
        }

        public static void e(String tag, String msg) {
            byte[] buffer = msg.getBytes();
            if (androidLogOn)
                Log.e(tag, msg);
            if (localLogOn)
                printToFile(ERROR, tag, buffer);
        }

        public static void i(String msg) {
            byte[] buffer = msg.getBytes();
            if (androidLogOn)
                Log.i(defalutTag, msg);
            if (localLogOn)
                printToFile(INFO, defalutTag, buffer);
        }

        public static void i(String tag, String msg) {
            byte[] buffer = msg.getBytes();
            if (androidLogOn)
                Log.i(tag, msg);
            if (localLogOn)
                printToFile(INFO, tag, buffer);
        }



        public static void d(String msg) {
            byte[] buffer = msg.getBytes();
            if (androidLogOn)
                Log.d(defalutTag, msg);
            if (localLogOn)
                printToFile(DEBUG, defalutTag, buffer);
        }

        public static void d(String tag, String msg) {
            byte[] buffer = msg.getBytes();
            if (androidLogOn)
                Log.d(tag, msg);
            if (localLogOn)
                printToFile(DEBUG, tag, buffer);
        }

        public static void v(String msg)
        {
                Log.v(defalutTag, msg);
        }
        public static void v(String tag, String msg) {
                Log.v(tag, msg);
        }


    private static void printToFile(int priority, String tag,
                                        byte[] buffer) {
            String logpath = logPath;
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);
            //int millisecond = cal.get(Calendar.MILLISECOND);

            String timeString = String.format("%d-%02d-%02d %02d:%02d:%02d",
                    year, month, day, hour, minute, second);
            String headString = String.format("\r\n%s\t(%d)\ttag:%s\tdata:",
                    timeString, priority, tag);
            byte[] headBuffer = headString.getBytes();
            String logFileName;
            switch (priority) {
                case ERROR:
                    logFileName = "%s/" + fileName + "_Error%d%02d%02d.%s";
                    break;
                case INFO:
                    logFileName = "%s/" + fileName + "_Info%d%02d%02d.%s";
                    break;
                case DEBUG:
                    logFileName = "%s/" + fileName + "_Debug%d%02d%02d.%s";
                    break;
                default:
                    logFileName = "%s/" + fileName + "%d%02d%02d.%s";
            }
            logFileName = String.format(logFileName, logpath, year, month, day, fileType);
            FileOutputStream fo = null;
            try {
                File file = new File(logFileName);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                fo = new FileOutputStream(file, true);
                fo.write(headBuffer);
                fo.write(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fo != null) {
                    try {
                        fo.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
}
