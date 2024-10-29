package nl.theepicblock.tanglr;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import nl.theepicblock.tanglr.objects.ItemDependencyComponent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

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
    public static final DeferredItem<ClockItem> CLOCK_ITEM = ITEMS.registerItem("clock", ClockItem::new, new Item.Properties());
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
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> CLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // For your own tabs, this method is preferred over the event
                output.accept(CLOCK_ITEM.get());
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
