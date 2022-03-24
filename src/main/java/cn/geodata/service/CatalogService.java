package cn.geodata.service;

import cn.geodata.dao.CatalogDao;
import cn.geodata.dao.SingleFileDao;
import cn.geodata.dao.UserDao;
import cn.geodata.dto.PageInfoDto;
import cn.geodata.entity.base.Catalog;
import cn.geodata.entity.base.ChildrenData;
import cn.geodata.entity.base.User;
import cn.geodata.entity.data.SingleFile;
import cn.geodata.utils.CompareUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import cn.geodata.utils.SnowflakeIdWorker;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CatalogService {
    @Autowired
    CatalogDao catalogDao;

    @Autowired
    UserDao userDao;

    @Autowired
    SingleFileDao singleFileDao;

    @Lazy
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
            Catalog catalog = new Catalog(user.getCatalogId(), "-1", children, 0, user.getId(), 0, new Date());
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
            String id = SnowflakeIdWorker.generateId2();
            Catalog catalog = new Catalog(id, parentId, children, 0, user.getId(), parentCatalog.getLevel() + 1, new Date());
            catalogDao.insert(catalog);
            ChildrenData childrenData = new ChildrenData("folder", name,new Date(), "", 0, id);
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
     * @description: 删除文件夹，需要更新父目录和子目录
     * @author: Tian
     * @date: 2022/1/4 15:27
     * @param id
     * @return: java.lang.Boolean
     */
    public Boolean delete(String id) {
       try{
           Catalog catalog = catalogDao.findOneById(id);
           if(catalog == null) {
               return Boolean.FALSE;
           }
           // 更新父目录
           Catalog parentCatalog = catalogDao.findOneById(catalog.getParentId());

           Iterator<ChildrenData> iterator = parentCatalog.getChildren().iterator();
           while(iterator.hasNext()) {
               ChildrenData childrenData = iterator.next();
               if(childrenData.getId().equals(catalog.getId())) {
                   iterator.remove();
                   break;
               }
           }
           // parentCatalog.setChildren(Lists.newArrayList(iterator));
           parentCatalog.setTotal(parentCatalog.getChildren().size());
           catalogDao.save(parentCatalog);


           // 更新子目录（判断子文件和文件夹的指针数目，如果为 0 了就代表该文件没用了，需要删除）
           Iterator<ChildrenData> childrenDataIterator = catalog.getChildren().iterator();
           while (childrenDataIterator.hasNext()) {
               ChildrenData childrenData = childrenDataIterator.next();
               if(childrenData.getType().equals("Folder")) {
                   delete(childrenData.getId());
               } else {
                   singleFileService.delete(childrenData.getId(), catalog.getId());
               }
           }
           // catalogDao.deleteById(id);
           if(catalogDao.removeCatalogById(id) == 1) {
               logger.info("delete catalog: " + catalog.toString());
           } else {
               logger.info("delete catalog entity error.");
           }
           return Boolean.TRUE;
       } catch (Exception err) {
           logger.warning("delete catalog error: " + err.toString());
           return Boolean.FALSE;
       }
    }
    /**
     * @description: 修改目录的值，只能修改名字和描述，type = name || description
     * @author: Tian
     * @date: 2022/3/11 15:20
     * @param catalogId
     * @param id
     * @param content
     * @param type
     * @return: java.lang.Boolean
     */
    public Boolean updateChildrenData(String catalogId, String id, String type, String content) {
        try{
            Catalog parentCatalog = catalogDao.findOneById(catalogId);
            if(parentCatalog == null) {
                logger.warning("update catalog name with wrong parent id: " + catalogId);
                return Boolean.FALSE;
            }
            Boolean flag = Boolean.FALSE;
            Iterator<ChildrenData> iterator = parentCatalog.getChildren().iterator();
            while (iterator.hasNext()) {
                ChildrenData temp = iterator.next();
                if (temp.getId().equals(id)) {
                    if(type.equals("name")) {
                        temp.setName(content);
                    } else if(type.equals("description")) {
                        temp.setDescription(content);
                    }
                    catalogDao.save(parentCatalog);
                    flag = Boolean.TRUE;
                    break;
                }
            }
            if(flag) {
                return Boolean.TRUE;
            } else {
                logger.warning("update catalog name with wrong id: " + catalogId);
                return Boolean.FALSE;
            }
        } catch (Exception err) {
            logger.warning("update catalog name error: " + err.toString());
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
    public Boolean updateWithFile(String fileId, String fileName, String catalogId, String description){
        try {
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog != null) {
                List<ChildrenData> childrenData = catalog.getChildren();
                childrenData.add(new ChildrenData("file", fileName,new Date(), description, 0, fileId));
                catalog.setChildren(childrenData);
                catalog.setTotal(childrenData.size());
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
     * @description: 根据 id 和 pageInfo 查询 catalog
     * @author: Tian
     * @date: 2022/3/4 14:36
     * @param catalogId
     * @param pageInfoDto page信息
     * @return: cn.geodata.entity.base.Catalog
     */
    public Catalog findByMultiItem(String catalogId, PageInfoDto pageInfoDto) {
        try {
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog == null) {
                new Error(catalogId + " 没有对应的实体");
            }

            List<ChildrenData> list = catalog.getChildren();
            // 排序
            Collections.sort(list, ((o1, o2) -> {
                switch (pageInfoDto.getSortField()) {
                    case "name":
                        return CompareUtil.compareStringWithHanZi(o1.getName(), o2.getName()) >= 0 ? 1: -1;
                    case "type":
                        return CompareUtil.compareStringWithHanZi(o1.getType(), o2.getType()) >= 0 ? 1: -1;
                    case "clicks":
                        return o1.getClicks() >= o2.getClicks() ? 1: -1;
                    default:
                        return o1.getDate().compareTo(o2.getDate()) >= 0 ? 1: -1;
                }
            }));
            if(!pageInfoDto.getAsc()) {
                Collections.reverse(list);
            }
            catalog.setTotal(list.size());

            // 分页
            ChildrenData[] arr = list.toArray(new ChildrenData[list.size()]);
            catalog.setTotal(arr.length);
            int start = (pageInfoDto.getPage() - 1) * pageInfoDto.getPageSize();
            int end = (start + pageInfoDto.getPageSize()) > arr.length ? arr.length : start + pageInfoDto.getPageSize();
            if(start > arr.length - 1) {
                catalog.setChildren(null);
                catalog.setTotal(0);
                return catalog;
            }
            ChildrenData[] temp = Arrays.copyOfRange(arr, start, end);
            List<ChildrenData> res = Arrays.stream(temp).collect(Collectors.toList());

            catalog.setChildren(res);
            return  catalog;
        } catch(Exception error) {
            throw  error;
        }
    }

    /**
     * @description:
     * @author: Tian
     * @date: 2022/3/4 14:36
     * @param catalogId
     * @param pageInfoDto
     * @param searchItem 搜索条目
     * @param searchContent  搜索内容
     * @return: cn.geodata.entity.base.Catalog
     */
    public Catalog findByMultiItem(String catalogId, PageInfoDto pageInfoDto, String searchItem, String searchContent) {
        try {
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog == null) {
                new Error(catalogId + " 没有对应的实体");
            }
            List<ChildrenData> tempList = catalog.getChildren();
            catalog.setTotal(tempList.size());
            // 查找
            List<ChildrenData> list = new ArrayList<>();
            switch (searchItem) {
                case "name":
                    list = tempList.stream().filter(o -> o.getName() != "" && o.getName().contains(searchContent)).collect(Collectors.toList());
                    break;
                case "description":
                    list = tempList.stream().filter(o -> o.getName() != "" && o.getDescription().contains(searchContent)).collect(Collectors.toList());
                    break;
            }
            // 排序
            Collections.sort(list, ((o1, o2) -> {
                switch (pageInfoDto.getSortField()) {
                    case "name":
                        return CompareUtil.compareStringWithHanZi(o1.getName(), o2.getName()) >= 0 ? 1: -1;
                    case "type":
                        return CompareUtil.compareStringWithHanZi(o1.getType(), o2.getType()) >= 0 ? 1: -1;
                    case "clicks":
                        return o1.getClicks() >= o2.getClicks() ? 1: -1;
                    default:
                        return o1.getDate().compareTo(o2.getDate()) >= 0 ? 1: -1;
                }
            }));
            if(!pageInfoDto.getAsc()) {
                Collections.reverse(list);
            }
            catalog.setTotal(list.size());
            // 分页
            ChildrenData[] arr = list.toArray(new ChildrenData[list.size()]);
            int start = (pageInfoDto.getPage() - 1) * pageInfoDto.getPageSize();
            int end = (start + pageInfoDto.getPageSize()) > arr.length ? arr.length : start + pageInfoDto.getPageSize();
            if(start > arr.length - 1) {
                catalog.setChildren(null);
                catalog.setTotal(0);
                return catalog;
            }
            ChildrenData[] temp = Arrays.copyOfRange(arr, start, end);
            List<ChildrenData> res = Arrays.stream(temp).collect(Collectors.toList());

            catalog.setChildren(res);
            catalog.setTotal(res.size());
            return  catalog;
        } catch(Exception error) {
            throw  error;
        }
    }

    /**
     * @description:
     * @author: Tian
     * @date: 2022/3/18 21:28
     * @param catalogId
     * @param id
     * @return: cn.geodata.entity.base.ChildrenData
     */
    public ChildrenData findChildrenData(String catalogId, String id) {
        try {
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog == null) {
                logger.warning("find childrenData with wrong catalogId: " + catalogId);
                return null;
            }
            List<ChildrenData> list = catalog.getChildren();
            Iterator<ChildrenData> iterator = list.iterator();
            Boolean flag = Boolean.FALSE;
            ChildrenData childrenData = iterator.next();
            while(childrenData != null) {
                if(childrenData.getId().equals(id)) {
                    flag = Boolean.TRUE;
                    break;
                }
                childrenData = iterator.next();
            }
            if(flag) {
                return  childrenData;
            } else {
                return  null;
            }
        } catch (Exception err) {
            logger.warning("find childrenData wrong: " + err.toString());
            throw err;
        }
    }

    /**
     * @description: 将其它目录下的一份文件发送到一个目录下
     * @author: Tian
     * @date: 2022/3/18 20:25
     * @param catalogId
     * @param childrenData
     * @return: java.lang.Boolean
     */
    public Boolean copyFile(String catalogId, ChildrenData childrenData) {
        try{
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog == null) {
                logger.warning("copy file warning, Wrong catalogId:  " + catalogId);
                return Boolean.FALSE;
            }
            SingleFile singleFile = singleFileDao.findOneById(childrenData.getId());
            if(singleFile == null) {
                logger.warning("copy file warning, Wrong childrenData.Id: " + childrenData.getId());
                return  Boolean.FALSE;
            }

            // 更新目录和 singleFile 表
            List<ChildrenData> list = catalog.getChildren();
            list.add(childrenData);
            catalog.setChildren(list);
            Map<String, String> nameList = singleFile.getNameList();
            nameList.put(catalogId, childrenData.getName());
            singleFile.setNameList(nameList);
            singleFile.setParentNumber(nameList.size());
            singleFile.setUseNumber(singleFile.getUseNumber() + 1);

            catalogDao.save(catalog);
            singleFileDao.save(singleFile);
            logger.info(String.format("copy file %d to catalog %d", childrenData.getId(), catalogId));
            return Boolean.TRUE;
        } catch (Exception err) {
            logger.warning("copy file error: " + err.toString());
            throw err;
        }
    }

    /**
     * @description: 递归复制一份目录树
     * @author: Tian
     * @date: 2022/3/18 21:20
     * @param catalogId
     * @param childrenData
     * @return: java.lang.Boolean
     */
    public Boolean copyFolder(String catalogId, ChildrenData childrenData) {
        try {
            Boolean flag = Boolean.TRUE;

            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog == null) {
                logger.warning("copy Folder warning, Wrong catalogId:  " + catalogId);
                return Boolean.FALSE;
            }
            Catalog childCatalog = catalogDao.findOneById(childrenData.getId());
            if(childCatalog == null) {
                logger.warning("copy Folder warning, Wrong childrenData.Id: " + childrenData.getId());
                return Boolean.FALSE;
            }
            // String oldId = childrenData.getId();

            // 更改父目录
            String newId = SnowflakeIdWorker.generateId2();
            List<ChildrenData> list = catalog.getChildren();
            list.add(childrenData);
            catalog.setChildren(list);
            catalogDao.save(catalog);

            // 更改子目录
            List<ChildrenData> childrenDataList = childCatalog.getChildren();
            childCatalog.setChildren(new ArrayList<>());
            childCatalog.setId(newId);
            childCatalog.setParentId(catalogId);
            catalogDao.save(childCatalog);
            Iterator<ChildrenData> iterator = childrenDataList.iterator();
            ChildrenData i = iterator.next();
            while(i != null) {
                if(i.getType().equals("file")) {
                    flag = copyFile(newId, i) && flag;
                } else {
                    flag = copyFolder(newId, i) && flag;
                    ;
                }
            }

            return  flag;
        } catch (Exception err) {
            logger.warning("copy Folder error: " + err.toString());
            throw err;
        }
    }
}
