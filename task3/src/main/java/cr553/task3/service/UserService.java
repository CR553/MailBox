package cr553.task3.service;

import cr553.task3.pojo.User;

public interface UserService {
    User queryUserByName(String username);

    void createUser(String username,String password);
}
