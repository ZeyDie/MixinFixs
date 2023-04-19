package com.zeydie.mixinfixs.mixin.draconicevolution.common;

import com.brandon3055.draconicevolution.integration.ModHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;

@Mixin(ModHelper.class)
public final class MixinModHelper {
    private static final List<String> blackList = new ArrayList<>();

    static {
        blackList.add("ic2");
        blackList.add("tconstruct");
    }

    @Overwrite
    public static boolean canRemoveEnchants(ItemStack stack) {
        if (!stack.isEmpty()) {
            ResourceLocation registry = stack.getItem().getRegistryName();
            if (registry != null)
                return !blackList.contains(registry.getResourceDomain());
        }
        return false;
    }
}
