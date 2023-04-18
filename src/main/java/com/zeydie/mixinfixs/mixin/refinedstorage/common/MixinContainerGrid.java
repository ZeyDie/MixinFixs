package com.zeydie.mixinfixs.mixin.refinedstorage.common;

import com.raoulvdberge.refinedstorage.container.ContainerBase;
import com.raoulvdberge.refinedstorage.container.ContainerGrid;
import com.raoulvdberge.refinedstorage.tile.TileBase;
import com.raoulvdberge.refinedstorage.tile.grid.TileGrid;
import com.raoulvdberge.refinedstorage.tile.grid.portable.TilePortableGrid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(ContainerGrid.class)
public abstract class MixinContainerGrid extends ContainerBase {
    @Shadow
    public abstract void onContainerClosed(EntityPlayer player);

    public MixinContainerGrid(@Nullable TileBase tile, EntityPlayer player) {
        super(tile, player);
    }

    @Override
    public ItemStack slotClick(int id, int dragType, ClickType clickType, EntityPlayer player) {
        TileEntity tileEntity = this.getTile();

        if (tileEntity != null) {
            final World world = tileEntity.getWorld();
            final BlockPos blockPos = tileEntity.getPos();
            final IBlockState block = world.getBlockState(blockPos);

            tileEntity = world.getTileEntity(blockPos);

            if (block.getBlock() == Blocks.AIR || (!(tileEntity instanceof TileGrid) && !(tileEntity instanceof TilePortableGrid))) {
                player.closeScreen();

                return ItemStack.EMPTY;
            }
        }

        return super.slotClick(id, dragType, clickType, player);
    }
}
