package cn.geodata.service;

import cn.geodata.dao.CatalogDao;
import cn.geodata.dao.SingleFileDao;
import cn.geodata.entity.base.Catalog;
import cn.geodata.entity.base.ChildrenData;
import cn.geodata.entity.data.SingleFile;
import cn.geodata.enums.ContentTypeEnum;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import cn.geodata.utils.FileUtils;
import cn.geodata.utils.SnowflakeIdWorker;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@Slf4j
@Service
public class SingleFileService {
    @Autowired
    SingleFileDao singleFileDao;

    @Autowired
    CatalogDao catalogDao;

    @Autowired
    CatalogService catalogService;

    @Value("${resourcePath}")
    private  String resourcePath;

    Logger logger = Logger.getLogger(SingleFileService.class.getName());

    /**
     * @description: 新建文件，如果文件 md5 存在就只是更新 singleFile 的属性
     * @author: Tian
     * @date: 2022/1/5 9:58
     * @param multipartFile
     * @param catalogId
     * @return: entity.data.SingleFile
     */
    public SingleFile create(MultipartFile multipartFile, String catalogId, String name, String description) throws Exception{
        try {
            String md5 = DigestUtils.md5DigestAsHex(multipartFile.getInputStream());
            String uploadPath = resourcePath + "/singleFile/";
            // String name = multipartFile.getName();
            SingleFile singleFile = singleFileDao.findOneByMd5(md5);
            if(singleFile == null) {
                Map<String, String> nameList = new LinkedHashMap<>();
                nameList.put(catalogId, name);
                singleFile = new SingleFile(SnowflakeIdWorker.generateId2(), md5, nameList, 1, (int)multipartFile.getSize(), 1, new Date(), Boolean.FALSE);
                if(FileUtils.uploadSingleFile(multipartFile, uploadPath, singleFile.getId()) && catalogService.updateWithFile(singleFile.getId(), name, catalogId, description)) {
                    singleFileDao.save(singleFile);
                    logger.info("create a new singleFile success: " + singleFile.toString());
                    return singleFile;
                } else {
                    throw new Exception("create a new singleFile fail.");
                }
            } else {
                Map<String, String> nameList = singleFile.getNameList();
                nameList.put(catalogId, name);
                singleFile.setNameList(nameList);
                singleFile.setParentNumber(nameList.size());
                singleFile.setUseNumber(singleFile.getUseNumber() + 1);
                catalogService.updateWithFile(singleFile.getId(), name, catalogId, description);
                singleFileDao.save(singleFile);
                logger.info("create a file with updating singleFile: " + singleFile.toString());
                return singleFile;
            }
        } catch (Exception err) {
            logger.warning("create a file error: " + err.toString());
            throw err;
        }
    }

    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/19 10:07
     * @param id
     * @param catalogId
     * @param response
     * @param type
     * @return: boolean
     */
    public boolean download(String id, String catalogId, HttpServletResponse response, String type) throws Exception {
        Boolean flag = false;
        String uploadPath = resourcePath + "/singleFile/";
        SingleFile singleFile = singleFileDao.findOneById(id);
        if(singleFile != null) {
            File file = new File(uploadPath + singleFile.getId());
            String fileName = singleFile.getNameList().get(catalogId);
            if(file.exists() && fileName != null) {
                flag = downLoadFile(response, file, fileName, type);
            }
        }
        return flag;
    }

    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/19 10:07
     * @param response
     * @param file
     * @param fileName
     * @param type
     * @return: boolean
     */
    public boolean downLoadFile(HttpServletResponse response, File file, String fileName, String type) throws UnsupportedEncodingException {
        boolean downLoadLog = false;
        log.info("文件大小" + file.length());
        if(type!=null){
            String contentType = ContentTypeEnum.getContentTypeByName(type).getText();
            response.setContentType(contentType);
        }else {
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + new String(fileName.getBytes(StandardCharsets.UTF_8),
                    "ISO8859-1"));
            response.setContentLength((int) file.length());

        }

        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            OutputStream outputStream = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                outputStream.write(buffer, 0, i);
                i = bis.read(buffer);
            }
            downLoadLog = true;
            //return "下载成功";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return downLoadLog;
    }


    /**
     * @description: 删除文件，同时更新父目录和文件
     * @author: Tian
     * @date: 2022/1/4 16:16
     * @param fileId
     * @param catalogId
     * @return: java.lang.Boolean
     */
    public Boolean delete(String fileId, String catalogId){
        try {
            // 更新父目录
            String uploadPath = resourcePath + "/singleFile/";
            Catalog catalog =  catalogDao.findOneById(catalogId);
            List<ChildrenData> children = catalog.getChildren();
            Iterator<ChildrenData> iterator = children.iterator();
            while(iterator.hasNext()) {
                ChildrenData temp = iterator.next();
                if(temp.getId().equals(fileId)) {
                    iterator.remove();
                    break;
                }
            }
            catalogDao.save(catalog);

            // 更新文件，如果 parentNumber 为 0 则删除文件
            SingleFile singleFile = singleFileDao.findOneById(fileId);
            Map<String, String> nameList = singleFile.getNameList();
            nameList.remove(catalogId);
            if(nameList.size() != 0) {
                singleFile.setNameList(nameList);
                singleFile.setParentNumber(nameList.size());
                singleFile.setUseNumber(singleFile.getUseNumber() + 1);
                singleFileDao.save(singleFile);
            } else {
                File file = new File(uploadPath + singleFile.getId());
                if(file.exists()) {
                    file.delete();
                } else {
                    logger.warning(String.format("delete file but file %s is not exists", singleFile.getId()));
                }
            }
            logger.info(String.format("delete file %s success.", singleFile.getId()));
            return Boolean.TRUE;
        } catch (Exception err) {
            logger.warning("delete file error: " + err.toString());
            return Boolean.FALSE;
        }
    }
}
