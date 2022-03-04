package cn.geodata.service;

import cn.geodata.dao.CatalogDao;
import cn.geodata.dao.UserDao;
import cn.geodata.dto.PageInfoDto;
import cn.geodata.entity.base.Catalog;
import cn.geodata.entity.base.ChildrenData;
import cn.geodata.entity.base.User;
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
            Catalog catalog = new Catalog(user.getCatalogId(), "-1", children, user.getId(), ".", 0, new Date());
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
            Catalog catalog = new Catalog(id, parentId, children, user.getId(), name, parentCatalog.getLevel() + 1, new Date());
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
     * @param pageInfoDto
     * @return: cn.geodata.entity.base.Catalog
     */
    public Catalog findByMultiItem(String catalogId, PageInfoDto pageInfoDto) {
        try {
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog == null) {
                new Error(catalogId + " 没有对应的实体");
            }
            ChildrenData[] arr = catalog.getChildren().toArray(new ChildrenData[catalog.getChildren().size()]);
            // 分页
            int start = (pageInfoDto.getPage() - 1) * pageInfoDto.getPageSize();
            int end = (start + pageInfoDto.getPageSize()) > arr.length ? arr.length : start + pageInfoDto.getPageSize();
            if(start > arr.length - 1) {
                return catalog;
            }
            ChildrenData[] temp = Arrays.copyOfRange(arr, start, end);
            List<ChildrenData> res = Arrays.stream(temp).collect(Collectors.toList());
            // 排序
            Collections.sort(res, ((o1, o2) -> {
                switch (pageInfoDto.getSortField()) {
                    case "name":
                        return o1.getName().compareTo(o2.getName()) >= 0 ? 1: -1;
                    case "type":
                        return o1.getType().compareTo(o2.getType()) >= 0 ? 1: -1;
                    case "clicks":
                        return o1.getClicks() >= o2.getClicks() ? 1: -1;
                    default:
                        return o1.getDate().compareTo(o2.getDate()) >= 0 ? 1: -1;
                }
            }));
            if(pageInfoDto.getAsc()) {
                Collections.reverse(res);
            }
            catalog.setChildren(res);
            return  catalog;
        } catch(Exception error) {
            throw  error;
        }
    }

    public Catalog findByMultiItem(String catalogId, PageInfoDto pageInfoDto, String searchItem, String searchContent) {
        try {
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog == null) {
                new Error(catalogId + " 没有对应的实体");
            }
            List<ChildrenData> tempList = catalog.getChildren();
            // 搜索
            List<ChildrenData> list = new ArrayList<>();
            switch (searchItem) {
                case "name":
                    list = tempList.stream().filter(o -> o.getName() != "" && o.getName().contains(searchContent)).collect(Collectors.toList());
                    break;
                case "description":
                    list = tempList.stream().filter(o -> o.getName() != "" && o.getDescription().contains(searchContent)).collect(Collectors.toList());
                    break;
            }
            ChildrenData[] arr = list.toArray(new ChildrenData[list.size()]);
            // 分页
            int start = (pageInfoDto.getPage() - 1) * pageInfoDto.getPageSize();
            int end = (start + pageInfoDto.getPageSize()) > arr.length ? arr.length : start + pageInfoDto.getPageSize();
            if(start > arr.length - 1) {
                return catalog;
            }
            ChildrenData[] temp = Arrays.copyOfRange(arr, start, end);
            List<ChildrenData> res = Arrays.stream(temp).collect(Collectors.toList());
            Collections.sort(res, ((o1, o2) -> {
                switch (pageInfoDto.getSortField()) {
                    case "name":
                        return o1.getName().compareTo(o2.getName()) >= 0 ? 1: -1;
                    case "type":
                        return o1.getType().compareTo(o2.getType()) >= 0 ? 1: -1;
                    case "date":
                        return o1.getDate().compareTo(o2.getDate()) >= 0 ? 1: -1;
                    default:        // 默认采用时间排序
                        return o1.getClicks() >= o2.getClicks() ? 1: -1;
                }
            }));
            if(pageInfoDto.getAsc()) {
                Collections.reverse(res);
            }
            catalog.setChildren(res);
            return  catalog;
        } catch(Exception error) {
            throw  error;
        }
    }
}
