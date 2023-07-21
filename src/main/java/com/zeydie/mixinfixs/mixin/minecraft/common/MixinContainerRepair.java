package com.zeydie.mixinfixs.mixin.minecraft.common;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerRepair.class)
public abstract class MixinContainerRepair extends Container {
    @Shadow(remap = false)
    public IInventory outputSlot;
    @Shadow(remap = false)
    public IInventory inputSlots;

    @Inject(method = "updateRepairOutput", at = @At("HEAD"), cancellable = true)
    public void onUpdateRepairOutput(CallbackInfo callbackInfo) {
        final ItemStack itemStackSlot = this.inputSlots.getStackInSlot(0);

        if (!itemStackSlot.isEmpty()) {
            final Block block = Block.getBlockFromItem(itemStackSlot.getItem());

            if (block != Blocks.AIR) {
                this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);

                callbackInfo.cancel();
            }
        }
    }
}
