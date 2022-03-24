package cn.geodata.entity.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@AllArgsConstructor
/**
 * @description: 用户表
 * @author: Tian
 * @date: 2021/12/31 10:33
 */
public class User {
    @Id
    String id;
    String catalogId;
    String name;
    String password;
    String email;
    String institution;     // 用户可能了来自不同的机构
    Date date;
    Binary picture; //头像二进制文件
}
