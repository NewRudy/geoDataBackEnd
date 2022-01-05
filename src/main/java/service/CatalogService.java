package service;

import dao.CatalogDao;
import dao.UserDao;
import entity.base.Catalog;
import entity.base.ChildrenData;
import entity.base.User;
import entity.data.SingleFile;
import org.springframework.beans.factory.annotation.Autowired;
import utils.SnowflakeIdWorker;

import java.util.*;
import java.util.logging.Logger;

public class CatalogService {
    @Autowired
    CatalogDao catalogDao;

    @Autowired
    UserDao userDao;

    @Autowired
    SingleFileService singleFileService;

    Logger logger = Logger.getLogger(CatalogService.class.getName());

    /**
     * @description: 新建用户的时候初始化根目录
     * @author: Tian
     * @date: 2022/1/4 22:42
     * @param user
     * @return: entity.base.Catalog
     */
    public Catalog createWithUser(User user) {
        try {
            List<ChildrenData> children = new ArrayList<>();
            Catalog catalog = new Catalog(user.getCatalogID(), "-1", children, user.getId(), ".", 0, new Date());
            catalogDao.insert(catalog);
            logger.info("create root catalog: " + catalog.toString());
            return catalog;
        } catch (Exception err) {
            logger.warning("create root catalog error: " + err.toString());
            throw err;
        }
    }

    /**
     * @description: 新建文件夹: 需要创建一个新的文件夹实例，并且更新父文件夹的属性
     * @author: Tian
     * @date: 2022/1/4 22:42
     * @param userId
     * @param parentId
     * @param name
     * @return: entity.base.Catalog
     */
    public Catalog create(String userId, String parentId, String name) {
        try {
            List <ChildrenData> children = new ArrayList<>();
            User user = userDao.findOneById(userId);
            Catalog parentCatalog = catalogDao.findOneById(parentId);
            Catalog catalog = new Catalog(SnowflakeIdWorker.generateId2(), parentId, children, user.getId(), name, parentCatalog.getLevel() + 1, new Date());
            catalogDao.insert(catalog);
            ChildrenData childrenData = new ChildrenData("folder", name, SnowflakeIdWorker.generateId2());
            parentCatalog.getChildren().add(childrenData);
            catalogDao.save(parentCatalog);
            logger.info("create new catalog: " + catalog.toString());
            return catalog;
        } catch (Exception err) {
            logger.warning("create new catalog error: " + err.toString());
            throw err;
        }
    }

    /**
     * @description: 创建完文件之后更新父目录
     * @author: Tian
     * @date: 2022/1/4 15:42
     * @param fileId
     * @param fileName
     * @param catalogId
     * @return: java.lang.Boolean
     */
    public Boolean updateWithFile(String fileId, String fileName, String catalogId){
        try {
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog != null) {
                List<ChildrenData> childrenData = catalog.getChildren();
                childrenData.add(new ChildrenData("file", fileName, fileId));
                catalog.setChildren(childrenData);
                catalogDao.save(catalog);
                return Boolean.TRUE;
            } else {
                throw new Exception(String.format("catalog %s is wrong.", catalogId.toString()));
            }
        } catch (Exception err) {
            logger.warning("update with file error: " + err.toString());
            return Boolean.FALSE;
        }
    }

    /**
     * @description: 删除文件夹，需要更新父目录和子目录
     * @author: Tian
     * @date: 2022/1/4 15:27
     * @param id
     * @return: java.lang.Boolean
     */
    public Boolean delete(String id) {
       try{
           Catalog catalog = catalogDao.findOneById(id);

           // 更新父目录
           Catalog parentCatalog = catalogDao.findOneById(catalog.getParentId());
           Iterator<ChildrenData> iterator = parentCatalog.getChildren().iterator();
           while(iterator.hasNext()) {
               ChildrenData childrenData = iterator.next();
               if(childrenData.getId() == catalog.getId()) {
                   iterator.remove();
                   break;
               }
           }

           // 更新子目录（判断子文件和文件夹的指针数目，如果为 0 了就代表该文件没用了，需要删除）
           Iterator<ChildrenData> childrenDataIterator = catalog.getChildren().iterator();
           while (childrenDataIterator.hasNext()) {
               ChildrenData childrenData = childrenDataIterator.next();
               if(childrenData.getType() == "Folder") {
                   delete(childrenData.getId());
               } else {
                   singleFileService.delete(childrenData.getId(), catalog.getId());
               }
           }

           logger.info("delete catalog: " + catalog.toString());
           return Boolean.TRUE;
       } catch (Exception err) {
           logger.warning("delete catalog error: " + err.toString());
           return Boolean.FALSE;
       }
    }
}
