package nl.theepicblock.tanglr;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import nl.theepicblock.tanglr.level.FutureServerLevel;
import nl.theepicblock.tanglr.level.LevelExtension;
import nl.theepicblock.tanglr.level.LevelManager;
import nl.theepicblock.tanglr.objects.ItemDependencyComponent;
import nl.theepicblock.tanglr.objects.PositionInfoHolder;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class TimeLogic {
    public static long NOT_DEPENDENT = -1L;
    /**
     * @param level The level where the block change occurred
     * @param location the block position that was changed
     * @param newState the new state of the block
     */
    public static void enqueueBlockChange(ServerLevel level, BlockPos location, BlockState newState) {
        if (level instanceof FutureServerLevel futureLevel) {
            // Changing blocks in the future does not do anything
            // There cannot be a dependency on an object in the future dimension,
            // since by definition, the future refers to a location far ahead of anything else.
            // There cannot be a dependency on anything in there, since the dependent would have to be further in
            // the future than the future.
            return;
        } else {
            var futureLevel = LevelManager.toFuture(level);
            var futureExt = (LevelExtension)futureLevel;
            var depId = futureExt.tanglr$getDependencyId(location);
            if (depId == null) {
                // This position implicitly depends on the block that was just changed,
                // so we'll replicate the change
                futureLevel.setBlock(location, getFutureState(newState), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
            } else if (depId == NOT_DEPENDENT) {
                // Kinda a hack, but we'll ignore not_dependents if the other side is air
                if (futureLevel.getBlockState(location).isAir()) {
                    futureLevel.setBlock(location, getFutureState(newState), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
                    futureExt.tanglr$setDependencyId(location, null);
                }
            }
            // TODO not all changes should be significant enough
            onBlockSignificantChange(level, location);
        }
        unDepend(level, location);
    }

    public static BlockState getFutureState(BlockState in) {
        if (in.is(Blocks.REDSTONE_BLOCK) || in.is(Blocks.REPEATER) || in.is(Blocks.COMPARATOR) || in.is(BlockTags.BUTTONS)) {
            return in.trySetValue(BlockStateProperties.POWERED, false);
        }
        in = in.trySetValue(BlockStateProperties.AGE_1, 1);
        in = in.trySetValue(BlockStateProperties.AGE_2, 2);
        in = in.trySetValue(BlockStateProperties.AGE_3, 3);
        in = in.trySetValue(BlockStateProperties.AGE_4, 4);
        in = in.trySetValue(BlockStateProperties.AGE_5, 5);
        in = in.trySetValue(BlockStateProperties.AGE_7, 7);
        in = in.trySetValue(BlockStateProperties.AGE_15, 15);
        in = in.trySetValue(BlockStateProperties.AGE_25, 25);
        return in;
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent e) {
        var level = e.getLevel();
        var pos = e.getPos();
        var dep = getDependencyForItemDrop(level, pos);
        if (dep != null) {
            e.getDrops().forEach(drop -> ItemDependencyComponent.markAllDependent(drop.getItem(), dep));
        }
    }

    public static boolean decreaseOrTrueIfImplode(ItemStack stack, @Nullable MinecraftServer server) {
        if (server == null) return true;
        var comp = stack.get(Tanglr.DEPENDENCY_COMPONENT.get());
        if (comp == null) return true;
        var toRemove = new HashSet<ItemDependencyComponent.ItemGroup>();
        for (var group : comp.miniGroups()) {
            if (!isValid(group, server)) {
                toRemove.add(group);
            }
        }
        var newComp = comp.withRemoved(toRemove);
        stack.set(Tanglr.DEPENDENCY_COMPONENT.get(), newComp);
        var c = 0;
        for (var group : toRemove) {
            c += group.count();
        }
        stack.setCount(stack.getCount() - c);
        if (stack.isEmpty()) {
            return false;
        }
        return true;
    }

    private static boolean isValid(ItemDependencyComponent.ItemGroup group, MinecraftServer server) {
        for (var dep : group.dependencies()) {
            if (!isValid(dep, server)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValid(ItemDependencyComponent.Dependency dep, MinecraftServer server) {
        var infoHolder = PositionInfoHolder.get(server);
        var info = infoHolder.lookup(dep.dependency());
        return info != null && info.generation == dep.generation();
    }

    public static @Nullable ItemDependencyComponent.Dependency getDependencyForItemDrop(ServerLevel level, BlockPos location) {
        if (level instanceof FutureServerLevel futureLevel) {
            var infoHolder = PositionInfoHolder.get(level.getServer());
            var id = ((LevelExtension)futureLevel).tanglr$getDependencyId(location);
            if (id == null) {
                // We presume it depends on the block in the same location in the present
                var present = LevelManager.toPresent(futureLevel);
                id = infoHolder.getOrCreateInfoId(present, location);
            }
            if (id == NOT_DEPENDENT) {
                return null;
            }
            var info = infoHolder.lookup(id);
            info.hasDependencies = true;
            infoHolder.setDirty();
            return new ItemDependencyComponent.Dependency(id, info.generation);
        } else {
            var ext = (LevelExtension)level;
            var id = ext.tanglr$getDependencyId(location);
            if (id == null) return null;
            var infoHolder = PositionInfoHolder.get(level.getServer());
            var info = infoHolder.lookup(id);
            info.hasDependencies = true;
            infoHolder.setDirty();
            return new ItemDependencyComponent.Dependency(id, info.generation);
        }
    }

    /**
     * Called when a block is changed so significantly that it breaks the timeline
     */
    public static void onBlockSignificantChange(ServerLevel level, BlockPos location) {
        var levelExt = (LevelExtension)level;
        var positionInfoId = levelExt.tanglr$getInfoId(location);
        if (positionInfoId == null) return;
        var infoHolder = PositionInfoHolder.get(level.getServer());
        var positionInfo = infoHolder.lookup(positionInfoId);
        if (positionInfo.hasDependencies) {
            infoHolder.setDirty();
            positionInfo.generation++;
            if (positionInfo.dependentBlocks != null) {
                for (long dependant : positionInfo.dependentBlocks) {
                    var dependantInfo = infoHolder.lookup(dependant);
                    dependantInfo.getLevel(level).setBlock(dependantInfo.position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
                }
                positionInfo.dependentBlocks.clear();
            }
            positionInfo.hasDependencies = false;
        }
    }

    /**
     * Make a block pos depend on something
     */
    public static void setDependency(long dependency, Level level, BlockPos dependantPos) {
        if (level.isClientSide()) return;
        var levelExt = (LevelExtension)level;
        levelExt.tanglr$setDependencyId(dependantPos, dependency);
        var infoHolder = PositionInfoHolder.get(level.getServer());
        var dependencyInfo = infoHolder.lookup(dependency);
        dependencyInfo.hasDependencies = true;
        if (dependencyInfo.dependentBlocks == null) dependencyInfo.dependentBlocks = new LongArrayList();
        dependencyInfo.dependentBlocks.add(infoHolder.getOrCreateInfoId(level, dependantPos));
    }

    public static void unDepend(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        var levelExt = (LevelExtension)level;
        var depId = levelExt.tanglr$getDependencyId(pos);
        if (depId == null) return;
        var selfId = levelExt.tanglr$getInfoId(pos);
        var infoHolder = PositionInfoHolder.get(level.getServer());
        levelExt.tanglr$setDependencyId(pos, null);
        var depList = infoHolder.lookup(depId).dependentBlocks;
        if (depList != null) {
            // Shouldn't be null
            depList.removeLong(depList.indexOf((long)selfId));
        }
    }
}
