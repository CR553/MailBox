package cr553.task3.dao;

import cr553.task3.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {
    User queryUserByName(String username);
    void createUser(String username,String password);
}
