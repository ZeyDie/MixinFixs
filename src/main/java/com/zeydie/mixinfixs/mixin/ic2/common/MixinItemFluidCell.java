package com.zeydie.mixinfixs.mixin.ic2.common;

import ic2.core.item.ItemFluidCell;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemFluidCell.class)
public final class MixinItemFluidCell {
    @Overwrite(remap = false)
    private boolean interactWithTank(EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing side) {
        assert !world.isRemote;

        IFluidHandler tileHandler = FluidUtil.getFluidHandler(world, pos, side);

        if (tileHandler == null) {
            return false;
        } else {
            ItemStack stack = StackUtil.get(player, hand);
            boolean single = StackUtil.getSize(stack) == 1;

            //TODO ZeyCodeClear
            /*if (!single) {
                stack = StackUtil.decSize(stack); //StackUtil.copyWithSize(stack, 1);
            }*/

            boolean changeMade = false;

            do {
                IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(StackUtil.copy(stack));

                assert itemHandler != null;

                if (FluidUtil.tryFluidTransfer(tileHandler, itemHandler, Integer.MAX_VALUE, true) == null) {
                    break;
                }

                if (single) {
                    StackUtil.set(player, hand, itemHandler.getContainer());
                    return true;
                }

                StackUtil.consumeOrError(player, hand, 1);
                StackUtil.storeInventoryItem(itemHandler.getContainer(), player, false);
                changeMade = true;
            } while(!StackUtil.isEmpty(player, hand));

            return changeMade;
        }
    }
}
