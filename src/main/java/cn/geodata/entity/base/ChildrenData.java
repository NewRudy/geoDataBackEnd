package cn.geodata.entity.base;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
/**
 * @description: 用于判断子目录是文件还是文件夹
 * @author: Tian
 * @date: 2021/12/31 10:33
 */
public class ChildrenData {
    String type;    // file or folder
    String name;
    Date date;
    String description;
    int clicks;
    String id;
}
