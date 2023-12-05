package io.github.gunymede.tooler.listeners;

import io.github.gunymede.tooler.ToolerPlugin;
import io.github.gunymede.tooler.Util;
import io.github.gunymede.tooler.classes.DelayedBlockRemover;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;

public class TreefellerListener extends BlockListener {
    private final ToolerPlugin plugin;

    private boolean debounce;

    public void setDebounce(boolean debounce) {
        this.debounce = debounce;
    }

    private final Material[] AXES = {
            Material.WOOD_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE,
            Material.GOLD_AXE,
            Material.DIAMOND_AXE,
    };

    public TreefellerListener(ToolerPlugin plugin, PluginManager pluginManager) {
        this.plugin = plugin;
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Lowest, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (debounce) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack heldItem = player.getItemInHand();
        if (Util.notInArray(heldItem.getType(), AXES)) return;

        Block block = event.getBlock();
        if (block.getType() != Material.LOG) return;

        int blocksBroken = scheduleAdjacentBlockBreaks(block.getLocation(), player);
        if (blocksBroken == 0) return;

        Util.removeHeldItemDurability(blocksBroken, heldItem, player);
    }

    private int scheduleAdjacentBlockBreaks(Location orginLocation, Player player) {
        int blockNum = 0;
        int currentDelay = 1;

        World world = orginLocation.getWorld();

        ArrayList<Location> uncheckedLocations = new ArrayList<>();
        ArrayList<Location> checkedLocations = new ArrayList<>();

        orginLocation = world.getBlockAt(orginLocation).getLocation();

        uncheckedLocations.add(orginLocation);
        checkedLocations.add(orginLocation);

        DelayedBlockRemover delayedBlockRemover = new DelayedBlockRemover(this, plugin, player);

        boolean foundLeaves = false;

        while (!uncheckedLocations.isEmpty()) {
            ArrayList<Location> newUncheckedLocations = new ArrayList<>();

            currentDelay++;

            for (Location location : uncheckedLocations) {
                Block block = world.getBlockAt(location);

                for (BlockFace face : BlockFace.values()) {
                    Block relativeBlock = block.getRelative(face);

                    Location relativeLocation = relativeBlock.getLocation();
                    Material adjacentMaterial = relativeBlock.getType();

                    if (checkedLocations.contains(relativeLocation)) continue;

                    if (adjacentMaterial == Material.LOG) {
                        blockNum += 1;
                        delayedBlockRemover.addLocation(relativeLocation, currentDelay);

                        newUncheckedLocations.add(relativeLocation);
                        checkedLocations.add(relativeLocation);
                    } else if ( adjacentMaterial == Material.LEAVES ) {
                        foundLeaves = true;
                    }
                }
            }

            uncheckedLocations.clear();
            uncheckedLocations.addAll(newUncheckedLocations);
        }

        if (!foundLeaves) return 0;

        delayedBlockRemover.removeBlocks(0);

        return blockNum;
    }
}