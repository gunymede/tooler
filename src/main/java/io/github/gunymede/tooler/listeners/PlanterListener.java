package io.github.gunymede.tooler.listeners;

import io.github.gunymede.tooler.ToolerPlugin;
import io.github.gunymede.tooler.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;

import java.util.List;

public class PlanterListener extends BlockListener {
    private final ToolerPlugin plugin;

    private final Material[] HOES = {
            Material.WOOD_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLD_HOE,
            Material.DIAMOND_HOE,
    };

    private final double BOUNDING_BOX_SIZE = 16;

    public PlanterListener(ToolerPlugin plugin, PluginManager pluginManager) {
        this.plugin = plugin;

        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Lowest, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack heldItem = player.getItemInHand();
        if (Util.notInArray(heldItem.getType(), HOES)) return;

        Block block = event.getBlock();
        if (block.getType() != Material.CROPS) { return; }

        byte blockData = block.getData();

        if (blockData < 7) {
            event.setCancelled(true);
            player.sendBlockChange(block.getLocation(), Material.CROPS, blockData);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> tryReplant(player, block));
        }
    }

    private void tryReplant(Player player, Block block) {
        if (block.getType() != Material.AIR) return;

        boolean usedSeed = tryDepleteSeed(player);
        if (!usedSeed) return;

        block.setType(Material.CROPS);
        block.setData((byte) 0);

        player.sendBlockChange(block.getLocation(), Material.CROPS, (byte) 0);
    }

    private boolean tryDepleteSeed(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        List<Entity> nearbyEntities = player.getNearbyEntities(BOUNDING_BOX_SIZE, BOUNDING_BOX_SIZE, BOUNDING_BOX_SIZE);

        for ( Entity entity : nearbyEntities ) {
            if (!(entity instanceof Item)) continue;

            Item item = (Item) entity;
            ItemStack itemStack = item.getItemStack();

            if (itemStack.getType() != Material.SEEDS || itemStack.getAmount() != 1) continue;

            item.remove();

            return true;
        }

        if (playerInventory.contains(Material.SEEDS)) {
            playerInventory.removeItem(new ItemStack(Material.SEEDS, 1));
            return true;
        }

        return false;
    }
}
