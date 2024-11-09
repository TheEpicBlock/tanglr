package nl.theepicblock.tanglr.level;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import nl.theepicblock.tanglr.Tanglr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Manages the future dimensions of all levels
 */
public class LevelManager {
    private static final WeakHashMap<ServerLevel,WeakReference<ServerLevel>> toFutureCache = new WeakHashMap<>();

    public static Map<ResourceKey<Level>,ServerLevel> createFutureLevels(Map<ResourceKey<Level>,ServerLevel> levels, ServerParameters p) {
        var futureLevels = new HashMap<ResourceKey<Level>,ServerLevel>();

        for (var entry : levels.entrySet()) {
            var futureName = toFuture(entry.getKey());
            var dimensionKey = ResourceKey.create(Registries.DIMENSION, futureName.location());

            var futureLevel = new FutureServerLevel(p, entry.getValue(), dimensionKey);
            futureLevels.put(futureName, futureLevel);
        }

        return futureLevels;
    }

    public static ResourceKey<Level> toFuture(ResourceKey<Level> present) {
        var presentName = present.location();
        var futureName = ResourceLocation.fromNamespaceAndPath(Tanglr.MODID,
                presentName.getNamespace().replace("_", "__")+"_"+presentName.getPath());
        return ResourceKey.create(present.registryKey(), futureName);
    }

    @Nullable
    public static ServerLevel toFuture(ServerLevel present) {
        var ref = toFutureCache.computeIfAbsent(present, p -> {
            var futureKey = toFuture(p.dimension());
            var futureLevel = p.getServer().getLevel(futureKey);
            if (futureLevel == null) {
                return null;
            } else {
                return new WeakReference<>(futureLevel);
            }
        });
        if (ref == null) return null;
        var future = ref.get();
        if (future == null) {
            toFutureCache.remove(present);
        }
        return future;
    }

    public static ResourceKey<Level> toPresent(ResourceKey<Level> future) {
        var futureName = future.location().getPath();
        var split = futureName.split("(?<!_)_(?!_)", 2);
        var presentName = ResourceLocation.fromNamespaceAndPath(split[0].replace("__", "_"), split[1]);
        return ResourceKey.create(future.registryKey(), presentName);
    }

    @NotNull
    public static ServerLevel toPresent(ServerLevel future) {
        var presentKey = toPresent(future.dimension());
        // A future level must be created only if there is a corresponding present
        var presentLevel = future.getServer().getLevel(presentKey);
        if (presentLevel == null) {
            throw new NullPointerException("Couldn't find level named "+presentKey);
        }
        return presentLevel;
    }
}
