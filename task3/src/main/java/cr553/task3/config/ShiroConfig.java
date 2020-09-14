package cr553.task3.config;

import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(@Qualifier("securityManager") DefaultWebSecurityManager securityManager )
    {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager);
        Map<String, String> filters = new LinkedHashMap();
        //user下面的操作只有登录才能访问
        filters.put("/user/*","authc");
        filters.put("/toReceiveMail","authc");
        filters.put("/toSendMail","authc");
        bean.setFilterChainDefinitionMap(filters);
        //设置登录路径
        bean.setLoginUrl("/toLogin");
        //设置未授权路径
        bean.setUnauthorizedUrl("/noauthc");
        return bean;
    }


    @Bean("securityManager")
    public DefaultWebSecurityManager getDefaultWebSecurityManager(@Autowired UserRealm userRealm)
    {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userRealm);
        return securityManager;
    }

    @Bean
    public UserRealm userRealm()
    {
        return new UserRealm();
    }
}
