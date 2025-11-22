package edu.univ.erp.service;

import java.time.Instant;
import java.util.Optional;
import java.util.Objects;

import edu.univ.erp.data.SettingsRepository;
import edu.univ.erp.domain.AppSetting;

/**
 * Handles maintenance mode flag stored in settings table.
 */
public class MaintenanceService {

    public static final String MAINTENANCE_KEY = "maintenanceMode";

    private final SettingsRepository settingsRepository;

    public MaintenanceService(SettingsRepository settingsRepository) {
        this.settingsRepository = Objects.requireNonNull(settingsRepository, "settingsRepository");
    }

    public boolean isMaintenanceMode() {
        Optional<AppSetting> setting = settingsRepository.findByKey(MAINTENANCE_KEY);
        return setting.map(s -> "ON".equalsIgnoreCase(s.value())).orElse(false);
    }

    public AppSetting toggle(boolean enable) {
        String value = enable ? "ON" : "OFF";
        AppSetting newSetting = new AppSetting(MAINTENANCE_KEY, value, Instant.now());
        return settingsRepository.findByKey(MAINTENANCE_KEY)
                .map(existing -> settingsRepository.update(newSetting))
                .orElseGet(() -> settingsRepository.save(newSetting));
    }
}

