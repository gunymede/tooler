package io.github.gunymede.tooler;

import java.util.Random;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Util {
    public static int getIndex(Object value, Object[] array) {
        for (int index = 0; index < array.length; ++index) {
            if (!value.equals(array[index])) continue;
            return index;
        }
        return -1;
    }

    public static boolean notInArray(Object value, Object[] array) {
        return Util.getIndex(value, array) == -1;
    }

    public static void removeHeldItemDurability(int removedDurability, ItemStack heldItem, Player player) {
        PlayerInventory inventory = player.getInventory();
        Material itemMaterial = heldItem.getType();

        short maxDurability = itemMaterial.getMaxDurability();
        short durability = heldItem.getDurability();

        player.incrementStatistic(Statistic.USE_ITEM, itemMaterial, removedDurability);
        short newDurability = (short)Math.min(maxDurability, durability + removedDurability);

        heldItem.setDurability(newDurability);
        inventory.setItemInHand(heldItem);
    }

    public static void breakBlock(Block block, Player player, ItemStack blockDrop, boolean dropItem) {
        if (block.getType() == Material.AIR) {
            return;
        }

        Random random = new Random();
        World world = block.getWorld();
        Location location = block.getLocation();

        player.getServer().getPluginManager().callEvent(new BlockBreakEvent(block, player));

        Material material = block.getType();
        world.playEffect(location, Effect.STEP_SOUND, material.getId());
        player.incrementStatistic(Statistic.MINE_BLOCK, material);

        block.setType(Material.AIR);

        if (!dropItem) {
            return;
        }

        ItemStack droppedItem = blockDrop.clone();
        int itemNum = 1 + random.nextInt(droppedItem.getAmount() + 1);
        droppedItem.setAmount(itemNum);

        world.dropItemNaturally(location, droppedItem);
    }
}
