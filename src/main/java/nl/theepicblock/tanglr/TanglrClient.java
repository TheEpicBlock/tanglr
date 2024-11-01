package nl.theepicblock.tanglr;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import nl.theepicblock.tanglr.debugrender.TanglrDebugRenderer;

import java.util.Objects;

@Mod(value = Tanglr.MODID, dist = Dist.CLIENT)
public class TanglrClient {
    public TanglrClient(IEventBus modEventBus) {
        if (TanglrDebugRenderer.isEnabled()) {
            NeoForge.EVENT_BUS.addListener(TanglrClient::setDebugRenderer);
        }
        modEventBus.addListener(TanglrClient::onClientSetup);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    Tanglr.CLOCK_ITEM.get(),
                    ResourceLocation.fromNamespaceAndPath(Tanglr.MODID, "is_future"),
                    (stack, level, player, seed) -> {
                        if (level == null) {
                            return 0.0f;
                        }
                        if (Objects.equals(level.dimension().location().getNamespace(), Tanglr.MODID)) {
                            return 1.0f;
                        } else {
                            return 0.0f;
                        }
                    }
            );
        });
    }

    private static void setDebugRenderer(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity().getServer() == null) return;
        TanglrDebugRenderer.level = e.getEntity().getServer().getLevel(e.getTo());
    }
}
