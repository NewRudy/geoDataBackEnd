package cn.geodata.controller;

import cn.geodata.dao.UserDao;
import cn.geodata.dto.UserDto;
import cn.geodata.entity.base.User;
import cn.geodata.utils.BASE64;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;
import cn.geodata.service.UserService;
import cn.geodata.utils.resultUtils.BaseResponse;
import cn.geodata.enums.ResultStatusEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.logging.Logger;

@Api(tags = "用户登录注册")
@RestController
@RequestMapping(value = "/user")
@Slf4j
/**
 * @description:
 * @author: Tian
 * @date: 2022/1/4 16:20
 */
public class UserController {
    @Autowired
    UserService userService;

    Logger logger = Logger.getLogger(UserController.class.getName());

    /**
     * @description: TODO BaseResponse 的 with 方法好像还有点问题
     * @author: Tian
     * @date: 2022/1/4 20:49
     * @param userDto
     * @return: utils.resultUtils.BaseResponse
     */
    @ApiOperation(value = "新建用户")
    @RequestMapping(method = RequestMethod.POST)
    public BaseResponse register(@RequestBody UserDto userDto){
        try {
            User user;
            user = userService.create(userDto);
            return new BaseResponse(ResultStatusEnum.SUCCESS, "新建用户成功", user);
        } catch (Exception err) {
            logger.warning("/users post error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "新建用户失败");
        }
    }

    @ApiOperation(value = "用户登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public BaseResponse login(@RequestBody UserDto userDto) {
        try {
            User user;
            if(userDto.getInstitution() == null) {
                user = userService.login(userDto.getName(), userDto.getPassword());
            } else {
                user = userService.login(userDto.getName(), userDto.getPassword(), userDto.getInstitution());
            }
            return new BaseResponse(ResultStatusEnum.SUCCESS, "新建用户成功", user);
        } catch (Exception err) {
            logger.warning("/users post error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "新建用户失败");
        }
    }

    @Autowired
    UserDao userDao;
    @ApiOperation(value = "信息修改")
    @RequestMapping(value = "/update")
    public void update(@RequestBody User newUser) {
        try {
            User user;

            user = userDao.findOneById(newUser.getId());
            user.setEmail(newUser.getEmail());
            user.setName(newUser.getName());
            user.setInstitution(newUser.getInstitution());

            userDao.save(user);
            System.out.println("用户更新"+newUser);
//            return new BaseResponse(ResultStatusEnum.SUCCESS, "新建用户成功", user);
        } catch (Exception err) {
            logger.warning("/users post error: " + err.toString());
//            return new BaseResponse(ResultStatusEnum.FAILURE, "新建用户失败");
        }


    }
    @ApiOperation(value = "信息修改")
    @RequestMapping(value = "/password")
    public void password (@RequestBody User user) {
        try{
            System.out.println("用户密码更新"+user);
            user.setPassword(BASE64.encryptBASE64(user.getPassword().getBytes()));
            userDao.save(user);
        }catch (Exception err){
            logger.warning("/users post error: " + err.toString());
        }
    }
    //
//
    @ApiOperation(value = "头像修改")
    @RequestMapping(value = "/picture")
    public String picture (@RequestParam("file") MultipartFile file ,@RequestParam("id") String id ) {
        try{
            if (file.isEmpty()) {
                return "图片为空";
            }
            System.out.println("用户头像更新"+id);
            User user = userDao.findOneById(id);
            user.setPicture(new Binary(file.getBytes()));
            userDao.save(user);
            return "成功";
        }catch (Exception err){
            logger.warning("/users post error: " + err.toString());
            return err.toString();
        }
    }
    @ApiOperation(value = "头像显示")
    @RequestMapping(value = "/image/{id}")
    public byte[] picture (@PathVariable String id ) {
        try{
            User user = userDao.findOneById(id);
            byte[] data = null;
            if(user != null){
                data = user.getPicture().getData();
            }
            return data;
        }catch (Exception err){
            logger.warning("/头像读取失败" + err.toString());
            throw err;
        }
    }
}
