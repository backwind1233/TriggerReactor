package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.IMigratable;
import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;

import java.io.File;
import java.util.function.BiFunction;

public class PluginConfigManager extends Manager implements IMigratable {
    private final IConfigSource configSource;

    public PluginConfigManager(TriggerReactorCore plugin) {
        this(plugin, ConfigSourceFactory::gson);
    }

    public PluginConfigManager(TriggerReactorCore plugin, BiFunction<File, String, IConfigSource> fn) {
        super(plugin);
        this.configSource = fn.apply(plugin.getDataFolder(), "config.json");
    }

    @Override
    public void reload() {
        configSource.reload();
    }

    @Override
    public void saveAll() {
        configSource.saveAll();
    }

    @Override
    public void disable() {
        configSource.disable();
    }

    @Override
    public boolean isMigrationNeeded() {
        File oldFile = new File(plugin.getDataFolder(), "config.yml");
        // after migration, file will be renamed to .yml.bak, and .json file will be created.
        // otherwise, do not migrate.
        return oldFile.exists() && !configSource.fileExists();
    }

    @Override
    public void migrate(IMigrationHelper migrationHelper) {
        migrationHelper.migrate(configSource);
        configSource.reload();
    }
}
