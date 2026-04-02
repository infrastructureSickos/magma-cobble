package com.infrastructuresickos.magma_cobble;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Implements MagmaCobble mechanics:
 *  1. Water + lava interaction → magma block (instead of cobblestone/obsidian)
 *  2. Breaking a magma block → places a lava source (unless silk touch)
 *
 * Registered manually on the FORGE bus — do NOT add @Mod.EventBusSubscriber.
 */
public class MagmaCobbleEventHandler {

    // -------------------------------------------------------------------------
    // Water + lava → magma block
    // -------------------------------------------------------------------------

    /**
     * Intercepts fluid-places-block events. When water contacts lava and would
     * produce cobblestone or obsidian, change the result to a magma block instead.
     */
    @SubscribeEvent
    public void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
        BlockState newState = event.getNewState();
        if (newState.is(Blocks.COBBLESTONE) || newState.is(Blocks.OBSIDIAN)) {
            event.setNewState(Blocks.MAGMA_BLOCK.defaultBlockState());
        }
    }

    // -------------------------------------------------------------------------
    // Breaking magma block → lava source
    // -------------------------------------------------------------------------

    /**
     * When a player breaks a magma block without silk touch, cancel the default
     * break, remove the block, and place a lava source in its place.
     * Silk touch bypasses this — the block drops normally via vanilla behaviour.
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(Blocks.MAGMA_BLOCK)) return;

        Player player = event.getPlayer();
        boolean hasSilkTouch = player != null
                && EnchantmentHelper.getItemEnchantmentLevel(
                        Enchantments.SILK_TOUCH, player.getMainHandItem()) > 0;

        if (hasSilkTouch) return; // drop the block item normally

        // Cancel the vanilla break so no item is dropped, then remove the block
        // and place a lava source. level.removeBlock() does not re-fire BreakEvent.
        event.setCanceled(true);

        // Damage the player's tool as if they mined the block
        if (player != null) {
            player.getMainHandItem().mineBlock(level, level.getBlockState(pos), pos, player);
        }

        level.removeBlock(pos, false);
        level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
    }
}
