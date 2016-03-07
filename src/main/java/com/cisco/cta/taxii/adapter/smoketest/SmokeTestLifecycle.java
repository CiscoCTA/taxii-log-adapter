package com.cisco.cta.taxii.adapter.smoketest;

import org.springframework.context.Lifecycle;

import com.cisco.cta.taxii.adapter.settings.SettingsConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SmokeTestLifecycle implements Lifecycle {

    private final SettingsConfiguration settingsConfig;

    @Override
    public void start() {
        // TODO do the smoke testing here   
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
