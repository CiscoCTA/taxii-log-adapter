package com.cisco.cta.taxii.adapter.settings;

import org.springframework.mock.env.MockPropertySource;


class PropertySourceHelper {

    public static MockPropertySource validProperties() {
        return new MockPropertySource()
            .withProperty("taxiiService.pollEndpoint", "http://taxii")
            .withProperty("taxiiService.username", "smith")
            .withProperty("taxiiService.password", "secret")
            .withProperty("taxiiService.feeds[0]", "alpha-feed")
            .withProperty("taxiiService.statusFile", "taxii-status.xml")
            .withProperty("schedule.cron", "* * * * * *")
            .withProperty("transform.stylesheet", "transform.xsl")
            .withProperty("proxy.url", "http://localhost:8001/")
            .withProperty("proxy.authenticationType", "NONE");
    }

    public static MockPropertySource exclude(MockPropertySource all, String excludePrefix) {
        MockPropertySource source = new MockPropertySource();
        for (String key : all.getPropertyNames()) {
            if (! key.startsWith(excludePrefix)) {
                source.setProperty(key, all.getProperty(key));
            }
        }
        return source;
    }

}
