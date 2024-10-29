package nl.theepicblock.tanglr.objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ItemDependencyComponent(long dependency, long generation) {
    public static final Codec<ItemDependencyComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("dependency").forGetter(ItemDependencyComponent::dependency),
                    Codec.LONG.fieldOf("generation").forGetter(ItemDependencyComponent::generation)
            ).apply(instance, ItemDependencyComponent::new)
    );
    public static final StreamCodec<ByteBuf, ItemDependencyComponent> STREAM_CODEC = new StreamCodec<>() {
        public ItemDependencyComponent decode(ByteBuf buf) {
            return new ItemDependencyComponent(-1, -1);
        }

        public void encode(ByteBuf buf, ItemDependencyComponent comp) {
        }
    };
}
