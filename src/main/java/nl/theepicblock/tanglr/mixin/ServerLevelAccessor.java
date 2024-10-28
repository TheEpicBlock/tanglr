package nl.theepicblock.tanglr.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerLevel.class)
public interface ServerLevelAccessor {
    @Accessor
    EntityTickList getEntityTickList();

    @Accessor
    PersistentEntitySectionManager<Entity> getEntityManager();

    @Invoker
    boolean invokeShouldDiscardEntity(Entity entity);
}
