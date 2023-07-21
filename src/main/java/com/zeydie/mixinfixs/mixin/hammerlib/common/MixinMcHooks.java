package com.zeydie.mixinfixs.mixin.hammerlib.common;

import codechicken.chunkloader.tile.TileChunkLoaderBase;
import com.zeitheron.hammercore.asm.McHooks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(McHooks.class)
public final class MixinMcHooks {
    @Overwrite(remap = false)
    public static void tickTile(final ITickable tickable) {
        if (tickable instanceof TileEntity) {
            final TileEntity tileEntity = (TileEntity) tickable;
            final World world = tileEntity.getWorld();
            final BlockPos blockPos = tileEntity.getPos();
            final Chunk chunk = world.getChunkFromBlockCoords(blockPos);

            boolean update;

            if (!(tileEntity instanceof TileChunkLoaderBase))
                if (world.isAnyPlayerWithinRangeAt
                        (
                                blockPos.getX(),
                                blockPos.getY(),
                                blockPos.getZ(),
                                32
                        )
                )
                    update = true;
                else
                    update = chunk
                            .getTileEntityMap()
                            .values()
                            .stream()
                            .anyMatch(tileEntityChunk -> tileEntityChunk instanceof TileChunkLoaderBase);
            else update = true;

            if (update)
                tickable.update();
        }
    }
}