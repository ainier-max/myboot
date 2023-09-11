package cbc.boot.myboot.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@PropertySource(value = "classpath:component.properties")
@ConfigurationProperties(prefix = "gis")
public class ReadPropertiesConfig {

    private Map<String, String> component = new HashMap<>();

    public Map<String, String> getComponent() {
        return component;
    }

    public void setComponent(Map<String, String> component) {
        this.component = component;
    }
}