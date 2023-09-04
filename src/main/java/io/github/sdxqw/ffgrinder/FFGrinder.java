package io.github.sdxqw.ffgrinder;

import io.github.sdxqw.ffgrinder.command.CommandItems;
import io.github.sdxqw.ffgrinder.config.SpawnerConfig;
import io.github.sdxqw.ffgrinder.listener.SpawnerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class FFGrinder extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        SpawnerConfig spawnerConfig = new SpawnerConfig(getConfig());
        CommandItems commandItems = new CommandItems(this, spawnerConfig);
        commandItems.loadCommand();
        getServer().getPluginManager().registerEvents(new SpawnerListener(commandItems, spawnerConfig), this);
    }
}
