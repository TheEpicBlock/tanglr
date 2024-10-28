package nl.theepicblock.tanglr.level;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.List;

public class FutureServerLevel extends ServerLevel {
    public FutureServerLevel(ServerParameters p, ServerLevel parent, ResourceKey<Level> dimension) {
        super(p.server(), p.executor(), p.storageSource(), (ServerLevelData)parent.getLevelData(), dimension, getStem(parent), p.listener(), false, 0, List.of(), false, new RandomSequences(0));
    }

    private static LevelStem getStem(ServerLevel parent) {
        return new LevelStem(parent.dimensionTypeRegistration(), new FutureChunkGenerator(parent));
    }
}
