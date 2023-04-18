package com.zeydie.mixinfixs.mixin.thermalexpansion.common;

import cofh.core.gui.container.ContainerInventoryItem;
import cofh.core.gui.slot.SlotLocked;
import cofh.thermalexpansion.gui.container.storage.ContainerSatchel;
import cofh.thermalexpansion.item.ItemSatchel;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerSatchel.class)
public abstract class MixinContainerSatchel extends ContainerInventoryItem {
    public MixinContainerSatchel(ItemStack stack, InventoryPlayer inventory) {
        super(stack, inventory);
    }

    @Shadow
    public abstract int getPlayerInventoryHorizontalOffset();

    @Shadow
    public abstract int getPlayerInventoryVerticalOffset();

    @Overwrite(remap = false)
    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        int xOffset = getPlayerInventoryHorizontalOffset();
        int yOffset = getPlayerInventoryVerticalOffset();
        int i;
        for (i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, xOffset + j * 18, yOffset + i * 18));
        }
        for (i = 0; i < 9; i++) {
            //TODO ZeyCodeStart
            if (inventoryPlayer.mainInventory.get(i).getItem() instanceof ItemSatchel) {
                //TODO ZeyCodeEnd
                //TODO ZeyCodeClear
                //if (i == inventoryPlayer.currentItem) {
                addSlotToContainer(new SlotLocked(inventoryPlayer, i, xOffset + i * 18, yOffset + 58));
            } else {
                addSlotToContainer(new Slot(inventoryPlayer, i, xOffset + i * 18, yOffset + 58));
            }
        }
    }
}
