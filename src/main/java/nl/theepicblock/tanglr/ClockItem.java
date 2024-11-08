package nl.theepicblock.tanglr;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import nl.theepicblock.tanglr.level.FutureServerLevel;
import nl.theepicblock.tanglr.level.LevelManager;

public class ClockItem extends Item {
    public ClockItem(Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (level instanceof ServerLevel sLevel) {
            var sPlayer = (ServerPlayer)player;
            if (level instanceof FutureServerLevel) {
                var present = LevelManager.toPresent(sLevel);
                sPlayer.teleportTo(present, sPlayer.getX(), sPlayer.getY(), sPlayer.getZ(), sPlayer.getYRot(), sPlayer.getXRot());
            } else {
                var future = LevelManager.toFuture(sLevel);
                if (future == null) {
                    sPlayer.sendSystemMessage(Component.translatable("tanglr.unsupported_level"));
                    return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
                }
                sPlayer.teleportTo(future, sPlayer.getX(), sPlayer.getY(), sPlayer.getZ(), sPlayer.getYRot(), sPlayer.getXRot());
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
