package cn.geodata.service;

import cn.geodata.dao.UserDao;
import cn.geodata.dto.UserDto;
import cn.geodata.entity.base.User;
import cn.geodata.utils.JudgeUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import cn.geodata.utils.BASE64;
import cn.geodata.utils.SnowflakeIdWorker;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.logging.Logger;

@Slf4j
@Service
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

    public void nullJudge(String name, String password) throws Exception {
        JudgeUtils.isNullString("name", name);
        JudgeUtils.isNullString("password", password);
    }

    public User create(UserDto userDto) throws  Exception {
        try {
            nullJudge(userDto.getName(), userDto.getPassword());
            if(userDto.getEmail() == null) {
                userDto.setEmail("");
            }
            if(userDto.getInstitution() == null) {
                userDto.setInstitution("");
            }
            User user = new User(SnowflakeIdWorker.generateId2(), SnowflakeIdWorker.generateId2(), userDto.getName(), BASE64.encryptBASE64(userDto.getPassword().getBytes()), userDto.getEmail(), userDto.getInstitution(), new Date(),new Binary(new byte[2]));
            userDao.insert(user);
            catalogService.createWithUser(user);
            logger.info("create user: " + user.toString());
            return user;
        }  catch (Exception err) {
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
            nullJudge(name, password);
            if(userDao.findUserByName(name) != null) {
                new Error("该用户名已经注册");
            }
            User user = new User(SnowflakeIdWorker.generateId2(), SnowflakeIdWorker.generateId2(), name, BASE64.encryptBASE64(password.getBytes()), "", "", new Date(),new Binary(new byte[2]));
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
     * @date: 2022/1/4 15:24
     * @param name
     * @param password
     * @param institution
     * @return: java.lang.Boolean
     */
    public User create(String name, String password,String email, String institution) throws Exception{
        try{
            nullJudge(name, password);
            if(userDao.findUserByNameAndInstitution(name, institution) != null) {
                new Error("该用户名及机构已经注册");
            }
            User user = new User(SnowflakeIdWorker.generateId2(), SnowflakeIdWorker.generateId2(), name, BASE64.encryptBASE64(password.getBytes()), email, institution, new Date(),new Binary(new byte[2]));
            userDao.insert(user);
            catalogService.createWithUser(user);
            logger.info("create user: " + user.toString());
            return user;
        } catch (Exception err) {
            logger.warning("create user error: " + err.toString());
            throw err;
        }
    }

    public User login(String name, String password) throws Exception{
        try {
            nullJudge(name, password);
            User user = userDao.findUserByName(name);
            if(user==null) {
                throw new Error("user is null.");
            }
            String temp = new String(BASE64.decryptBASE64(user.getPassword()));
            if(temp.equals(password)) {
                return user;
            } else {
                throw new Error("password is not right");
            }
        } catch (Exception err) {
            throw err;
        }
    }

    public User login(String name, String password, String institution) throws Exception {
        try {
            nullJudge(name, password);
            User user = userDao.findUserByNameAndInstitution(name, institution);
            if(user==null) {
                throw new Error("user is null.");
            }
            if(BASE64.encryptBASE64(password.getBytes()) == user.getPassword()) {
                return user;
            } else {
                throw new Error("password is not right");
            }
        } catch (Exception err) {
            throw err;
        }
    }

    public User change(String name, String password, String institution) throws Exception {
        try {
            nullJudge(name, password);
            User user = userDao.findUserByNameAndInstitution(name, institution);
            if(user==null) {
                throw new Error("user is null.");
            }
            if(BASE64.encryptBASE64(password.getBytes()) == user.getPassword()) {
                return user;
            } else {
                throw new Error("password is not right");
            }
        } catch (Exception err) {
            throw err;
        }
    }
}
