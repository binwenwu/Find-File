import java.io.*;
import java.util.zip.*;

/**
 * @author wbw
 */
public final class FindNameJava {
    public static void main(String[] args) {

        // 压缩文件路径
        String zipFilePath = "src/main/demo/Files.zip";
        // 解压缩目录
        String unzipFolderPath = "src/main/demo/";
        // 目标文件内容
        String targetContent = "MSC2023";


        // 1. 解压文件
        try {
            unzip(zipFilePath, unzipFolderPath);
            System.out.println("Unzip operation successful.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. 开始遍历寻找
        String targetFileName = findTargetFileName("src/main/demo/Files", targetContent);
        if (targetFileName != null) {
            String outputFileName = "src/main/demo/target.txt";
            try (PrintWriter writer = new PrintWriter(outputFileName)) {
                writer.println(targetFileName);
                System.out.println("Target file name written to " + outputFileName + ": " + targetFileName);
            } catch (IOException e) {
                System.out.println("Error writing to " + outputFileName + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Target file not found.");
        }


    }


    /**
     * 解压缩
     * @author wbw
     * @param zipFilePath 压缩文件路径
     * @param outputFolder 解压缩目录
     */
    public static void unzip(String zipFilePath, String outputFolder) throws IOException {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String entryName = zipEntry.getName();
                File newFile = new File(outputFolder, entryName);
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, length);
                        }
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }


    /**
     * 遍历文件夹，寻找目标文件名，返回文件名
     * @author wbw
     * @param folderPath 文件夹路径
     * @param targetContent 目标文件内容
     * @return 目标文件名
     */
    public static String findTargetFileName(String folderPath, String targetContent) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        StringBuilder content = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            content.append(line);
                        }
                        if (content.toString().equals(targetContent)) {
                            // 找到对应文件
                            return file.getName();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // 未找到
        return null;
    }
}
