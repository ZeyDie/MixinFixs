package com.zeydie.mixinfixs.mixin.ic2.common;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({TileEntityIndustrialWorkbench.class})
public final class MixinTileEntityIndustrialWorkbench {
    @Shadow
    @Final
    public InvSlot craftingGrid;

    @Shadow
    @Final
    public InvSlot craftingStorage;

    @Overwrite
    private static int getPossible(int max, ItemStack existing, ItemStack in) {
        int amount = Math.min(max, in.isStackable() ? in.getMaxStackSize() : 1);
        if (!StackUtil.isEmpty(existing)) {
            if (!StackUtil.checkItemEqualityStrict(existing, in))
                return 0;
            amount -= StackUtil.getSize(existing);
        }
        return Math.min(amount, StackUtil.getSize(in));
    }

    @Overwrite
    private static ItemStack transfer(InvSlot slot, ItemStack gridItem, boolean allowEmpty) {
        for (int index = 0; index < slot.size(); index++) {
            ItemStack stack = slot.get(index);
            int amount = getPossible(slot.getStackSizeLimit(), stack, gridItem);
            if (amount >= 1) {
                if (StackUtil.isEmpty(stack)) {
                    if (!allowEmpty)
                        continue;
                    slot.put(index, StackUtil.copyWithSize(gridItem, amount));
                } else {
                    slot.put(index, StackUtil.incSize(stack, amount));
                }
                gridItem = StackUtil.decSize(gridItem, amount);
                if (StackUtil.isEmpty(gridItem))
                    break;
            }
            continue;
        }
        return gridItem;
    }

    @Overwrite(remap = false)
    public void clear(EntityPlayer player) {
        if (!this.craftingGrid.isEmpty()) {
            int index;
            label26: for (index = 0; index < this.craftingGrid.size(); index++) {
                if (!this.craftingGrid.isEmpty(index)) {
                    ItemStack stack = this.craftingGrid.get(index);
                    for (int pass = 0; pass < 2; pass++) {
                        stack = transfer(this.craftingStorage, stack, (pass == 1));
                        if (StackUtil.isEmpty(stack)) {
                            this.craftingGrid.clear(index);
                            continue label26;
                        }
                    }
                    if (player.inventory.getFirstEmptyStack() > -1)
                        if (StackUtil.storeInventoryItem(stack, player, false)) {
                            this.craftingGrid.clear(index);
                        } else {
                            this.craftingGrid.put(stack);
                        }
                }
            }
        }
    }
}
