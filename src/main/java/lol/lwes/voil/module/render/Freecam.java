package lol.lwes.voil.module.render;

import lol.lwes.voil.event.CameraPosEvent;
import lol.lwes.voil.event.CameraRotationEvent;
import lol.lwes.voil.event.CameraUpdateEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.event.MouseUpdatePostEvent;
import lol.lwes.voil.event.MouseUpdatePreEvent;
import lol.lwes.voil.event.MovementInput;
import lol.lwes.voil.event.PlayerInputState;
import lol.lwes.voil.mixin.accessor.KeyBindingAccessor;
import lol.lwes.voil.mixin.accessor.KeyboardInputAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleTag;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Freecam extends Module {
    NumberSetting speed = new NumberSetting("Speed", 0.1, 5.0, 1.0, 0.1, "Speed of freecam");
    BooleanSetting disableOnDamage = new BooleanSetting("DisableOnDamage", false, "Disable freecam if you get damaged");
    BooleanSetting retainMovement = new BooleanSetting("Retain Movement", false, "Your player keeps on moving in the way before freecam");
    public Vec3d camPos;
    public Vec3d prevCamPos;
    public MovementInput savedInput;
    public float cameraYaw;
    public float cameraPitch;
    public float playerYaw;
    public float playerPitch;
    public Perspective previousPerspective;
    public boolean previousBobView;
    private static MovementInput neutralInput = new MovementInput();

    public Freecam() {
        super("Freecam", "Lets your camera move freely", Category.Render);
        Setting[] asetting = new Setting[]{this.speed, this.disableOnDamage, this.retainMovement};
        this.addSettings(asetting);
        this.camPos = Vec3d.ZERO;
        this.prevCamPos = Vec3d.ZERO;
        ModuleTag[] amoduletag = new ModuleTag[]{ModuleTag.NOTOBSPROOF};
        this.addTags(amoduletag);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen == null && mc.player != null) {
            if (this.isEnabled()) {
                Vec3d vec3d = new Vec3d(
                    -MathHelper.sin(-this.playerYaw * (float) (Math.PI / 180.0) - (float) Math.PI),
                    0.0,
                    -MathHelper.cos(-this.playerYaw * (float) (Math.PI / 180.0) - (float) Math.PI)
                );
                Vec3d vec3d1 = new Vec3d(0.0, 1.0, 0.0);
                Vec3d vec3d2 = vec3d1.crossProduct(vec3d);
                Vec3d vec3d3 = vec3d.crossProduct(vec3d1);
                Vec3d vec3d4 = Vec3d.ZERO;
                KeyBinding keybinding = mc.options.forwardKey;
                if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor)keybinding).getBoundKey().getCode()) == 1) {
                    vec3d4 = vec3d4.add(vec3d);
                }

                KeyBinding keybinding1 = mc.options.backKey;
                if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor)keybinding1).getBoundKey().getCode()) == 1) {
                    vec3d4 = vec3d4.subtract(vec3d);
                }

                KeyBinding keybinding2 = mc.options.leftKey;
                if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor)keybinding2).getBoundKey().getCode()) == 1) {
                    vec3d4 = vec3d4.add(vec3d2);
                }

                KeyBinding keybinding3 = mc.options.rightKey;
                if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor)keybinding3).getBoundKey().getCode()) == 1) {
                    vec3d4 = vec3d4.add(vec3d3);
                }

                KeyBinding keybinding4 = mc.options.jumpKey;
                if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor)keybinding4).getBoundKey().getCode()) == 1) {
                    vec3d4 = vec3d4.add(0.0, this.speed.getFloat(), 0.0);
                }

                KeyBinding keybinding5 = mc.options.sneakKey;
                if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor)keybinding5).getBoundKey().getCode()) == 1) {
                    vec3d4 = vec3d4.add(0.0, -this.speed.getFloat(), 0.0);
                }

                KeyBinding keybinding6 = mc.options.sprintKey;
                vec3d4 = vec3d4.normalize()
                    .multiply(this.speed.getFloat() * (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor)keybinding6).getBoundKey().getCode()) == 1 ? 2 : 1));
                this.camPos = this.prevCamPos;
                this.prevCamPos = this.prevCamPos.add(vec3d4);
                if (this.disableOnDamage.isEnabled() && mc.player.hurtTime > 0) {
                    this.toggle();
                }
            }
        }
    }

    @Override
    public void onDisable() {
        mc.options.getBobView().setValue(this.previousBobView);
        mc.options.setPerspective(this.previousPerspective);
    }

    @Override
    public void onEnable() {
        Vec3d vec3d = mc.player.getEyePos();
        Freecam freecam1 = this;
        freecam1.prevCamPos = vec3d;
        this.camPos = vec3d;
        this.playerYaw = mc.player.getYaw();
        this.playerPitch = mc.player.getPitch();
        GameOptions gameoptions = mc.options;
        this.previousBobView = gameoptions.getBobView().getValue();
        gameoptions.getBobView().setValue(false);
        this.previousPerspective = gameoptions.getPerspective();
        gameoptions.setPerspective(Perspective.THIRD_PERSON_BACK);
        MovementInput movementinput = new MovementInput();
        movementinput.playerInput = new PlayerInputState();
        movementinput.playerInput.forward = gameoptions.forwardKey.isPressed();
        movementinput.playerInput.backward = gameoptions.backKey.isPressed();
        movementinput.playerInput.left = gameoptions.leftKey.isPressed();
        movementinput.playerInput.right = gameoptions.rightKey.isPressed();
        movementinput.playerInput.jump = gameoptions.jumpKey.isPressed();
        movementinput.playerInput.sneak = gameoptions.sneakKey.isPressed();
        movementinput.forward = KeyboardInputAccessor.getMovementMultiplier(movementinput.playerInput.forward, movementinput.playerInput.backward);
        movementinput.sideways = KeyboardInputAccessor.getMovementMultiplier(movementinput.playerInput.left, movementinput.playerInput.right);
        this.savedInput = movementinput;
    }

    @Override
    public void setEnabled(boolean flag) {
        if (!flag || mc.player != null) {
            super.setEnabled(flag);
        }
    }

    @EventHandler
    public void onInput(InputEvent inputevent) {
        inputevent.input = this.retainMovement.isEnabled() ? this.savedInput : neutralInput;
    }

    @EventHandler
    public void onMouse(MouseUpdatePreEvent mouseupdatepreevent) {
        this.playerYaw = mc.player.getYaw();
        this.playerPitch = mc.player.getPitch();
        mc.player.setYaw(this.cameraYaw);
        mc.player.setPitch(this.cameraPitch);
    }

    @EventHandler
    public void onMouse(MouseUpdatePostEvent mouseupdatepostevent) {
        this.cameraYaw = mc.player.getYaw();
        this.cameraPitch = mc.player.getPitch();
        mc.player.setYaw(this.playerYaw);
        mc.player.setPitch(this.playerPitch);
    }

    @EventHandler
    public void camPosEvent(CameraPosEvent cameraposevent) {
        float f = SystemUtil.getTickDelta();
        cameraposevent.setX(MathHelper.lerp(f, this.camPos.x, this.prevCamPos.x));
        cameraposevent.setY(MathHelper.lerp(f, this.camPos.y, this.prevCamPos.y));
        cameraposevent.setZ(MathHelper.lerp(f, this.camPos.z, this.prevCamPos.z));
        cameraposevent.cancel();
    }

    @EventHandler
    public void camRotEvent(CameraRotationEvent camerarotationevent) {
        camerarotationevent.yaw = this.playerYaw;
        camerarotationevent.pitch = this.playerPitch;
    }

    @EventHandler
    public void camMoveByEvent(CameraUpdateEvent cameraupdateevent) {
        cameraupdateevent.cancel();
    }
}
