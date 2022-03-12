package cn.geodata.entity.base;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * @description: 构建整个目录。
 * parent 和 children 的 List类似与快捷方式，能表示复杂方式了；map 是因为目录只需要名字和其指向的id；父目录只能是文件夹，子目录可能是文件夹或者文件，所以使用了 dataType
 * @author: Tian
 * @date: 2021/12/31 10:32
 */
public class Catalog {
    @Id
    String id;
    String parentId;
    List<ChildrenData> children;    // 子文件和子文件夹
    int total;      // 文件数
    String userId;
    int level;
    Date date;
}
