package nl.theepicblock.tanglr.debugrender;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import nl.theepicblock.tanglr.level.LevelExtension;
import nl.theepicblock.tanglr.objects.PositionInfoHolder;
import org.joml.Vector3d;

import java.util.Objects;

/**
 * This thing is terrible and only works in singleplayer
 */
@OnlyIn(Dist.CLIENT)
public class TanglrDebugRenderer {
    private static final boolean ENABLED = true;

    public static ServerLevel level = null;

    public static boolean isEnabled() {
        return ENABLED && !FMLEnvironment.production;
    }

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ) {
        if (!isEnabled()) return;
        if (level == null) return;

        var ext = (LevelExtension)level;
        var infoHolder = PositionInfoHolder.get(level.getServer());
        ext.tanglr$getInternalInfo().forEach((srcPos, infoId) -> {
            var info = infoHolder.lookup(infoId);

            boolean isWrong = false;
            if (!Objects.equals(srcPos, info.position)) {
                renderFloatingText(poseStack, bufferSource, srcPos, "Wrong position");
                isWrong = true;
            }
            if (level != info.level) {
                renderFloatingText(poseStack, bufferSource, srcPos, "Wrong level");
                isWrong = true;
            }
            var hasBlockDeps = info.dependentBlocks != null && !info.dependentBlocks.isEmpty();
            if (!info.hasDependencies && hasBlockDeps) {
                renderFloatingText(poseStack, bufferSource, srcPos, "Deps listed but marked as no deps");
                isWrong = true;
            }
            renderFilledUnitCube(
                    poseStack,
                    bufferSource,
                    srcPos,
                    isWrong ? 0 : 1,
                    info.hasDependencies ? 0 : 1,
                    hasBlockDeps ? 0 : 1,
                    0.2f
            );

            var infoStartC = FastColor.ARGB32.color(255, 255, 0, 0);
            var infoEndC = FastColor.ARGB32.color(255, 255, 255, 255);
            if (hasBlockDeps) {
                for (var dL : info.dependentBlocks) {
                    var di = infoHolder.lookup(dL);
                    renderLine(poseStack, bufferSource, srcPos, di.position, 0.1f, infoStartC, infoEndC);
                }
            }
        });
        ext.tanglr$getInternalDependency().forEach((srcPos, depId) -> {
            var startC = FastColor.ARGB32.color(255, 0, 0, 255);
            var endC = FastColor.ARGB32.color(255, 255, 255, 255);
            var di = infoHolder.lookup(depId);
            renderLine(poseStack, bufferSource, srcPos, di.position, -0.1f, startC, endC);
        });
    }

    public static void renderFilledUnitCube(PoseStack poseStack, MultiBufferSource bufferSource, BlockPos pos, float red, float green, float blue, float alpha) {
        DebugRenderer.renderFilledBox(poseStack, bufferSource, pos, pos, red, green, blue, alpha);
    }

    public static void renderFloatingText(PoseStack stack, MultiBufferSource.BufferSource bufferSource, BlockPos pos, String text) {
        DebugRenderer.renderFloatingText(stack, bufferSource, text, pos.getX(), pos.getY() + 1, pos.getZ(), -256);
    }

    public static void renderLine(PoseStack stack, MultiBufferSource.BufferSource bufferSource, BlockPos from, BlockPos to, float heightOffset, int startColour, int endColour) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            var x = camera.getPosition().x;
            var y = camera.getPosition().y;
            var z = camera.getPosition().z;
            var buf = bufferSource.getBuffer(RenderType.debugLineStrip(6.0));
            var fromP = new Vector3d(from.getX() - x + 0.5, from.getY() - y + 0.5 + heightOffset, from.getZ() - z + 0.5);
            var toP = new Vector3d(to.getX() - x + 0.5, to.getY() - y + 0.5 + heightOffset, to.getZ() - z + 0.5);

            // Use multiple steps for a nice gradient
            int STEPS = 20;
            for (int i = 0; i <= STEPS; i++) {
                var clerp = step(i, STEPS);
                var p = fromP.lerp(toP, i/(double)STEPS);
                buf.addVertex(stack.last(), (float)p.x, (float)p.y, (float)p.z).setColor(FastColor.ARGB32.lerp((float)clerp, startColour, endColour));
            }
        }
    }

    static double step(double i, int s) {
        return 3*(i/(double)s)*(i/(double)s) - 2*(i/(double)s)*(i/(double)s)*(i/(double)s);
//        return -Math.exp(-(3*i) / (double)s) + 1;
    }
}
