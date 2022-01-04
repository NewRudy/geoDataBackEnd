package service;

import dao.CatalogDao;
import dao.SingleFileDao;
import entity.base.Catalog;
import entity.base.ChildrenData;
import entity.data.SingleFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import utils.FileUtils;
import utils.SnowflakeIdWorker;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.logging.Logger;

public class SingleFileService {
    @Autowired
    SingleFileDao singleFileDao;

    @Autowired
    CatalogDao catalogDao;

    @Autowired
    CatalogService catalogService;

    @Value("${resourcePath}")
    private  String resourcePath;

    private String uploadPath = resourcePath + "/singleFile/";

    Logger logger = Logger.getLogger(SingleFileService.class.getName());

    /**
     * @description: 新建文件，如果文件 md5 存在就只是更新 singleFile 的属性
     * @author: Tian
     * @date: 2022/1/4 15:22
     * @param	multipartFile
     * @param	catalogId
     * @return: java.lang.Boolean
     */
    public Boolean create(MultipartFile multipartFile, String catalogId) throws Exception {
        try {
            String md5 = DigestUtils.md5DigestAsHex(multipartFile.getInputStream());
            String name = multipartFile.getName();
            SingleFile singleFile = singleFileDao.findOneByMd5(md5);
            if(singleFile == null) {
                Map<String, String> nameList = new LinkedHashMap<>();
                nameList.put(catalogId, name);
                singleFile = new SingleFile(SnowflakeIdWorker.generateId2(), md5, nameList, 1, (int)multipartFile.getSize(), 1, new Date(), Boolean.FALSE);
                if(FileUtils.uploadSingleFile(multipartFile, uploadPath, singleFile.getId()) && catalogService.updateWithFile(singleFile.getId(), name, catalogId)) {
                    singleFileDao.save(singleFile);
                    logger.info("create a new singleFile success: " + singleFile.toString());
                    return Boolean.TRUE;
                } else {
                    logger.warning("create a new singleFile fail.");
                    return Boolean.FALSE;
                }
            } else {
                Map<String, String> nameList = singleFile.getNameList();
                nameList.put(catalogId, name);
                singleFile.setNameList(nameList);
                singleFile.setParentNumber(nameList.size());
                singleFile.setUseNumber(singleFile.getUseNumber() + 1);
                catalogService.updateWithFile(singleFile.getId(), name, catalogId);
                singleFileDao.save(singleFile);
                logger.info("create a file with updating singleFile: " + singleFile.toString());
                return Boolean.TRUE;
            }
        } catch (Exception err) {
            logger.warning("create a file error: " + err.toString());
            return Boolean.FALSE;
        }
    }

    /**
     * @description: 删除文件，同时更新父目录和文件
     * @author: Tian
     * @date: 2022/1/4 16:16
     * @param fileId
     * @param catalogId
     * @return: java.lang.Boolean
     */
    public Boolean delete(String fileId, String catalogId) throws Exception {
        try {
            // 更新父目录
            Catalog catalog =  catalogDao.findOneById(catalogId);
            List<ChildrenData> children = catalog.getChildren();
            Iterator<ChildrenData> iterator = children.iterator();
            while(iterator.hasNext()) {
                ChildrenData temp = iterator.next();
                if(temp.getId() == fileId) {
                    iterator.remove();
                    break;
                }
            }

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
