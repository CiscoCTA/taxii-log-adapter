package com.cisco.cta.taxii.adapter.smoketest;

import org.springframework.context.Lifecycle;

import com.cisco.cta.taxii.adapter.AdapterRunner;
import com.cisco.cta.taxii.adapter.settings.SettingsConfiguration;
import com.google.common.base.Strings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class SmokeTestLifecycle implements Lifecycle {

    private final SettingsConfiguration settingsConfig;

    @Override
    public void start() {
        // TODO do the smoke testing here
        logSettingsConfig();
        AdapterRunner.exit();
    }

    void logSettingsConfig() {
        log.info("Listing configuration parameters ...");
        log.info("=== taxiiService ===");
        log.info("pollEndpoint={}", settingsConfig.taxiiServiceSettings().getPollEndpoint());
        log.info("username={}", settingsConfig.taxiiServiceSettings().getUsername());
        log.info("password={}", Strings.isNullOrEmpty(settingsConfig.taxiiServiceSettings().getPassword()) ? "" : "*****");
        log.info("feeds");
        for (String feed : settingsConfig.taxiiServiceSettings().getFeeds()) {
            log.info("  {}", feed);
        }
        log.info("=== schedule ===");
        log.info("cron={}", settingsConfig.scheduleSettings().getCron());
        log.info("=== transform ===");
        log.info("stylesheet={}", settingsConfig.transformSettings().getStylesheet());
        if (settingsConfig.proxySettings() != null) {
            log.info("=== proxy ===");
            log.info("url={}", settingsConfig.proxySettings().getUrl());
            log.info("authenticationType={}", settingsConfig.proxySettings().getAuthenticationType());
            log.info("username={}", settingsConfig.proxySettings().getUsername());
            log.info("password={}", Strings.isNullOrEmpty(settingsConfig.proxySettings().getPassword()) ? "" : "*****");
        }
    }

    @Override
    public void stop() {
        // do nothing
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
