package nl.theepicblock.tanglr.level;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import nl.theepicblock.tanglr.mixin.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

public class FutureServerLevel extends ServerLevel {
    public FutureServerLevel(ServerParameters p, ServerLevel parent, ResourceKey<Level> dimension) {
        super(p.server(), p.executor(), p.storageSource(), (ServerLevelData)parent.getLevelData(), dimension, getStem(parent), p.listener(), false, 0, List.of(), false, new RandomSequences(0));
    }

    private static LevelStem getStem(ServerLevel parent) {
        return new LevelStem(parent.dimensionTypeRegistration(), new FutureChunkGenerator(parent));
    }

    @Override
    public void tick(BooleanSupplier hasTimeLeft) {
        var accessor = (ServerLevelAccessor)this;
        ProfilerFiller profilerfiller = this.getProfiler();

        profilerfiller.push("chunkSource");
        this.getChunkSource().tick(hasTimeLeft, true);
        profilerfiller.popPush("blockEvents");

        boolean flag1 = !this.players().isEmpty() || ForcedChunkManager.hasForcedChunks(this);
        if (flag1) {
            this.resetEmptyTime();
        }

        // TODO
//        if (flag1 || this.emptyTime++ < 300) {
        if (true) {
            profilerfiller.push("entities");

            accessor.getEntityTickList().forEach((p_308566_) -> {
                if (!p_308566_.isRemoved()) {
                    if (accessor.invokeShouldDiscardEntity(p_308566_)) {
                        p_308566_.discard();
                    } else if (!tickRateManager().isEntityFrozen(p_308566_)) {
                        profilerfiller.push("checkDespawn");
                        p_308566_.checkDespawn();
                        profilerfiller.pop();
                        if (this.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(p_308566_.chunkPosition().toLong())) {
                            Entity entity = p_308566_.getVehicle();
                            if (entity != null) {
                                if (!entity.isRemoved() && entity.hasPassenger(p_308566_)) {
                                    return;
                                }

                                p_308566_.stopRiding();
                            }

                            profilerfiller.push("tick");
                            if (!p_308566_.isRemoved() && !(p_308566_ instanceof PartEntity)) {
                                this.guardEntityTick(this::tickNonPassenger, p_308566_);
                            }

                            profilerfiller.pop();
                        }
                    }
                }

            });
            profilerfiller.pop();
            this.tickBlockEntities();
        }

        profilerfiller.push("entityManagement");
        accessor.getEntityManager().tick();
        profilerfiller.pop();
    }
}
