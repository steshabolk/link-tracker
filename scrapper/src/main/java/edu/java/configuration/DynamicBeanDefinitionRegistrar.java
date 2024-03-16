package edu.java.configuration;

import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

public class DynamicBeanDefinitionRegistrar implements BeanDefinitionRegistryPostProcessor {

    private final List<String> beanNames;

    public DynamicBeanDefinitionRegistrar(Environment environment, String propertyPrefix) {
        beanNames =
            Binder.get(environment)
                .bind(propertyPrefix, Bindable.listOf(String.class))
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        beanNames.forEach(
            name -> {
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClassName(name);
                registry.registerBeanDefinition(
                    StringUtils.uncapitalize(name.substring(name.lastIndexOf('.') + 1)),
                    beanDefinition
                );
            });
    }
}
