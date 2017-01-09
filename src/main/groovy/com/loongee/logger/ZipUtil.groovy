package com.loongee.logger

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

public class ZipUtil {

    public static void unzipToFolder(String srcZipFile, String destFolder) throws IOException {
        FileInputStream fileReader = new FileInputStream(srcZipFile);
        ZipInputStream zis = new ZipInputStream(fileReader);

        File rootDir = new File(destFolder);
        byte[] buffer = new byte[1024];

        try {
            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null) {
                File curFile = new File(rootDir, ze.getName());
                if (ze.isDirectory()) {
                    curFile.mkdirs();
                } else {
                    curFile.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(curFile);
                    int len = 0;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }
            }
        } finally {
            zis.close();
        }
    }

    public static void zipFolder(String srcFolder, String destZipFile) throws IOException {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;

        try {
            fileWriter = new FileOutputStream(destZipFile);
            zip = new ZipOutputStream(fileWriter);

            addFolderToZip(srcFolder, zip);
            zip.flush();
        } finally {
            zip.close();
        }
    }

    private static void addFolderToZip(String srcFolder, ZipOutputStream zip)
            throws IOException {
        File rootDir = new File(srcFolder);
        LinkedList<String> pendingDirQueue = new LinkedList<>();
        pendingDirQueue.add("");

        while (!pendingDirQueue.isEmpty()) {
            String curRelDir = pendingDirQueue.removeFirst();
            File curRelDirFile = new File(rootDir, curRelDir);
            for (String fileName : curRelDirFile.list()) {
                File item = new File(curRelDirFile, fileName);
                String curRelFilePath = (curRelDir.equals("") ? "" : curRelDir + "/") + fileName;
                if (item.isDirectory()) {
                    pendingDirQueue.add(curRelFilePath);
                    zip.putNextEntry(new ZipEntry(curRelFilePath + "/"));
                } else {
                    addFileToZip(curRelFilePath, item, zip);
                }
            }
        }
    }

    private static void addFileToZip(String relPath, File file, ZipOutputStream zip) throws IOException {
        if (file.isDirectory()) {
            return;
        }

        byte[] buf = new byte[1024];
        int len;
        FileInputStream input = new FileInputStream(file);
        zip.putNextEntry(new ZipEntry(relPath));
        while ((len = input.read(buf)) > 0) {
            zip.write(buf, 0, len);
        }
    }
}