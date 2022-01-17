package cn.geodata.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * @description: 共有数据，每个用户都可以使用
 * @author: Tian
 * @date: 2021/12/31 10:35
 */
public class PublicData extends SingleFile {
    String name;
}
