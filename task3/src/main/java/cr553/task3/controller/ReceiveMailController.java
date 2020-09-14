package cr553.task3.controller;

import com.sun.mail.util.MailSSLSocketFactory;
import cr553.task3.pojo.ReceiveMessage;
import cr553.task3.pojo.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Controller
public class ReceiveMailController {

    //接收邮件
    public static Message[] getAllMails(User LoginUser) {
        String protocol = "pop3";//使用pop3协议
        boolean isSSL = true;//使用SSL加密
        String host = "pop.qq.com";//QQ邮箱的pop3服务器
        int port = 995;//端口
        final String user = LoginUser.getName();//邮箱账户
        final String pwd = LoginUser.getPwd();//smtp/pop3授权密码
        /*
         *Properties是一个属性对象，用来创建Session对象
         */
        try{
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            Properties props = new Properties();
            props.put("mail.pop3.auth.plain.disable",true);
            props.put("mail.pop3.ssl.enable",true);
            props.put("mail.pop3.auth", "true"); //这样才能通过验证
            props.put("mail.pop3.ssl.socketFactory", sf);
            props.put("mail.pop3.host", host);
            props.put("mail.pop3.port", port);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    //登录用户名密码
                    return new PasswordAuthentication(user, pwd);
                }
            });
            /*
             * Store类实现特定邮件协议上的读、写、监视、查找等操作。
             * 通过Store类可以访问Folder类。
             * Folder类用于分级组织邮件，并提供照Message格式访问email的能力。
             */
            Store store = null;
            Folder folder = null;
            try {
                store = session.getStore(protocol);
                store.connect(user, pwd);

                folder = store.getFolder("INBOX");// 获得用户的邮件帐户
                folder.open(Folder.READ_WRITE); // 设置对邮件帐户的访问权限
                Message[] messages = folder.getMessages();
                return messages;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("接收完毕！");
        return null;
    }

    //接收页面
    @RequestMapping("/toReceiveMail")
    public String toRevice()
    {
        return "user/receiveMail";
    }

    @RequestMapping("/user/receiveMail")
    public String receiveMail(Model model) throws Exception {
        System.out.println("receiveMail开始了");
        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
        org.apache.shiro.session.Session ShiroSession = subject.getSession();
        User LoginUser = (User) ShiroSession.getAttribute("LoginUser");
        Message[] allMails = getAllMails(LoginUser);
        ReceiveMessage[] messages=new ReceiveMessage[allMails.length];

        for(int i=0;i<allMails.length;i++)
        {
            MimeMessage msg= (MimeMessage) allMails[i];
            String Msubject=getSubject(msg);
            String FromUser=getFrom(msg);
            String receiveAddress = getReceiveAddress(msg,null);
            String sentDate = getSentDate(msg,null);
            boolean seen = isSeen(msg);

            /*System.out.println("------------------正在解析第"+i+"封邮件！------------------");
            System.out.println("主题："+Msubject);
            System.out.println("发件人"+FromUser);
            System.out.println("收件地址"+receiveAddress);
            System.out.println("发送日期"+sentDate);
            System.out.println("是否已读"+seen);*/

            StringBuffer content = new StringBuffer(30);
            getMailTextContent(msg, content);
           /* System.out.println("邮件正文：" + (content.length() > 100 ? content.substring(0,100) + "..." : content));
            System.out.println("------------------第" + msg.getMessageNumber() + "封邮件解析结束-------------------- ");
            System.out.println();*/

            ReceiveMessage rcMsg = new ReceiveMessage();
            rcMsg.setContent((content.length() > 100 ? content.substring(0,100) + "..." : content).toString());
            rcMsg.setFromUser(FromUser);
            rcMsg.setSeen(seen);
            rcMsg.setReceviceAddress(receiveAddress);
            rcMsg.setSendDate(sentDate);
            rcMsg.setSubject(Msubject);
            messages[i]=rcMsg;
        }
        model.addAttribute("mailList",messages);
        return "/user/receiveMail";
    }

    /**
     * 获得邮件主题
     * @param msg 邮件内容
     * @return 解码后的邮件主题
     */
    public static String getSubject(MimeMessage msg) throws UnsupportedEncodingException, MessagingException {
        return MimeUtility.decodeText(msg.getSubject());
    }

    /**
     * 获得邮件发件人
     * @param msg 邮件内容
     * @return 姓名 <Email地址>
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static String getFrom(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        String from = "";
        Address[] froms = msg.getFrom();
        if (froms.length < 1)
            throw new MessagingException("没有发件人!");

        InternetAddress address = (InternetAddress) froms[0];
        String person = address.getPersonal();
        if (person != null) {
            person = MimeUtility.decodeText(person) + " ";
        } else {
            person = "";
        }
        from = person + "<" + address.getAddress() + ">";

        return from;
    }

    /**
     * 根据收件人类型，获取邮件收件人、抄送和密送地址。如果收件人类型为空，则获得所有的收件人
     * <p>Message.RecipientType.TO  收件人</p>
     * <p>Message.RecipientType.CC  抄送</p>
     * <p>Message.RecipientType.BCC 密送</p>
     * @param msg 邮件内容
     * @param type 收件人类型
     * @return 收件人1 <邮件地址1>, 收件人2 <邮件地址2>, ...
     * @throws MessagingException
     */
    public static String getReceiveAddress(MimeMessage msg, Message.RecipientType type) throws MessagingException {
        StringBuffer receiveAddress = new StringBuffer();
        Address[] addresss = null;
        if (type == null) {
            addresss = msg.getAllRecipients();
        } else {
            addresss = msg.getRecipients(type);
        }

        if (addresss == null || addresss.length < 1)
            throw new MessagingException("没有收件人!");
        for (Address address : addresss) {
            InternetAddress internetAddress = (InternetAddress)address;
            receiveAddress.append(internetAddress.toUnicodeString()).append(",");
        }

        receiveAddress.deleteCharAt(receiveAddress.length()-1);	//删除最后一个逗号

        return receiveAddress.toString();
    }

    /**
     * 获得邮件发送时间
     * @param msg 邮件内容
     * @return yyyy年mm月dd日 星期X HH:mm
     * @throws MessagingException
     */
    public static String getSentDate(MimeMessage msg, String pattern) throws MessagingException {
        Date receivedDate = msg.getSentDate();
        if (receivedDate == null)
            return "";

        if (pattern == null || "".equals(pattern))
            pattern = "yyyy年MM月dd日 E HH:mm ";

        return new SimpleDateFormat(pattern).format(receivedDate);
    }

    /**
     * 判断邮件是否已读
     * @param msg 邮件内容
     * @return 如果邮件已读返回true,否则返回false
     * @throws MessagingException
     */
    public static boolean isSeen(MimeMessage msg) throws MessagingException {
        return msg.getFlags().contains(Flags.Flag.SEEN);
    }


    /**
     * 获得邮件文本内容
     * @param part 邮件体
     * @param content 存储邮件文本内容的字符串
     * @throws MessagingException
     * @throws IOException
     */
    public static void getMailTextContent(Part part, StringBuffer content) throws MessagingException, IOException {
        //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part)part.getContent(),content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart,content);
            }
        }
    }



    /**
     * 读取输入流中的数据保存至指定目录
     * @param is 输入流
     * @param fileName 文件名
     * @param destDir 文件存储目录
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void saveFile(InputStream is, String destDir, String fileName)
            throws FileNotFoundException, IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(new File(destDir + fileName)));
        int len = -1;
        while ((len = bis.read()) != -1) {
            bos.write(len);
            bos.flush();
        }
        bos.close();
        bis.close();
    }

    /**
     * 文本解码
     * @param encodeText 解码MimeUtility.encodeText(String text)方法编码后的文本
     * @return 解码后的文本
     * @throws UnsupportedEncodingException
     */
    public static String decodeText(String encodeText) throws UnsupportedEncodingException {
        if (encodeText == null || "".equals(encodeText)) {
            return "";
        } else {
            return MimeUtility.decodeText(encodeText);
        }
    }


}
