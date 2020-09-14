package cr553.task3.config;

import cr553.task3.pojo.User;
import cr553.task3.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken userToken = (UsernamePasswordToken) authenticationToken;
        User user = userService.queryUserByName(userToken.getUsername());
        if(user==null)//用户名认证
        {
            return null;
        }
        //用户登录后隐藏登录按钮
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        session.setAttribute("LoginUserName",user.getName());
        session.setAttribute("LoginUser",user);
        //密码认证
        return new SimpleAuthenticationInfo(user,user.getPwd(),"");
    }
}
