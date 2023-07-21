package com.zeydie.mixinfixs.mixin.minecraft.common;

import com.gamerforea.eventhelper.util.EventUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityArmorStand.class)
public abstract class MixinEntityArmorStand extends EntityLivingBase {

    public MixinEntityArmorStand(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Inject(method = "attackEntityFrom", at = @At("HEAD"), cancellable = true)
    public void onAttackEntityFrom(DamageSource source, float damage, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final Entity entity = source.getTrueSource();

        System.out.println("|Mixin: " + entity);

        if (entity instanceof EntityPlayer)
            if (EventUtils.cantAttack((EntityPlayer) entity, this))
                callbackInfoReturnable.setReturnValue(false);
    }
}
