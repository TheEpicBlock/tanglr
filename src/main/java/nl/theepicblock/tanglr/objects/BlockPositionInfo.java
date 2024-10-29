package nl.theepicblock.tanglr.objects;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class BlockPositionInfo {
    public BlockPos position;
    public Level level;
    public long generation;
    public boolean hasDependencies;
//    public long dependency;
    public LongList dependentBlocks;

    public static BlockPositionInfo fromNbt(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        BlockPositionInfo info = new BlockPositionInfo();
        info.position = NbtUtils.readBlockPos(tag, "position").orElseThrow();
        info.level = levelRegistry.lookup(Registries.DIMENSION).orElseThrow().get(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension")))).orElseThrow().value();
        info.generation = tag.getLong("generation");
        info.hasDependencies = tag.getBoolean("dependencies");
        info.dependentBlocks = new LongArrayList(tag.getLongArray("dependentBlocks"));
        return info;
    }
}
