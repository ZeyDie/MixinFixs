package com.zeydie.mixinfixs.mixin.thermalexpansion.common;

import cofh.core.util.helpers.ItemHelper;
import cofh.thermalexpansion.block.storage.BlockCache;
import cofh.thermalexpansion.block.storage.TileCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin({BlockCache.class})
public final class MixinBlockCache {
    @Overwrite(remap = false)
    public static boolean insertAllItemsFromPlayer(TileCache tile, EntityPlayer player) {
        boolean playSound = false;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            if (tile.insertItem(player.inventory.getStackInSlot(i), true) != player.inventory.getStackInSlot(i)) {
                player.inventory.setInventorySlotContents(i, tile.insertItem(player.inventory.getStackInSlot(i), false));
                playSound = true;
            }
        }
        if (playSound)
            tile.getWorld().playSound(null, tile.getPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.1F, 0.7F);
        return playSound;
    }

    @Overwrite(remap = false)
    public boolean onBlockActivatedDelegate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileCache tile = (TileCache) world.getTileEntity(pos);
        if (tile != null && tile.canPlayerAccess(player)) {
            if (ItemHelper.isPlayerHoldingNothing(player) && player.isSneaking()) {
                tile.setLocked(!tile.isLocked());
                if (tile.isLocked()) {
                    world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.2F, 0.8F);
                } else {
                    world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.5F);
                }
            } else {
                boolean i = false;
                boolean playSound = false;
                ItemStack heldItem = player.getHeldItem(hand);
                ItemStack ret = tile.insertItem(heldItem, false);
                long time = player.getEntityData().getLong("thermalexpansion:CacheClick");
                long currentTime = world.getTotalWorldTime();
                player.getEntityData().setLong("thermalexpansion:CacheClick", currentTime);
                if (!player.capabilities.isCreativeMode) {
                    if (ret != heldItem) {
                        player.setHeldItem(hand, ret);
                        playSound = true;
                    }
                    if (!tile.getStoredInstance().isEmpty() && currentTime - time < 15L)
                        i = playSound & (!insertAllItemsFromPlayer(tile, player) ? true : false);
                }
                if (i)
                    world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.1F, 0.7F);
            }
            return true;
        }
        return false;
    }
}
