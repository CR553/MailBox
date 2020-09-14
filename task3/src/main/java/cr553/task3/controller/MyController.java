package cr553.task3.controller;

import cr553.task3.pojo.User;
import cr553.task3.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.regex.Pattern;

@Controller
public class MyController {

    @Autowired
    private UserService userService;

    //首页
    @RequestMapping({"/","/index"})
    public String toIndex()
    {
        return "index";
    }

    @RequestMapping("/toLogin")
    public String toLogin()
    {
        return "login";
    }

    //登录
    @RequestMapping("/login")
    public String login(String username, String password, Model model)
    {
        //获取当前对象
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username,password);
        try{
            subject.login(token);//执行登录方法
            return "index";
        }
        catch (UnknownAccountException e){
            model.addAttribute("msg","你输入的用户名或密码不正确，请重新输入。");
            return "login";
        } catch (IncorrectCredentialsException e){
            model.addAttribute("msg","你输入的用户名或密码不正确，请重新输入。");
            return "login";
        }
    }

    //登出功能
    @RequestMapping("/logOut")
    public String logOut()
    {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "index";

    }

    @RequestMapping("/toRegister")
    public String register() {
        return "register";
    }

    @RequestMapping("/RegisterSuccess")
    public String toRegisterSuccess() {
        return "RegisterSuccess";
    }

    //注册
    @RequestMapping("/register")
    public String register(String username, String password,String confirmPass, Model model)
    {
        //用户名格式检验
        String regex="^\\w+@\\w+.\\w+\\.?\\w+$";
        Pattern pattern=Pattern.compile(regex);
        if(!pattern.matcher(username).matches())
        {
            model.addAttribute("msg","用户名应该符合邮箱格式！");
            return "register";
        }
        User user = userService.queryUserByName(username);
        if(user!=null)
        {
            model.addAttribute("msg","用户名"+user.getName()+"已存在,请更换！");
            return "register";
        }
        if(password.length()<3)
        {
            model.addAttribute("msg","密码长度必须大于3位数");
            return "register";
        }
        if(password.equals(confirmPass))
        {
            model.addAttribute("msg","两次密码输入不一致，请仔细核对！");
            return "register";
        }
        userService.createUser(username,password);
        return "RegisterSuccess";
    }

    @RequestMapping("/noauthc")
    @ResponseBody
    public String toNoAuthc()
    {
        return "未授权页面";
    }


    //邮件发送成功页面
    @RequestMapping("/toSendSuccess")
    public String toSendSuccess()
    {
        return "/user/SendSuccess";
    }


}
