package nl.theepicblock.tanglr.level;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;

public class TimeDataStorage extends SavedData {
    public final Object2LongMap<BlockPos> infoIds = new Object2LongOpenHashMap<>();
    public final Object2LongMap<BlockPos> depIds = new Object2LongOpenHashMap<>();

    public static TimeDataStorage load(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        var s = new TimeDataStorage();
        var infos = tag.getList("infoIds", Tag.TAG_COMPOUND);
        for (var i : infos) {
            var pos = NbtUtils.readBlockPos((CompoundTag)i, "pos").orElseThrow();
            var v = ((CompoundTag)i).getLong("v");
            s.infoIds.put(pos, v);
        }
        var deps = tag.getList("infoIds", Tag.TAG_COMPOUND);
        for (var i : deps) {
            var pos = NbtUtils.readBlockPos((CompoundTag)i, "pos").orElseThrow();
            var v = ((CompoundTag)i).getLong("v");
            s.depIds.put(pos, v);
        }
        return s;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        var infos = new ListTag();
        for (var i : infoIds.entrySet()) {
            var t = new CompoundTag();
            t.put("pos", NbtUtils.writeBlockPos(i.getKey()));
            t.putLong("v", i.getValue());
            infos.add(t);
        }
        var deps = new ListTag();
        for (var i : infoIds.entrySet()) {
            var t = new CompoundTag();
            t.put("pos", NbtUtils.writeBlockPos(i.getKey()));
            t.putLong("v", i.getValue());
            deps.add(t);
        }
        compoundTag.put("infoIds", infos);
        compoundTag.put("depIds", deps);
        return compoundTag;
    }

    public static SavedData.Factory<TimeDataStorage> factory() {
        return new SavedData.Factory<>(TimeDataStorage::new, TimeDataStorage::load, null);
    }
}
