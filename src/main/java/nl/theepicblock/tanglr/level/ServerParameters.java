package nl.theepicblock.tanglr.level;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.concurrent.Executor;

public record ServerParameters(MinecraftServer server, Executor executor, LevelStorageSource.LevelStorageAccess storageSource, ChunkProgressListener listener) {
}
