package com.dotm.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.github.url")
@Data
public class GithubCallBackProperties {

    private String authCallback;

    private String accessCallback;
}
