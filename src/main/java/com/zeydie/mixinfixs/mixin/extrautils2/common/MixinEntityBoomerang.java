package com.zeydie.mixinfixs.mixin.extrautils2.common;

import com.gamerforea.eventhelper.util.EventUtils;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.DataWatcherItemStack;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.enchants.BoomerangEnchantment;
import com.rwtema.extrautils2.entity.EntityBoomerang;
import com.rwtema.extrautils2.items.ItemBoomerang;
import com.rwtema.extrautils2.utils.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Mixin(EntityBoomerang.class)
public abstract class MixinEntityBoomerang extends Entity {
    @Shadow
    public int flyTime;
    @Shadow
    @Final
    private static DataParameter<Byte> DATAWATCHER_OUT_FLAG;
    @Shadow
    @Final
    public static DataWatcherItemStack.Wrapper DATAWATCHER_STACK;

    @Shadow
    public abstract Entity getOwner();

    @Shadow
    public abstract void setMeDead();

    @Shadow
    public abstract int getEnchantmentLevel(BoomerangEnchantment enchantment);

    @Shadow
    public abstract Vec3d calcTargetVec();

    @Shadow
    public abstract Iterable<BlockPos> getNeighbourBlocks();

    @Shadow
    public int potionColor;

    @Shadow
    public abstract void addItem(Entity entity);

    @Shadow
    public abstract boolean isOwner(Entity entity);

    public MixinEntityBoomerang(World p_i1582_1_) {
        super(p_i1582_1_);

        setSize(0.5F, 0.5F);
        this.noClip = true;
    }

    @Overwrite
    public void onUpdate() {
        super.onUpdate();
        Entity owner = this.getOwner();
        boolean isRemote = this.world.isRemote;
        Vec3d dest = this.calcTargetVec();
        ++this.flyTime;
        boolean returning = (Byte) this.dataManager.get(DATAWATCHER_OUT_FLAG) != 0;
        Vec3d destDiff = dest.subtract(this.posX, this.posY, this.posZ);
        float d = MathHelper.sqrt(destDiff.x * destDiff.x + destDiff.y * destDiff.y + destDiff.z * destDiff.z);
        destDiff = destDiff.normalize();
        double acceleration = (double) this.flyTime * 0.001 + (returning ? 0.05 : 0.0);
        if (returning) {
            acceleration *= (double) (1 + this.getEnchantmentLevel(ItemBoomerang.SPEED));
        }

        if ((!((double) d < 1.0E-4) || this.flyTime <= 25) && !(acceleration > 1.0)) {
            this.motionX *= 1.0 - acceleration;
            this.motionY *= 1.0 - acceleration;
            this.motionZ *= 1.0 - acceleration;
            this.motionX += destDiff.x * acceleration;
            this.motionY += destDiff.y * acceleration;
            this.motionZ += destDiff.z * acceleration;
            float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * 180.0 / Math.PI);
            this.rotationPitch = (float) (MathHelper.atan2(this.motionY, (double) f) * 180.0 / Math.PI);
            if ((this.flyTime > 5 || returning) && MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) >= d) {
                this.setLocationAndAngles(dest.x, dest.y, dest.z, this.rotationYaw, this.rotationPitch);
                this.setMeDead();
            } else {
                if (this.world.isRemote) {
                    this.move(MoverType.SELF, motionX, motionY, motionZ);
                } else {
                    HashSet<BlockPos> prevPosSet = new HashSet();
                    if (this.flyTime > 1) {
                        Iterables.addAll(prevPosSet, this.getNeighbourBlocks());
                    }

                    this.move(MoverType.SELF, motionX, motionY, motionZ);
                    Iterator var11 = this.getNeighbourBlocks().iterator();

                    label229:
                    while (true) {
                        BlockPos newPos;
                        IBlockState blockState;
                        Block block;
                        do {
                            do {
                                do {
                                    if (!var11.hasNext()) {
                                        break label229;
                                    }

                                    newPos = (BlockPos) var11.next();
                                } while (prevPosSet.contains(newPos));

                                blockState = this.world.getBlockState(newPos);
                                block = blockState.getBlock();
                                if (block == Blocks.STONE_BUTTON || block == Blocks.WOODEN_BUTTON || block == Blocks.LEVER) {
                                    CompatHelper.activateBlock(block, this.world, newPos, blockState, FakePlayerFactory.getMinecraft((WorldServer) this.world), EnumHand.MAIN_HAND, (ItemStack) null, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
                                }
                            } while (this.getEnchantmentLevel(ItemBoomerang.DIGGING) <= 0);
                        } while (!(block instanceof IPlantable) && !(block instanceof IShearable));

                        block.dropBlockAsItem(this.world, newPos, blockState, 0);
                        this.world.setBlockState(newPos, Blocks.AIR.getDefaultState(), 3);
                        WorldHelper.markBlockForUpdate(this.world, newPos);
                    }
                }

                ItemStack potionStack = DataWatcherItemStack.getStack(this.dataManager, DATAWATCHER_STACK);
                if (isRemote && !returning) {
                    if (this.potionColor == -1) {
                        if (StackHelper.isNonNull(potionStack)) {
                            List<PotionEffect> effectsFromStack = PotionUtils.getEffectsFromStack(potionStack);
                            if (effectsFromStack.isEmpty()) {
                                this.potionColor = 0;
                            } else {
                                this.potionColor = PotionUtils.getPotionColorFromEffectList(effectsFromStack);
                            }
                        } else {
                            this.potionColor = 0;
                        }
                    }

                    double dx = this.posX - this.prevPosX;
                    double dy = this.posY - this.prevPosY;
                    double dz = this.posZ - this.prevPosZ;

                    for (int k = 0; k < 4; ++k) {
                        double t = (double) k / 4.0;
                        this.world.spawnParticle(EnumParticleTypes.CRIT, this.posX + dx * t, this.posY + dy * t, this.posZ + dz * t, -dx, -dy + 0.2, -dz, new int[0]);
                    }

                    if (this.potionColor != 0) {
                        double d0 = (double) (this.potionColor >> 16 & 255) / 255.0;
                        double d1 = (double) (this.potionColor >> 8 & 255) / 255.0;
                        double d2 = (double) (this.potionColor & 255) / 255.0;

                        for (int j = 0; j < 3; ++j) {
                            double t = (double) j / 3.0;
                            this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + dx * t, this.posY + dy * t, this.posZ + dz * t, d0, d1, d2, new int[0]);
                        }
                    }
                }

                Vec3d startVec = new Vec3d(this.posX, this.posY, this.posZ);
                Vec3d endVec = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
                RayTraceResult movingobjectposition = this.world.rayTraceBlocks(startVec, endVec, false, true, false);
                if (!isRemote) {
                    Entity entity = null;
                    List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().offset(this.motionX, this.motionY, this.motionZ).grow(1.0, 1.0, 1.0));
                    double d0 = -1.0;
                    Iterator var38 = list.iterator();

                    label186:
                    while (true) {
                        Entity e = null;
                        double d1;
                        do {
                            RayTraceResult mop;
                            do {
                                EntityPlayer entityplayer;
                                do {
                                    do {
                                        do {
                                            label156:
                                            do {
                                                while (var38.hasNext()) {
                                                    e = (Entity) var38.next();
                                                    if (!(e instanceof EntityItem) && !(e instanceof EntityXPOrb)) {
                                                        continue label156;
                                                    }

                                                    if (e.getRidingEntity() == null) {
                                                        this.addItem(e);
                                                    }
                                                }

                                                //TODO ZeyCodeStart
                                                if (owner instanceof EntityPlayer && entity != null)
                                                    if (EventUtils.cantAttack((EntityPlayer) owner, entity))
                                                        continue;
                                                //TODO ZeyCodeEnd

                                                if (!returning && entity != null && entity.attackEntityFrom(new MixinDamageSourceBoomerang(this, owner), 4.0F + (float) (4 * this.getEnchantmentLevel(ItemBoomerang.SHARPNESS))) && entity instanceof EntityLivingBase && !(entity instanceof EntityEnderman)) {
                                                    this.motionX = this.motionY = this.motionZ = 0.0;
                                                    this.dataManager.set(DATAWATCHER_OUT_FLAG, (byte) 1);
                                                    returning = true;
                                                    EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
                                                    if (owner instanceof EntityLivingBase) {
                                                        EnchantmentHelper.applyThornEnchantments(entitylivingbase, owner);
                                                        EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) owner, entitylivingbase);
                                                    }

                                                    if (StackHelper.isNonNull(potionStack)) {
                                                        List<PotionEffect> potionEffects = PotionUtils.getEffectsFromStack(potionStack);
                                                        Iterator var44 = potionEffects.iterator();

                                                        while (var44.hasNext()) {
                                                            PotionEffect potionEffect = (PotionEffect) var44.next();
                                                            if (potionEffect.getPotion().isInstant()) {
                                                                potionEffect.getPotion().affectEntity(this, owner, entitylivingbase, potionEffect.getAmplifier(), 0.25);
                                                            } else {
                                                                entitylivingbase.addPotionEffect(new PotionEffect(potionEffect.getPotion(), potionEffect.getDuration() / 8, potionEffect.getAmplifier(), potionEffect.getIsAmbient(), potionEffect.doesShowParticles()));
                                                            }
                                                        }
                                                    }

                                                    int boom = this.getEnchantmentLevel(ItemBoomerang.EXPLODE);
                                                    if (boom > 0) {
                                                        this.world.createExplosion(owner, this.posX, this.posY, this.posZ, (float) boom, false);
                                                    }

                                                    int flame = this.getEnchantmentLevel(ItemBoomerang.FLAMING);
                                                    if (flame > 0) {
                                                        entity.setFire(5);
                                                    }

                                                    if (owner != null && entity != owner && entity instanceof EntityPlayer && owner instanceof EntityPlayerMP) {
                                                        ((EntityPlayerMP) owner).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
                                                    }
                                                }
                                                break label186;
                                            } while (returning);
                                        } while (!e.canBeCollidedWith());
                                    } while (this.isOwner(e));

                                    if (!(e instanceof EntityPlayer)) {
                                        break;
                                    }

                                    entityplayer = (EntityPlayer) e;
                                } while (entityplayer.capabilities.disableDamage || owner instanceof EntityPlayer && !((EntityPlayer) owner).canAttackPlayer(entityplayer));

                                float f1 = 0.3F;
                                AxisAlignedBB axisAlignedBB = e.getEntityBoundingBox().grow((double) f1, (double) f1, (double) f1);
                                mop = axisAlignedBB.calculateIntercept(startVec, endVec);
                            } while (mop == null);

                            d1 = startVec.squareDistanceTo(mop.hitVec);
                        } while (!(d1 < d0) && d0 != -1.0);

                        entity = e;
                        d0 = d1;
                    }
                }

                if (movingobjectposition != null) {
                    this.motionX = this.motionY = this.motionZ = 0.0;
                    this.dataManager.set(DATAWATCHER_OUT_FLAG, (byte) 1);
                    if (!returning && !this.world.isRemote) {
                        int boom = this.getEnchantmentLevel(ItemBoomerang.EXPLODE);
                        if (boom > 0) {
                            Random rand = this.world.rand;
                            this.world.createExplosion(this, this.posX + rand.nextGaussian() * 0.1, this.posY + rand.nextGaussian() * 0.1, this.posZ + rand.nextGaussian() * 0.1, (float) boom, false);
                        }
                    }
                }

            }
        } else {
            this.setMeDead();
        }
    }

    @Mixin(EntityBoomerang.DamageSourceBoomerang.class)
    public class MixinDamageSourceBoomerang extends EntityDamageSourceIndirect {
        public MixinDamageSourceBoomerang(EntityBoomerang indirectEntityIn, Entity owner) {
            super("boomerang", indirectEntityIn, owner);
        }

        public MixinDamageSourceBoomerang(Entity indirectEntityIn, Entity owner) {
            super("boomerang", indirectEntityIn, owner);
        }
    }
}
