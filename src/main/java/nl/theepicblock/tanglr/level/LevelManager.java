package nl.theepicblock.tanglr.level;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import nl.theepicblock.tanglr.Tanglr;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the future dimensions of all levels
 */
public class LevelManager {
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
}
