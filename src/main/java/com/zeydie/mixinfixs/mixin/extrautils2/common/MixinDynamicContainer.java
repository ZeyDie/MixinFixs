package com.zeydie.mixinfixs.mixin.extrautils2.common;

import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DynamicContainer.class)
public abstract class MixinDynamicContainer extends Container {
    @Shadow
    public int playerSlotsStart;

    @Overwrite
    @ItemStackNonNull
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
        ItemStack itemstack = StackHelper.empty();
        Slot slot = this.inventorySlots.get(par2);
        ItemStack otherItemStack;

        if (slot instanceof SlotCrafting) {
            otherItemStack = slot.getStack().copy();
            if (slot.getHasStack() && StackHelper.isNonNull(otherItemStack)) {
                itemstack = otherItemStack;
                if (!this.mergeItemStack(otherItemStack, this.playerSlotsStart, this.inventorySlots.size(), true)) {
                    return StackHelper.empty();
                }

                slot.onSlotChange(otherItemStack, itemstack);

                if (StackHelper.isEmpty(otherItemStack)) {
                    slot.putStack(StackHelper.empty());
                } else {
                    slot.onSlotChanged();
                }

                if (StackHelper.getStacksize(otherItemStack) == StackHelper.getStacksize(itemstack)) {
                    return StackHelper.empty();
                }

                CompatHelper.setSlot(slot, par1EntityPlayer, otherItemStack);
            }

            return itemstack;
        } else {
            if (this.playerSlotsStart > 0 && slot != null && slot.getHasStack()) {
                otherItemStack = slot.getStack().copy();
                if (StackHelper.isNull(otherItemStack)) {
                    return StackHelper.empty();
                }

                itemstack = otherItemStack;

                if (par2 < this.playerSlotsStart) {
                    if (!this.mergeItemStack(otherItemStack, this.playerSlotsStart, this.inventorySlots.size(), true)) {
                        return StackHelper.empty();
                    }
                } else if (!this.mergeItemStack(otherItemStack, 0, this.playerSlotsStart, false)) {
                    return StackHelper.empty();
                }

                if (StackHelper.isEmpty(otherItemStack)) {
                    slot.putStack(StackHelper.empty());
                } else {

                    //TODO ZeyCodeStart
                    slot.putStack(itemstack);
                    //TODO ZeyCodeEnd

                    slot.onSlotChanged();
                }
            }

            return itemstack;
        }
    }
}
