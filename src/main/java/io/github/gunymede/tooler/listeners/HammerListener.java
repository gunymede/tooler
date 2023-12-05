package io.github.gunymede.tooler.listeners;

import io.github.gunymede.tooler.ToolerPlugin;
import io.github.gunymede.tooler.Util;
import io.github.gunymede.tooler.classes.Vec3d;
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

import java.util.List;

public class HammerListener extends BlockListener {
    private boolean debounce;

    private final Material[] PICKAXES = {
            Material.WOOD_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLD_PICKAXE,
            Material.DIAMOND_PICKAXE,
    };

    // Ain't got no time for rotating vectors
    private final Vec3d[] OFFSETS_UP_DOWN = {
            new Vec3d(1, 0, 0),
            new Vec3d(1, 0, 1),
            new Vec3d(1, 0, -1),
            new Vec3d(-1, 0, 0),
            new Vec3d(-1, 0, 1),
            new Vec3d(-1, 0, -1),
            new Vec3d(0, 0, 1),
            new Vec3d(0, 0, -1),
    };

    private final Vec3d[] OFFSETS_EAST_WEST = {
            new Vec3d(1, 0, 0),
            new Vec3d(-1, 0, 0),
            new Vec3d(1, 1, 0),
            new Vec3d(1, -1, 0),
            new Vec3d(-1, 1, 0),
            new Vec3d(-1, -1, 0),
            new Vec3d(0, 1, 0),
            new Vec3d(0, -1, 0),
    };

    private final Vec3d[] OFFSETS_SOUTH_NORTH = {
            new Vec3d(0, 0, 1),
            new Vec3d(0, 0, -1),
            new Vec3d(0, 1, 1),
            new Vec3d(0, -1, 1),
            new Vec3d(0, 1, -1),
            new Vec3d(0, -1, -1),
            new Vec3d(0, 1, 0),
            new Vec3d(0, -1, 0),
    };

    private final Material[] VALID_HAMMER_BLOCKS = {
            Material.STONE,
            Material.COBBLESTONE,
    };

    private final ItemStack DROPPED_ITEM = new ItemStack(Material.COBBLESTONE, 1);

    public HammerListener(ToolerPlugin plugin, PluginManager pluginManager) {
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Lowest, plugin);
    }
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (debounce) return;

        Block block = event.getBlock();
        if (Util.notInArray(block.getType(), VALID_HAMMER_BLOCKS)) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack heldItem = player.getItemInHand();
        if (Util.notInArray(heldItem.getType(), PICKAXES)) return;

        debounce = true;

        BlockFace brokenFace = getBlockFace(player);
        Vec3d[] offsets = getBlockOffsets(brokenFace);

        int blocksBroken = breakStoneBlocks(block, player, offsets);
        Util.removeHeldItemDurability(blocksBroken, heldItem, player);

        debounce = false;
    }

    //https://www.spigotmc.org/threads/getting-the-blockface-of-a-targeted-block.319181/#post-3002432
    private BlockFace getBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
        if (lastTwoTargetBlocks.size() != 2) return null;

        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);

        return targetBlock.getFace(adjacentBlock);
    }

    private Vec3d[] getBlockOffsets(BlockFace brokenFace) {
        if (brokenFace == BlockFace.UP || brokenFace == BlockFace.DOWN) {
            return OFFSETS_UP_DOWN;
        } else if (brokenFace == BlockFace.EAST || brokenFace == BlockFace.WEST) {
            return OFFSETS_EAST_WEST;
        } else {
            return OFFSETS_SOUTH_NORTH;
        }
    }

    private int breakStoneBlocks(Block originBlock, Player player, Vec3d[] offsets) {
        int brokenBlocks = 0;

        Location originLocation = originBlock.getLocation();
        World world = originBlock.getWorld();

        int originX = originLocation.getBlockX();
        int originY = originLocation.getBlockY();
        int originZ = originLocation.getBlockZ();

        for ( Vec3d offset : offsets ) {
            Block block = world.getBlockAt(originX + offset.x, originY + offset.y, originZ + offset.z);
            if (Util.notInArray(block.getType(), VALID_HAMMER_BLOCKS)) continue;

            Util.breakBlock(block, player, DROPPED_ITEM, true);
            brokenBlocks++;
        }

        return brokenBlocks;
    }
}
