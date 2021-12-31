package dao;

import entity.data.PublicData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PublicDataDao extends MongoRepository<PublicData, String> {

}
