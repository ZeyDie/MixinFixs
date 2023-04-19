package com.zeydie.mixinfixs.mixin.draconicevolution.common;

import com.brandon3055.brandonscore.inventory.ContainerBCBase;
import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorCore;
import com.brandon3055.draconicevolution.inventory.ContainerReactor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(ContainerReactor.class)
public abstract class MixinContainerReactor extends ContainerBCBase<TileReactorCore> {
    @Shadow(remap = false)
    public abstract int getFuelValue(ItemStack itemStack);

    @Shadow(remap = false)
    public abstract int getChaosValue(ItemStack itemStack);

    @Overwrite(remap = false)
    @Nullable
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        int maxFuel = 10383;
        int installedFuel = (int) (this.tile.reactableFuel.value + this.tile.convertedFuel.value);
        int free = maxFuel - installedFuel;
        Slot slot = getSlot(slotId);
        if (slot instanceof ContainerReactor.SlotReactor && clickTypeIn == ClickType.PICKUP) {
            InventoryPlayer inventory = player.inventory;
            ItemStack stackInSlot = slot.getStack();
            ItemStack heldStack = inventory.getItemStack();
            if (!heldStack.isEmpty()) {
                ItemStack copy = heldStack.copy();
                copy.setCount(1);
                int value;
                if ((value = getFuelValue(copy)) > 0) {
                    int maxInsert = free / value;
                    int insert = Math.min(Math.min(heldStack.getCount(), maxInsert), (dragType == 1) ? 1 : 64);
                    this.tile.reactableFuel.value += (insert * value);
                    heldStack.shrink(insert);
                } else if ((value = getChaosValue(copy)) > 0) {
                    int maxInsert = free / value;
                    int insert = Math.min(Math.min(heldStack.getCount(), maxInsert), (dragType == 1) ? 1 : 64);
                    this.tile.convertedFuel.value += (insert * value);
                    heldStack.shrink(insert);
                }
                if (heldStack.getCount() <= 0)
                    inventory.setItemStack(ItemStack.EMPTY);
            } else if (!stackInSlot.isEmpty()) {
                this.tile.reactableFuel.value -= getFuelValue(stackInSlot);
                this.tile.convertedFuel.value -= getChaosValue(stackInSlot);
                inventory.setItemStack(stackInSlot);
            }
            return ItemStack.EMPTY;
        }

        //TODO ZeyCodeStart
        if (clickTypeIn == ClickType.PICKUP_ALL)
            return ItemStack.EMPTY;
        //TODO ZeyCodeEnd

        if (slotId <= 35)
            return super.slotClick(slotId, dragType, clickTypeIn, player);

        return ItemStack.EMPTY;
    }
}
