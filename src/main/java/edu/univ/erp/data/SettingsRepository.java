package edu.univ.erp.data;

import java.util.Optional;

import edu.univ.erp.domain.AppSetting;

/**
 * Data access abstraction for application settings.
 */
public interface SettingsRepository {

    Optional<AppSetting> findByKey(String key);

    AppSetting save(AppSetting setting);

    AppSetting update(AppSetting setting);
}

