package io.github.gunymede.tooler.listeners;

import io.github.gunymede.tooler.ToolerPlugin;
import io.github.gunymede.tooler.Util;
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
import java.util.HashMap;

public class VeinMinerListener extends BlockListener {
    private boolean debounce;

    private final HashMap<Material, ItemStack> ORE_TO_DROP = new HashMap<Material, ItemStack>() {{
            put(Material.COAL_ORE,              new ItemStack(Material.COAL));
            put(Material.IRON_ORE,              new ItemStack(Material.IRON_ORE));
            put(Material.GOLD_ORE,              new ItemStack(Material.GOLD_ORE));
            put(Material.DIAMOND_ORE,           new ItemStack(Material.DIAMOND));
            put(Material.REDSTONE_ORE,          new ItemStack(Material.REDSTONE, 4));
            put(Material.GLOWING_REDSTONE_ORE,  new ItemStack(Material.REDSTONE, 4));
            put(Material.LAPIS_ORE,             new ItemStack(Material.INK_SACK.getId(), 7, (short) 4));
    }};

    private final HashMap<Integer, Integer> PRIMARY_TO_SECONDARY_BLOCK_ID = new HashMap<Integer, Integer>() {{
        put(Material.REDSTONE_ORE.getId(), Material.GLOWING_REDSTONE_ORE.getId());
        put(Material.GLOWING_REDSTONE_ORE.getId(), Material.REDSTONE_ORE.getId());
    }};

    private final Material[] PICKAXES = {
            Material.WOOD_PICKAXE,
            Material.GOLD_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.DIAMOND_PICKAXE,
    };

    private final HashMap<Material, Integer> ORE_HARDNESS = new HashMap<Material, Integer>() {{
        put(Material.COAL_ORE, 0);
        put(Material.LAPIS_ORE, 2);
        put(Material.IRON_ORE, 2);
        put(Material.GOLD_ORE, 3);
        put(Material.DIAMOND_ORE, 3);
        put(Material.REDSTONE_ORE, 3);
        put(Material.GLOWING_REDSTONE_ORE, 3);
    }};
    
    public VeinMinerListener(ToolerPlugin plugin, PluginManager pluginManager) {
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Lowest, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (debounce) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack heldItem = player.getItemInHand();
        if (Util.notInArray(heldItem.getType(), PICKAXES)) return;

        Block block = event.getBlock();
        if (!isValidBlock(block)) return;

        debounce = true;

        int blocksBroken = mineValidBlocks(block, player, canDropItem(heldItem, block));
        Util.removeHeldItemDurability(blocksBroken, heldItem, player);

        debounce = false;
    }

    private boolean isValidBlock(Block block) {
        return ORE_TO_DROP.get(block.getType()) != null;
    }

    private boolean canDropItem(ItemStack item, Block block) {
        int pickaxeHardness = Util.getIndex(item.getType(), PICKAXES);
        int blockHardness = ORE_HARDNESS.get(block.getType());

        return pickaxeHardness >= blockHardness;
    }

    private int mineValidBlocks(Block originBlock, Player player, boolean dropItem) {
        World world = originBlock.getWorld();

        Material blockMaterial = originBlock.getType();

        int materialId = blockMaterial.getId();
        ItemStack blockDrop = ORE_TO_DROP.get(blockMaterial);

        ArrayList<Location> blockLocations = getVeinBlockLocations(world, originBlock.getLocation(), materialId);

        for (Location location : blockLocations) {
            Block block = world.getBlockAt(location);
            Util.breakBlock(block, player, blockDrop, dropItem);
        }

        return blockLocations.size()-1;
    }

    private ArrayList<Location> getVeinBlockLocations(World world, Location originLocation, int materialId) {
        ArrayList<Location> checkedLocations = new ArrayList<>();
        ArrayList<Location> uncheckedLocations = new ArrayList<>();

        uncheckedLocations.add(originLocation);
        checkedLocations.add(originLocation);

        int secondaryId = PRIMARY_TO_SECONDARY_BLOCK_ID.getOrDefault(materialId, -1);

        while (!uncheckedLocations.isEmpty()) {
            ArrayList<Location> newUncheckedLocations = new ArrayList<>();

            for ( Location location : uncheckedLocations ) {
                Block block = world.getBlockAt(location);

                for ( BlockFace face : BlockFace.values()) {
                    Block adjacentBlock = block.getRelative(face);

                    int adjacentId = adjacentBlock.getTypeId();
                    if (adjacentId != materialId && adjacentId != secondaryId) continue;

                    Location adjacentLocation = adjacentBlock.getLocation();

                    if (checkedLocations.contains(adjacentLocation)) continue;

                    checkedLocations.add(adjacentLocation);
                    newUncheckedLocations.add(adjacentLocation);
                }
             }

            uncheckedLocations.clear();
            uncheckedLocations.addAll(newUncheckedLocations);
        }

        checkedLocations.remove(originLocation);

        return checkedLocations;
    }

}
