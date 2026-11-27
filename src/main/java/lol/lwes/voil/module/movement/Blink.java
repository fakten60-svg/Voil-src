package lol.lwes.voil.module.movement;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketBlinkEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.entity.FakePlayerEntity;
import lol.lwes.voil.util.render.Render3DUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Blink extends Module {
    private final BooleanSetting renderPath;
    private final BooleanSetting useDistanceCheck;
    private final NumberSetting distance;
    private final BooleanSetting useTimeCheck;
    private final NumberSetting milliseconds;
    private final ColorSetting lineColor;
    private FakePlayerEntity fakePlayer;
    private final List<Vec3d> positions;
    private long startTime;

    public Blink() {
        super("Blink", "Holds packets and lets you blink", Category.Movement);
        this.renderPath = new BooleanSetting("Render Path", true, "Renders the path of the blink");
        this.useDistanceCheck = new BooleanSetting("Use Distance Check", true, "Disables blink if the player moves more than X blocks");
        this.distance = new NumberSetting("Distance", 3.0, 25.0, 10.0, 1.0, "Distance to check if the player moves more than X blocks");
        this.useTimeCheck = new BooleanSetting("Use Time Check", false, "Disables blink after Y milliseconds");
        this.milliseconds = new NumberSetting("Milliseconds", 0.0, 30000.0, 900.0, 1.0, "The Y milliseconds after which to disable blinks");
        String s2 = "Color of the line";
        Color color = Color.RED;
        this.lineColor = new ColorSetting("Line Color", color, s2);
        this.positions = new CopyOnWriteArrayList<>();
        Setting[] asetting = new Setting[]{this.renderPath, this.useDistanceCheck, this.distance, this.useTimeCheck, this.milliseconds, this.lineColor};
        this.addSettings(asetting);
        NumberSetting numbersetting = this.distance;
        BooleanSupplier booleansupplier = this.useDistanceCheck::isEnabled;
        numbersetting.setVisibleWhen(booleansupplier);
        NumberSetting numbersetting1 = this.milliseconds;
        BooleanSupplier booleansupplier1 = this.useTimeCheck::isEnabled;
        numbersetting1.setVisibleWhen(booleansupplier1);
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            this.positions.add(mc.player.getEntityPos());
            if (this.useDistanceCheck.isEnabled() && this.positions.size() > 1) {
                Vec3d vec3d = mc.player.getEntityPos();
                if (vec3d.distanceTo((Vec3d)this.positions.get(0)) > this.distance.getValue()) {
                    this.toggle();
                }
            }
        }
    }

    @Override
    public String getSuffix() {
        long j = System.currentTimeMillis() - this.startTime;
        return j + "ms";
    }

    @Override
    public void onDisable() {
        if (this.fakePlayer != null) {
            this.fakePlayer.despawn();
            this.fakePlayer = null;
        }
    }

    @Override
    public void onEnable() {
        ClientPlayerEntity clientplayerentity = mc.player;
        this.fakePlayer = new FakePlayerEntity(clientplayerentity, "Voil", 20.0F, false);
        this.fakePlayer.spawn();
        this.positions.clear();
        this.startTime = System.currentTimeMillis();
    }

    @EventHandler
    private void onPacketSend(PacketBlinkEvent packetblinkevent) {
        packetblinkevent.held = true;
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.useTimeCheck.isEnabled() && this.startTime + this.milliseconds.getValue() < System.currentTimeMillis()) {
            this.toggle();
        }

        if (this.renderPath.isEnabled() && !this.positions.isEmpty()) {
            Vec3d vec3d = (Vec3d)this.positions.get(0);

            for (Vec3d vec3d1 : this.positions) {
                Color color = this.lineColor.getColor();
                Render3DUtil.drawLine(vec3d, vec3d1, color);
                vec3d = vec3d1;
            }
        }
    }
}
