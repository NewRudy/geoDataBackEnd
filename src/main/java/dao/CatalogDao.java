package dao;

import entity.base.Catalog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CatalogDao extends MongoRepository<Catalog, String> {

}
