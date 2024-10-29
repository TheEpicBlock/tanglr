package nl.theepicblock.tanglr;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import nl.theepicblock.tanglr.level.FutureServerLevel;
import nl.theepicblock.tanglr.level.LevelManager;
import nl.theepicblock.tanglr.objects.ItemDependencyComponent;
import nl.theepicblock.tanglr.level.LevelExtension;
import nl.theepicblock.tanglr.objects.PositionInfoHolder;
import org.jetbrains.annotations.Nullable;

public class TimeLogic {
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
            // Replicate the change in the future
            var futureLevel = LevelManager.toFuture(level);
            futureLevel.setBlock(location, newState, Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
            // TODO not all changes should be significant enough
            onBlockSignificantChange(level, location);
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent e) {
        var level = e.getLevel();
        var pos = e.getPos();
        var comp = getDependencyForItemDrop(level, pos);
        if (comp != null) {
            e.getDrops().forEach(drop -> drop.getItem().set(Tanglr.DEPENDENCY_COMPONENT.get(), comp));
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent e) {
        var item = e.getEntity().getItem();
        var comp = item.get(Tanglr.DEPENDENCY_COMPONENT.get());
        if (comp != null) {
            var id = comp.dependency();
            if (e.getPlayer().getServer() != null) {
                var infoHolder = PositionInfoHolder.get(e.getPlayer().getServer());
                var info = infoHolder.lookup(id);
                if (info.generation == comp.generation()) {
                    e.getPlayer().sendSystemMessage(Component.literal("This item is still valid"));
                } else {
                    e.getPlayer().sendSystemMessage(Component.literal("This item is not valid"));
                }
            }
        }
    }

    public static @Nullable ItemDependencyComponent getDependencyForItemDrop(ServerLevel level, BlockPos location) {
        if (level instanceof FutureServerLevel futureLevel) {
            var infoHolder = PositionInfoHolder.get(level.getServer());
            var id = ((LevelExtension)futureLevel).tanglr$getDependencyId(location);
            if (id == null) {
                // We presume it depends on the block in the same location in the present
                var present = LevelManager.toPresent(futureLevel);
                id = infoHolder.getOrCreateInfoId(present, location);
            }
            var info = infoHolder.lookup(id);
            info.hasDependencies = true;
            infoHolder.setDirty();
            return new ItemDependencyComponent(id, info.generation);
        } else {
            var ext = (LevelExtension)level;
            var id = ext.tanglr$getDependencyId(location);
            if (id == null) return null;
            var infoHolder = PositionInfoHolder.get(level.getServer());
            var info = infoHolder.lookup(id);
            info.hasDependencies = true;
            infoHolder.setDirty();
            return new ItemDependencyComponent(id, info.generation);
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
                    dependantInfo.level.setBlock(dependantInfo.position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
                }
                positionInfo.dependentBlocks.clear();
            }
        }
    }
}
