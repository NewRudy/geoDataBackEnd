package cn.geodata.entity.data;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Data
/**
 * @description: 文件组成的压缩包，可能是下载或是上传的，
 * @author: Tian
 * @date: 2021/12/31 10:37
 */
public class ZipData {
    @Id
    String id;
    List<String> dataId;
    int useNumber;      // 某段时间都组成了这个压缩包，将其作为资源
    Date date;
    Boolean isResource;
}
