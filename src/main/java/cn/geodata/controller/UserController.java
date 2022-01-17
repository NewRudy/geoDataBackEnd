package cn.geodata.controller;

import cn.geodata.dto.UserDto;
import cn.geodata.entity.base.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import cn.geodata.service.UserService;
import cn.geodata.utils.resultUtils.BaseResponse;
import cn.geodata.utils.resultUtils.DefaultStatus;

import java.util.logging.Logger;

@Api(tags = "用户登录注册")
@RestController
@RequestMapping(value = "/users")
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
    public BaseResponse register(@RequestBody UserDto userDto) throws Exception {
        try {
            User user;
            if(userDto.getInstitution() == null) {
                user = userService.create(userDto.getName(), userDto.getPassword());
            } else {
                user = userService.create(userDto.getName(), userDto.getPassword(), userDto.getInstitution());
            }
            return new BaseResponse(DefaultStatus.SUCCESS, "新建用户成功", user);
            // BaseResponse<String> response = new BaseResponse<>();
            // response.setCode(DefaultStatus.SUCCESS);
            // response.setMessage("新建用户成功");
            // response.setData(user.toString());
            // return response;
        } catch (Exception err) {
            logger.warning("/users post error: " + err.toString());
            return new BaseResponse(DefaultStatus.FAILURE, "新建用户失败");
            // BaseResponse<String> response = new BaseResponse<>();
            // response.setCode(DefaultStatus.FAILURE);
            // response.setMessage("新建用户失败");
            // return response;
        }
    }
}
