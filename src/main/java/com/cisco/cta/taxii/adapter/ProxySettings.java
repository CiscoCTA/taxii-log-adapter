package com.cisco.cta.taxii.adapter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import java.net.URL;

@ConfigurationProperties(prefix="proxy")
@Data
public class ProxySettings {

    private URL url;

    private ProxyAuthenticationType authenticationType;

    private String domain;

    private String username;

    private String password;

    private String workstation;

}
