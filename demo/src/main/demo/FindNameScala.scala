import org.apache.spark.{SparkConf, SparkContext}

import java.io._
import java.util.zip._
import java.nio.file.{Paths, Path}

object FindNameScala {
  def main(args: Array[String]): Unit = {
    val zipFilePath = "src/main/demo/Files.zip" // ZIP文件路径
    val unzipFolderPath = "src/main/demo/" // 解压后的输出文件夹（当前目录）
    val targetContent = "MSC2023" // 目标文件内容


    // 1. 解压文件
    unzip(zipFilePath, unzipFolderPath)


    // 2. 开始寻找
    val conf = new SparkConf().setMaster("local").setAppName("ParallelFileSearch")
    val sc = new SparkContext(conf)
    val folderPath = "src/main/demo/Files" // 文件夹路径
    val textFiles = sc.wholeTextFiles(folderPath) // 读取所有文件的内容，返回(文件名，内容)对
    val matchingFiles = textFiles.filter { case (_, content) => content.trim == targetContent }
    if (matchingFiles.isEmpty()) {
      println("Target file not found.")
    } else {
      val (filePath, _) = matchingFiles.first() // 获取第一个匹配的文件名
      val outputFile = new java.io.PrintWriter("src/main/demo/target.txt") // 创建目标文件
      val fileName = extractFileName(filePath) // 提取文件名
      outputFile.println(fileName) // 写入文件名
      outputFile.close()
      println(s"Target file name written to target.txt: $fileName")
    }
    sc.stop()

  }


  /**
   * @author wbw
   * @param zipFilePath     压缩文件路径
   * @param unzipFolderPath 解压缩目录
   */
  def unzip(zipFilePath: String, unzipFolderPath: String): Unit = {
    val buffer = new Array[Byte](1024)
    val zipFile = new ZipFile(zipFilePath)

    val entries = zipFile.entries()

    while (entries.hasMoreElements) {
      val entry = entries.nextElement()
      val entryName = entry.getName
      val entryPath = s"$unzipFolderPath/$entryName"

      if (!entry.isDirectory) {
        val inputStream = zipFile.getInputStream(entry)
        val outputStream = new BufferedOutputStream(new FileOutputStream(entryPath))

        var bytesRead = 0
        while ( {
          bytesRead = inputStream.read(buffer);
          bytesRead != -1
        }) {
          outputStream.write(buffer, 0, bytesRead) // 从输入流读取内容并写入到输出流
        }
        outputStream.close()
        inputStream.close()
      } else {
        new File(entryPath).mkdirs()
      }
    }
    zipFile.close()
    println("Unzip completed.")
  }


/**
   * 从文件路径中提取文件名
   * @param filePath 文件路径
   * @return 文件名
   */
  def extractFileName(filePath: String): String = {
    val lastIndex = filePath.lastIndexOf('/')
    if (lastIndex != -1) {
      filePath.substring(lastIndex + 1)
    } else {
      filePath
    }
  }

}
