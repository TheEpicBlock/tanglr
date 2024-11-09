package nl.theepicblock.tanglr;

import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import nl.theepicblock.tanglr.block.DelayedRepeaterBlock;
import nl.theepicblock.tanglr.block.TimeMoverBlock;
import nl.theepicblock.tanglr.objects.ItemDependencyComponent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Tanglr.MODID)
public class Tanglr {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tanglr";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // deferred registries
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // deferred objects
    public static final DeferredItem<ClockItem> CLOCK_ITEM = ITEMS.registerItem("clock", ClockItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredBlock<DelayedRepeaterBlock> DELAYED_REPEATER = BLOCKS.registerBlock("delayed_repeater", DelayedRepeaterBlock::new, BlockBehaviour.Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<TimeMoverBlock> TIME_MOVER = BLOCKS.registerBlock("time_mover", TimeMoverBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(3.0F).requiresCorrectToolForDrops().isRedstoneConductor((a,b,c) -> false));
    public static final DeferredItem<BlockItem> DELAYED_REPEATER_ITEM = ITEMS.registerSimpleBlockItem(DELAYED_REPEATER);
    public static final DeferredItem<BlockItem> TIME_MOVER_ITEM = ITEMS.registerSimpleBlockItem(TIME_MOVER);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemDependencyComponent>> DEPENDENCY_COMPONENT = DATA_COMPONENTS.registerComponentType(
            "basic",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(ItemDependencyComponent.CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(ItemDependencyComponent.STREAM_CODEC)
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tanglr"))
            .icon(() -> CLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // For your own tabs, this method is preferred over the event
                output.accept(CLOCK_ITEM.get());
                output.accept(DELAYED_REPEATER_ITEM.get());
                output.accept(TIME_MOVER_ITEM.get());
            }).build());

    public Tanglr(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Registries
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.register(TimeLogic.class);
        NeoForge.EVENT_BUS.register(ItemEvents.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
    }
}
