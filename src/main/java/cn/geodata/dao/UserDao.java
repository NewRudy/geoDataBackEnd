package cn.geodata.dao;

import cn.geodata.entity.base.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserDao extends MongoRepository<User, String> {
    User findOneById(String id);
    User findUserByName(String Name);
    User findUserByNameAndInstitution(String name, String institution);
}
