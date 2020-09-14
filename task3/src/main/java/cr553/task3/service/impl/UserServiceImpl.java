package cr553.task3.service.impl;

import cr553.task3.dao.UserDao;
import cr553.task3.pojo.User;
import cr553.task3.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public User queryUserByName(String username) {
        return userDao.queryUserByName(username);
    }

    @Override
    public void createUser(String username, String password) {
        userDao.createUser(username,password);
    }
}
