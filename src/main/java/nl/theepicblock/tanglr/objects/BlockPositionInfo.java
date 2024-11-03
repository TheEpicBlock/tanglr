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
import org.jetbrains.annotations.Nullable;

public class BlockPositionInfo {
    public BlockPos position;
    public Level level;
    public long generation;
    public boolean hasDependencies;
//    public long dependency;
    public @Nullable LongList dependentBlocks;

    public static BlockPositionInfo fromNbt(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        BlockPositionInfo info = new BlockPositionInfo();
        info.position = NbtUtils.readBlockPos(tag, "position").orElseThrow();
        info.level = levelRegistry.lookup(Registries.DIMENSION).orElseThrow().get(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension")))).orElseThrow().value();
        info.generation = tag.getLong("generation");
        info.hasDependencies = tag.getBoolean("dependencies");
        var arr = tag.getLongArray("dependentBlocks");
        if (arr.length != 0) {
            info.dependentBlocks = new LongArrayList(arr);
        }
        return info;
    }

    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.put("position", NbtUtils.writeBlockPos(this.position));
        tag.putString("dimension", this.level.dimension().location().toString());
        tag.putLong("generation", this.generation);
        tag.putBoolean("dependencies", this.hasDependencies);
        if (this.dependentBlocks == null) {
            tag.putLongArray("dependentBlocks", new long[0]);
        } else {
            tag.putLongArray("dependentBlocks", this.dependentBlocks.toLongArray());
        }
        return tag;
    }
}
