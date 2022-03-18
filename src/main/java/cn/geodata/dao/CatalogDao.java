package cn.geodata.dao;

import cn.geodata.entity.base.Catalog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CatalogDao extends MongoRepository<Catalog, String> {
    Catalog findOneById(String id);
    Long removeCatalogById(String id);
}
