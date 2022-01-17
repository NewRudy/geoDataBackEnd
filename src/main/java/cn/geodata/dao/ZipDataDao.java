package cn.geodata.dao;

import cn.geodata.entity.data.ZipData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ZipDataDao extends MongoRepository<ZipData, String> {

}
