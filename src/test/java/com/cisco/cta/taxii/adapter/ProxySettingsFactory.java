package com.cisco.cta.taxii.adapter;

import java.net.MalformedURLException;
import java.net.URL;

public class ProxySettingsFactory {

    public static ProxySettings createDefaults() {
        try {
            ProxySettings proxySettings = new ProxySettings();
            proxySettings.setUrl(new URL("http://localhost:8001/"));
            return proxySettings;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
