package cr553.task3.controller;

import cr553.task3.pojo.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.regex.Pattern;

@Controller
public class SendMailController {

    @RequestMapping("/toSendMail")
    public String toSendMail()
    {
        return "user/sendMail";
    }
    //发送邮件方法
    public void send(String receiverName, String titleName, String infoName, User LoginUser)
    {
        try {
            String host = "smtp.qq.com";//这是QQ邮箱的smtp服务器地址
            String port = "25"; //端口号
            /*
             *Properties是一个属性对象，用来创建Session对象
             */
            Properties props = new Properties();
            props.setProperty("mail.smtp.host", host);
            props.setProperty("mail.smtp.port", port);
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.ssl.enable", "false");//"true"
            props.setProperty("mail.smtp.connectiontimeout", "5000");
            /*
            //测试数据
            final String user = "960162212@qq.com";
            final String pwd = "khcxygjavjvrbdia";
            */
            final String user =LoginUser.getName();//邮箱账号
            final String pwd = LoginUser.getPwd();//smtp/pop3授权密码
            /*
             *Session类定义了一个基本的邮件对话。
             */
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    //登录用户名密码
                    return new PasswordAuthentication(user, pwd);
                }
            });
            session.setDebug(true);
            /*
             *Transport类用来发送邮件。
             *传入参数smtp，transport将自动按照smtp协议发送邮件。
             */
            Transport transport = session.getTransport("smtp");//"smtps"
            transport.connect(host, user, pwd);
            /*
             *Message对象用来储存实际发送的电子邮件信息
             */
            MimeMessage message = new MimeMessage(session);
            message.setSubject(titleName);

            //消息发送者接收者设置(发件地址，昵称)，收件人看到的昵称是这里设定的
            message.setFrom(new InternetAddress(user, user));
            message.addRecipients(Message.RecipientType.TO, new InternetAddress[]{
                    //消息接收者(收件地址，昵称)
                    // 不过这个昵称貌似没有看到效果
                    new InternetAddress(receiverName, "Test"),});
            message.saveChanges();
            //设置邮件内容及编码格式
            // 后一个参数可以不指定编码，如"text/plain"，但是将不能显示中文字符
            message.setContent(infoName, "text/plain;charset=UTF-8");
            //发送
            Transport.send(message);
            transport.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //发送邮件
    @RequestMapping("/user/sendMail")
    public String sendMail(String receiverName, String titleName, String infoName, Model model)
    {
        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
        org.apache.shiro.session.Session ShiroSession = subject.getSession();
        User LoginUser = (User) ShiroSession.getAttribute("LoginUser");

        //验证表单
        if(titleName==null||infoName==null||receiverName==null||infoName.equals("")||titleName.equals("")||receiverName.equals(""))
        {
            model.addAttribute("msg","内容不能为空！");
            return "/user/sendMail";
        }
        //用户名格式检验
        String regex="^\\w+@\\w+.\\w+\\.?\\w+$";
        Pattern pattern=Pattern.compile(regex);
        if(!pattern.matcher(receiverName).matches())
        {
            model.addAttribute("msg","收件人名称应该符合邮箱格式！");
            return "/user/sendMail";
        }
        try {
            //发送邮件
            send(receiverName,titleName,infoName,LoginUser);
        }catch (Exception e)
        {
            model.addAttribute("msg","发送邮件失败");
            e.printStackTrace();
            return "/user/sendMail";
        }
        return "/user/SendSuccess";
    }





}
