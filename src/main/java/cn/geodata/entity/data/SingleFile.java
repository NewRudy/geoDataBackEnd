package cn.geodata.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * @description: 单个文件实体
 * @author: Tian
 * @date: 2021/12/31 10:35
 */
public class SingleFile {
    @Id
    String id;
    String md5;
    Map<String, String> nameList;
    int parentNumber;   // 被指向的次数，刚开始次数是 1，当次数归 0 的时候删除文件
    int size;
    int useNumber;      // 每次访问次数加一，某段时间内使用次数到达多少次，将其认为资源
    Date date;
    Boolean isResource;
}
