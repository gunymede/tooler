package io.github.gunymede.tooler;

import io.github.gunymede.tooler.listeners.HammerListener;
import io.github.gunymede.tooler.listeners.PlanterListener;
import io.github.gunymede.tooler.listeners.TreefellerListener;
import io.github.gunymede.tooler.listeners.VeinMinerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class ToolerPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Configuration config = this.getConfiguration();
        config.load();

        PluginManager pluginManager = this.getServer().getPluginManager();

        if (config.getBoolean("veinminer", true)) {
            new VeinMinerListener(this, pluginManager);
        }

        if (config.getBoolean("hammer", true)) {
            new HammerListener(this, pluginManager);
        }

        if (config.getBoolean("planter", true)) {
            new PlanterListener(this, pluginManager);
        }

        if (config.getBoolean("treefeller", true)) {
            new TreefellerListener(this, pluginManager);
        }

        config.save();
    }

    @Override
    public void onDisable() {

    }
}
