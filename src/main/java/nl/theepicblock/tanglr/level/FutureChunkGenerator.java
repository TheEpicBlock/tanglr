package nl.theepicblock.tanglr.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FutureChunkGenerator extends ChunkGenerator {
    private final ServerLevel parent;

    public FutureChunkGenerator(ServerLevel parent) {
        super(new FixedBiomeSource(parent.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS)));
//        super(new BiomeSource() {
//            @Override
//            protected MapCodec<? extends BiomeSource> codec() {
//                return null;
//            }
//
//            @Override
//            protected Stream<Holder<Biome>> collectPossibleBiomes() {
//                return Stream.empty();
//            }
//
//            @Override
//            public Holder<Biome> getNoiseBiome(int i, int i1, int i2, Climate.Sampler sampler) {
//                return null;
//            }
//        });
        this.parent = parent;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return null;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {

    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return parent.getHeight();
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        var chunkPos = chunk.getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;

        return parent.getChunkSource().getChunkFuture(chunkX, chunkZ, ChunkStatus.FULL, true).thenApply(pChunkR -> {
            pChunkR.ifSuccess(pChunk -> {
                var tmpPos = new BlockPos.MutableBlockPos();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int x1 = SectionPos.sectionToBlockCoord(chunkX, x);
                        int z1 = SectionPos.sectionToBlockCoord(chunkZ, z);
                        for (int y = pChunk.getMinBuildHeight(); y < pChunk.getMaxBuildHeight(); y++) {
                            tmpPos.set(x1, y, z1);
                            chunk.setBlockState(tmpPos, pChunk.getBlockState(tmpPos), false);
                        }
                    }
                }
            });
            return chunk;
        });
    }

    @Override
    public int getSeaLevel() {
        return parent.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return parent.getMinBuildHeight();
    }

    @Override
    public int getBaseHeight(int i, int i1, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return 0;
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int i, int i1, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {

    }

    public ServerLevel getParentLevel() {
        return this.parent;
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        var chunkPos = chunk.getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;

        var pChunk = parent.getChunkSource().getChunk(chunkX, chunkZ, true);
        if (pChunk == null) return;

        var tmpPos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int x1 = SectionPos.sectionToBlockCoord(chunkX, x);
                int z1 = SectionPos.sectionToBlockCoord(chunkZ, z);
                for (int y = pChunk.getMinBuildHeight(); y < pChunk.getMaxBuildHeight(); y++) {
                    tmpPos.set(x1, y, z1);
                    level.setBlock(tmpPos, pChunk.getBlockState(tmpPos), 2);
                }
            }
        }
    }

    @Override
    public @Nullable Pair<BlockPos,Holder<Structure>> findNearestMapStructure(ServerLevel level, HolderSet<Structure> structure, BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        return null;
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> biome, StructureManager structureManager, MobCategory category, BlockPos pos) {
        return WeightedRandomList.create();
    }

    @Override
    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager structureTemplateManager) {
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunk) {
        return parent.getChunkSource().getChunkFuture(chunk.getPos().x, chunk.getPos().z, ChunkStatus.BIOMES, true).thenApply((pChunkR) -> {
            pChunkR.ifSuccess((pChunk) -> {
                chunk.fillBiomesFromNoise((x,y,z,s) -> pChunk.getNoiseBiome(x,y,z), null);
            });
            return chunk;
        });
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSetLookup, RandomState randomState, long seed) {
        return super.createState(structureSetLookup, randomState, seed);
    }
}
