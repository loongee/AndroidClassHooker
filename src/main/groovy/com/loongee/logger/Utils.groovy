package com.loongee.logger

public class Utils {
    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    public static void printVerboseMsg(msg) {
        if (HookerConfigExtension.getDefault().verbose) {
            println(msg)
        }
    }
}