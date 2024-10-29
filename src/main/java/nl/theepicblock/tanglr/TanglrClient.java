package nl.theepicblock.tanglr;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import nl.theepicblock.tanglr.debugrender.TanglrDebugRenderer;

@Mod(value = Tanglr.MODID, dist = Dist.CLIENT)
public class TanglrClient {
    public TanglrClient(IEventBus modEventBus) {
        if (TanglrDebugRenderer.isEnabled()) {
            NeoForge.EVENT_BUS.addListener(TanglrClient::setDebugRenderer);
        }
    }

    private static void setDebugRenderer(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity().getServer() == null) return;
        TanglrDebugRenderer.level = e.getEntity().getServer().getLevel(e.getTo());
    }
}
