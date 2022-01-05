package service;

import dao.UserDao;
import entity.base.User;
import org.springframework.beans.factory.annotation.Autowired;
import utils.BASE64;
import utils.SnowflakeIdWorker;

import java.util.Date;
import java.util.logging.Logger;

/**
 * @description:
 * @author: Tian
 * @date: 2021/12/31 11:26
 */
public class UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    CatalogService catalogService;

    Logger logger = Logger.getLogger(UserService.class.getName());

    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/4 15:24
     * @param name
     * @param password
     * @param institution
     * @return: java.lang.Boolean
     */
    public User create(String name, String password, String institution) throws Exception{
        try{
            User user = new User(SnowflakeIdWorker.generateId2(), SnowflakeIdWorker.generateId2(), name, BASE64.encryptBASE64(password.getBytes()), institution, new Date());
            userDao.insert(user);
            catalogService.createWithUser(user);
            logger.info("create user: " + user.toString());
            return user;
        } catch (Exception err) {
            logger.warning("create user error: " + err.toString());
            throw err;
        }
    }

    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/4 15:25
     * @param name
     * @param password
     * @return: java.lang.Boolean
     */
    public User create(String name, String password) throws Exception{
        try{
            User user = new User(SnowflakeIdWorker.generateId2(), SnowflakeIdWorker.generateId2(), name, BASE64.encryptBASE64(password.getBytes()), "", new Date());
            userDao.insert(user);
            catalogService.createWithUser(user);
            logger.info("create user: " + user.toString());
            return user;
        } catch (Exception err) {
            logger.warning("create user error: " + err.toString());
            throw err;
        }
    }
}
