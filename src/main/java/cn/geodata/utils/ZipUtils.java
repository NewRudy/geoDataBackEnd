package cn.geodata.utils;

import cn.geodata.entity.base.ChildrenData;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static void zipFiles(HttpServletResponse response, List<File> fileList, File zipPath, String[] fileListName) {
        // 1 文件压缩
        if (!zipPath.exists()) { // 判断压缩后的文件存在不，不存在则创建
            try {
                zipPath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fileOutputStream=null;
        ZipOutputStream zipOutputStream=null;
        FileInputStream fileInputStream=null;
        try {
            fileOutputStream=new FileOutputStream(zipPath); // 实例化 FileOutputStream对象
            zipOutputStream=new ZipOutputStream(fileOutputStream); // 实例化 ZipOutputStream对象
            ZipEntry zipEntry=null; // 创建 ZipEntry对象
            for (int i=0; i<fileList.size(); i++) { // 遍历源文件数组
                fileInputStream = new FileInputStream(fileList.get(i)); // 将源文件数组中的当前文件读入FileInputStream流中
                zipEntry = new ZipEntry(fileListName[i]); // 实例化ZipEntry对象，源文件数组中的当前文件
                zipOutputStream.putNextEntry(zipEntry);
                int len; // 该变量记录每次真正读的字节个数
                byte[] buffer=new byte[1024]; // 定义每次读取的字节数组
                while ((len=fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            assert fileInputStream != null;
            fileInputStream.close();
            fileOutputStream.close();

            // 2 文件下载
            long currentTime=System.currentTimeMillis(); // 当时时间戳
            int randomFour=(int)((Math.random()*9+1)*1000); // 4位随机数
            String fileName=String.valueOf(currentTime)+String.valueOf(randomFour)+".zip"; // 新的文件名称
            String path=zipPath.toString();

            // 设置输出的格式
            response.reset();
            response.setContentType("bin");
            response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // 循环取出流中的数据
            FileInputStream inStream=new FileInputStream(path); // 读到流中
            byte[] b = new byte[100];
            int len;
            try {
                OutputStream os=response.getOutputStream();
                response.setContentType("application/octet-stream");
                while ((len = inStream.read(b)) > 0){
                    os.write(b, 0, len);
                }
                inStream.close();
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {  // 3 删除压缩包
                String path=zipPath.toString();
                File zfile = new File(path);
                zfile.delete();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void zipFiles(HttpServletResponse response, List<File> fileList, File zipPath) {
        // 1 文件压缩
        if (!zipPath.exists()) { // 判断压缩后的文件存在不，不存在则创建
            try {
                zipPath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fileOutputStream=null;
        ZipOutputStream zipOutputStream=null;
        FileInputStream fileInputStream=null;
        try {
            fileOutputStream=new FileOutputStream(zipPath); // 实例化 FileOutputStream对象
            zipOutputStream=new ZipOutputStream(fileOutputStream); // 实例化 ZipOutputStream对象
            ZipEntry zipEntry=null; // 创建 ZipEntry对象
            for (File file : fileList) { // 遍历源文件数组
                fileInputStream = new FileInputStream(file); // 将源文件数组中的当前文件读入FileInputStream流中
                zipEntry = new ZipEntry(file.getName()); // 实例化ZipEntry对象，源文件数组中的当前文件
                zipOutputStream.putNextEntry(zipEntry);
                int len; // 该变量记录每次真正读的字节个数
                byte[] buffer = new byte[1024]; // 定义每次读取的字节数组
                while ((len = fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            assert fileInputStream != null;
            fileInputStream.close();
            fileOutputStream.close();

            // 2 文件下载
            long currentTime=System.currentTimeMillis(); // 当时时间戳
            int randomFour=(int)((Math.random()*9+1)*1000); // 4位随机数
            String fileName=String.valueOf(currentTime)+String.valueOf(randomFour)+".zip"; // 新的文件名称
            String path=zipPath.toString();

            // 设置输出的格式
            response.reset();
            response.setContentType("bin");
            response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // 循环取出流中的数据
            FileInputStream inStream=new FileInputStream(path); // 读到流中
            byte[] b = new byte[100];
            int len;
            try {
                OutputStream os=response.getOutputStream();
                response.setContentType("application/octet-stream");
                while ((len = inStream.read(b)) > 0){
                    os.write(b, 0, len);
                }
                inStream.close();
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {  // 3 删除压缩包
                String path=zipPath.toString();
                File zfile = new File(path);
                zfile.delete();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
