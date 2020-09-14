package cr553.task3;

import cr553.task3.pojo.User;
import cr553.task3.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Task3ApplicationTests {


    @Autowired
    UserService userService;

    @Test
    void contextLoads() {
        User root = userService.queryUserByName("root");
        System.out.println(root);
    }


    @Test
    void Test2()
    {
        userService.createUser("new1","abc");
    }
}
