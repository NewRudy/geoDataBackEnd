package cn.geodata.dao;

import cn.geodata.entity.data.SingleFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SingleFileDao extends MongoRepository<SingleFile, String> {
    SingleFile findOneById(String id);
    SingleFile findOneByMd5(String md5);
}
