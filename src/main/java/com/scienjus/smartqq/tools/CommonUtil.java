package com.scienjus.smartqq.tools;

import com.scienjus.smartqq.constant.OsNameEnum;

/**
 * Created by zhf on 2018/4/26.
 */
public class CommonUtil {

    public static OsNameEnum getOsNameEnum() {
        String os = System.getProperty("os.name").toUpperCase();
        if (os.indexOf(OsNameEnum.DARWIN.toString()) >= 0) {
            return OsNameEnum.DARWIN;
        } else if (os.indexOf(OsNameEnum.WINDOWS.toString()) >= 0) {
            return OsNameEnum.WINDOWS;
        } else if (os.indexOf(OsNameEnum.LINUX.toString()) >= 0) {
            return OsNameEnum.LINUX;
        } else if (os.indexOf(OsNameEnum.MAC.toString()) >= 0) {
            return OsNameEnum.MAC;
        }
        return OsNameEnum.OTHER;
    }
    public static boolean printQCode(String path) {
        Runtime runtime = Runtime.getRuntime();
        switch (getOsNameEnum()) {
            case WINDOWS:
                    try {
                        runtime.exec("cmd /c start " + path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                break;
            case MAC:
                     try {
                        runtime.exec("open " + path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                break;

            default:
                break;
        }
        return true;
    }
}
