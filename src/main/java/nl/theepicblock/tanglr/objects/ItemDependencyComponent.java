package nl.theepicblock.tanglr.objects;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import nl.theepicblock.tanglr.Tanglr;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record ItemDependencyComponent(List<ItemGroup> miniGroups) {
    public static final Codec<ItemDependencyComponent> CODEC = Codec.list(ItemGroup.CODEC).xmap(ItemDependencyComponent::new, ItemDependencyComponent::miniGroups);
    public static final StreamCodec<FriendlyByteBuf, ItemDependencyComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ItemGroup.STREAM_CODEC), ItemDependencyComponent::miniGroups,
            ItemDependencyComponent::new
    );

    public ItemDependencyComponent withRemoved(Set<ItemGroup> groups) {
        if (groups.size() == this.miniGroups().size()) return null;
        var newList = new ArrayList<ItemGroup>();
        for (var group : this.miniGroups) {
            if (!groups.contains(group)) {
                newList.add(group);
            }
        }
        return new ItemDependencyComponent(newList);
    }

    public ItemDependencyComponent withReplaced(ItemGroup from, ItemGroup to) {
        var newList = new ArrayList<ItemGroup>();
        for (var group : this.miniGroups) {
            if (group == from) {
                newList.add(to);
            } else {
                newList.add(group);
            }
        }
        return new ItemDependencyComponent(newList);
    }

    public static void markAllDependent(ItemStack stack, Dependency dep) {
        stack.set(Tanglr.DEPENDENCY_COMPONENT.get(), ofSingle(dep, stack.getCount()));
    }

    public static ItemDependencyComponent of(List<ItemGroup> groups) {
        if (groups.isEmpty()) {
            return null;
        }
        return new ItemDependencyComponent(groups);
    }

    public static ItemDependencyComponent ofSingle(Dependency dep, int count) {
        return new ItemDependencyComponent(List.of(new ItemGroup(List.of(dep), count)));
    }

    public static ItemDependencyComponent merge(@Nullable ItemDependencyComponent a, ItemDependencyComponent b) {
        var list = new ArrayList<ItemGroup>();
        if (a != null) {
            list.addAll(a.miniGroups());
        }
        list.addAll(b.miniGroups());
        return new ItemDependencyComponent(list);
    }

    public ItemGroup popSpecialCandidate() {
        ItemGroup selGroup = null;
        for (var group : this.miniGroups()) {
            if (group.dependencies().size() == 1) {
                selGroup = group;
                break;
            }
        }
        return selGroup;
    }

    public ItemGroup popCandidate() {
        ItemGroup selGroup = null;
        for (var group : this.miniGroups()) {
            selGroup = group;
            break;
        }
        return selGroup;
    }

    public Pair<ItemDependencyComponent, Dependency> popSpecialOne() {
        var group = this.popSpecialCandidate();
        if (group == null) {
            return null;
        }
        if (group.count() == 1) {
            return new Pair<>(this.withRemoved(Set.of(group)), group.dependencies().getFirst());
        } else {
            return new Pair<>(this.withReplaced(group, group.decrement(1)), group.dependencies().getFirst());
        }
    }

    public Pair<ItemDependencyComponent, ItemGroup> popOne() {
        var group = this.popCandidate();
        if (group == null) {
            return null;
        }
        if (group.count() == 1) {
            return new Pair<>(this.withRemoved(Set.of(group)), group);
        } else {
            return new Pair<>(this.withReplaced(group, group.decrement(1)), group);
        }
    }

    public Pair<ItemDependencyComponent, ItemDependencyComponent> split(int n) {
        var left = new ArrayList<ItemGroup>();
        var right = new ArrayList<ItemGroup>();

        for (var group : this.miniGroups()) {
            if (n == 0) {
                right.add(group);
                continue;
            }

            if (n < group.count()) {
                left.add(group.withCount(n));
                right.add(group.decrement(n));
                n = 0;
            } else {
                left.add(group);
                n -= group.count();
            }
        }

        return new Pair<>(of(left), of(right));
    }

    public record ItemGroup(List<Dependency> dependencies, int count) {
        public static final Codec<ItemGroup> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.list(Dependency.CODEC).fieldOf("deps").forGetter(ItemGroup::dependencies),
                Codec.INT.fieldOf("count").forGetter(ItemGroup::count)
        ).apply(builder, ItemGroup::new));
        public static final StreamCodec<FriendlyByteBuf, ItemGroup> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(ArrayList::new, Dependency.STREAM_CODEC), ItemGroup::dependencies,
                ByteBufCodecs.VAR_INT, ItemGroup::count,
                ItemGroup::new
        );

        public ItemGroup withCount(int c) {
            return new ItemGroup(this.dependencies, c);
        }

        public ItemGroup decrement(int c) {
            return new ItemGroup(this.dependencies, this.count - c);
        }
    }

    public record Dependency(long dependency, long generation) {
        public static final Codec<Dependency> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.LONG.fieldOf("dependency").forGetter(Dependency::dependency),
                        Codec.LONG.fieldOf("generation").forGetter(Dependency::generation)
                ).apply(instance, Dependency::new)
        );
        public static final StreamCodec<ByteBuf, Dependency> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, Dependency::dependency,
                ByteBufCodecs.VAR_LONG, Dependency::generation,
                Dependency::new
        );
    }
}
