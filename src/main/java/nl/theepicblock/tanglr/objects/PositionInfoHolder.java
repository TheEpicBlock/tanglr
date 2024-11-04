package nl.theepicblock.tanglr.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import nl.theepicblock.tanglr.level.LevelExtension;

import java.util.ArrayList;
import java.util.List;

public class PositionInfoHolder extends SavedData {
    private final List<BlockPositionInfo> infos;

    public PositionInfoHolder(List<BlockPositionInfo> infos) {
        this.infos = infos;
    }

    public BlockPositionInfo lookup(long id) {
        if (id >= infos.size() || id < 0) return null;
        return infos.get((int)id);
    }

    public long getOrCreateInfoId(Level level, BlockPos pos) {
        var ext = (LevelExtension)level;
        var id = ext.tanglr$getInfoId(pos);
        if (id != null) return id;
        long newId = infos.size();
        BlockPositionInfo newInfo = new BlockPositionInfo();
        newInfo.position = pos;
        newInfo.hasDependencies = false;
        newInfo.generation = 0;
        newInfo.level = level.dimension();
        this.infos.add(newInfo);
        ext.tanglr$setInfoId(pos, newId);
        return newId;
    }

    public static PositionInfoHolder load(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        var nbtList = tag.getList("infos", CompoundTag.TAG_COMPOUND);
        var list = new ArrayList<BlockPositionInfo>(nbtList.size());
        nbtList.forEach(item -> {
            try {
                list.add(BlockPositionInfo.fromNbt((CompoundTag)item, levelRegistry));
            } catch (NullPointerException ignored) {
            }
        });
        return new PositionInfoHolder(list);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        var l = new ListTag();
        this.infos.forEach(i -> l.add(i.toNbt()));
        compoundTag.put("infos", l);
        return compoundTag;
    }

    public static SavedData.Factory<PositionInfoHolder> factory() {
        return new SavedData.Factory<>(() -> new PositionInfoHolder(new ArrayList<>()), PositionInfoHolder::load, null);
    }

    public static PositionInfoHolder get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), "tanglr_position_info");
    }
}
