package dao;

import entity.data.SingleFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SingleFileDao extends MongoRepository<SingleFile, String> {
    SingleFile findOneById(String id);
    SingleFile findOneByMd5(String md5);
}
