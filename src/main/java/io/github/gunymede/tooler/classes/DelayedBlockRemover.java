package io.github.gunymede.tooler.classes;


import io.github.gunymede.tooler.Util;
import io.github.gunymede.tooler.listeners.TreefellerListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;

public class DelayedBlockRemover {
    private final TreefellerListener listener;
    private final World world;
    private final Plugin plugin;
    private final Player player;

    ConcurrentHashMap<Location, Integer> blockLocationAndDelay = new ConcurrentHashMap<>();

    public DelayedBlockRemover(TreefellerListener treefellerListener, Plugin plugin, Player player) {
        this.listener = treefellerListener;
        this.plugin = plugin;
        this.world = player.getWorld();
        this.player = player;
    }

    public void addLocation(Location location, int delay) {
        blockLocationAndDelay.put(location, delay);
    }

    public void removeBlocks(int currentDelay) {
        if (blockLocationAndDelay.isEmpty()) return;

        for (Location location : blockLocationAndDelay.keySet()) {
            if (currentDelay < blockLocationAndDelay.get(location)) continue;

            Block block = world.getBlockAt(location);
            ItemStack item = new ItemStack(block.getTypeId(), 0, block.getData());

            listener.setDebounce(true);
            Util.breakBlock(block, player, item, true);
            listener.setDebounce(false);

            blockLocationAndDelay.remove(location);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> removeBlocks(currentDelay + 1));
    }
}
