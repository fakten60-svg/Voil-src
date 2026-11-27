package lol.lwes.voil.util.world;

import lol.lwes.voil.VoilClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class InteractionUtil {
    private InteractionUtil() {
    }

    public static boolean useItem(Hand hand) {
        MinecraftClient mc = VoilClient.mc;
        if (mc.player == null || mc.interactionManager == null) {
            return false;
        }

        ActionResult result = mc.interactionManager.interactItem(mc.player, hand);
        if (result.isAccepted()) {
            mc.player.swingHand(hand);
            return true;
        }
        return false;
    }

    public static boolean clickBlock(BlockPos pos, Hand hand, boolean swing) {
        MinecraftClient mc = VoilClient.mc;
        if (pos == null || mc.player == null || mc.world == null || mc.interactionManager == null) {
            return false;
        }

        Vec3d center = pos.toCenterPos();
        Vec3d eyes = mc.player.getEyePos();
        Direction face = Direction.getFacing(eyes.x - center.x, eyes.y - center.y, eyes.z - center.z);
        Vec3d hitPos = center.add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
        BlockHitResult hit = new BlockHitResult(hitPos, face, pos, false);

        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hit);
        if (!result.isAccepted()) {
            return false;
        }

        if (swing) {
            mc.player.swingHand(hand);
        } else if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }
        return true;
    }
}
