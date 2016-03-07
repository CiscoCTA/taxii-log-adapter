package com.cisco.cta.taxii.adapter.smoketest;

import org.springframework.context.Lifecycle;

import com.cisco.cta.taxii.adapter.settings.SettingsConfiguration;

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
    }

    private void logSettingsConfig() {
        log.info("pollEndpoint={}", settingsConfig.taxiiServiceSettings().getPollEndpoint());
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
