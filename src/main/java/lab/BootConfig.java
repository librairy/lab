package lab;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created on 28/06/16:
 *
 * @author cbadenes
 */
@Configuration("librairy-lab")
@ComponentScan({"org.librairy.storage", "org.librairy.eventbus"})
@PropertySource({"classpath:boot.properties"})
public class BootConfig {

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }


}
