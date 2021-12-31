package dao;

import entity.data.ZipData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ZipDataDao extends MongoRepository<ZipData, String> {

}
