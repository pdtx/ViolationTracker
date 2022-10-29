package cn.edu.fudan.issueservice.component;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-20 16:10
 **/
public class ApplicationContextGetBeanHelper implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static Object getBean(String className) throws BeansException, IllegalArgumentException {
        if (className == null || className.length() <= 0) {
            throw new IllegalArgumentException("className为空");
        }

        String beanName = null;
        if (className.length() > 1) {
            beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
        } else {
            beanName = className.toLowerCase();
        }
        return applicationContext != null ? applicationContext.getBean(beanName) : null;
    }

    public static void addBean(Class<?> beanClass, String beanName) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
        // 注册到spring容器中
        beanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextGetBeanHelper.applicationContext = applicationContext;
    }
}