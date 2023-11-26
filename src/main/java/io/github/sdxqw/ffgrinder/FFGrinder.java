package io.github.sdxqw.ffgrinder;

import io.github.sdxqw.ffgrinder.config.SpawnerConfig;
import io.github.sdxqw.ffgrinder.listener.SpawnerListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class FFGrinder extends JavaPlugin {

    @Getter
    private static FFGrinder instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        SpawnerConfig spawnerConfig = new SpawnerConfig(getConfig());
        getServer().getPluginManager().registerEvents(new SpawnerListener(spawnerConfig), this);
    }
}
