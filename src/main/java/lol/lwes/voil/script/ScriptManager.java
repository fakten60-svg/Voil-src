package lol.lwes.voil.script;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.script.parse.ScriptLexer;
import lol.lwes.voil.script.parse.ScriptParser;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.ChatUtil;
import lol.lwes.voil.util.MovementUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class ScriptManager {
    private static final long OPAQUE_ALPHA_MASK = 0xFF000000L;

    private final Map<String, Script> scripts = new HashMap<>();
    public final ScriptInterpreter runtime = new ScriptInterpreter();

    public ScriptManager() {
        this.registerOutputFunctions();
        this.registerPlayerFunctions();
        this.registerCombatFunctions();
    }

    private void registerOutputFunctions() {
        this.runtime.registerNative("print", args -> {
            requireArguments(args, 1, 1, "value");
            ChatUtil.info(args.get(0).toString());
            return ScriptValue.nullValue();
        });
        this.runtime.registerNative("drawText", args -> {
            requireArguments(args, 3, 4, "text, x, y, [color]");
            int color = args.size() == 4 ? parseHexColor(args.get(3).toString()) : Color.WHITE.getRGB();
            VoilClient.fonts.getSelectedFont().render(args.get(0).toString(), args.get(1).asFloat(), args.get(2).asFloat(), color);
            return ScriptValue.nullValue();
        });
        this.runtime.registerNative("drawRectangle", args -> {
            requireArguments(args, 5, 6, "x, y, width, height, radius, [color]");
            int color = args.size() == 6 ? parseHexColor(args.get(5).toString()) : Color.WHITE.getRGB();
            Render2DUtil.drawRoundedRect(
                args.get(0).asFloat(), args.get(1).asFloat(), args.get(2).asFloat(), args.get(3).asFloat(), args.get(4).asFloat(), 1.0F, new Color(color, true)
            );
            return ScriptValue.nullValue();
        });
        this.runtime.registerNative("drawBlur", args -> {
            requireArguments(args, 5, 5, "x, y, width, height, radius");
            Render2DUtil.drawBlurRect(args.get(0).asFloat(), args.get(1).asFloat(), args.get(2).asFloat(), args.get(3).asFloat(), args.get(4).asFloat(), 14.0F);
            return ScriptValue.nullValue();
        });
    }

    private void registerPlayerFunctions() {
        this.registerGetter("getHealth", ClientPlayerEntity::getHealth);
        this.registerGetter("isMoving", player -> MovementUtil.hasMovementInput());
        this.registerGetter("getSpeed", player -> MovementUtil.getHorizontalSpeed());
        this.registerGetter("isSneaking", ClientPlayerEntity::isSneaking);
        this.registerGetter("isSprinting", ClientPlayerEntity::isSprinting);
        this.registerGetter("isOnGround", ClientPlayerEntity::isOnGround);
        this.registerGetter("getMotionX", player -> player.getVelocity().x);
        this.registerGetter("getMotionY", player -> player.getVelocity().y);
        this.registerGetter("getMotionZ", player -> player.getVelocity().z);
        this.registerGetter("getYaw", ClientPlayerEntity::getYaw);
        this.registerGetter("getPitch", ClientPlayerEntity::getPitch);
        this.registerGetter("getX", ClientPlayerEntity::getX);
        this.registerGetter("getY", ClientPlayerEntity::getY);
        this.registerGetter("getZ", ClientPlayerEntity::getZ);

        this.registerAction("jump", (player, args) -> player.jump());
        this.registerAction("forward", (player, args) -> {
            requireArguments(args, 1, 1, "speed");
            double[] offset = MovementUtil.getMoveOffset(args.get(0).asDouble());
            player.setVelocity(offset[0], player.getVelocity().y, offset[1]);
        });
        this.registerAction("strafe", (player, args) -> {
            requireArguments(args, 1, 1, "speed");
            MovementUtil.strafe(args.get(0).asFloat());
        });
        this.registerAction("setPitch", (player, args) -> {
            requireArguments(args, 1, 1, "pitch");
            player.setPitch(args.get(0).asFloat());
        });
        this.registerAction("setYaw", (player, args) -> {
            requireArguments(args, 1, 1, "yaw");
            player.setYaw(args.get(0).asFloat());
        });
        this.registerAction("setVelocity", (player, args) -> {
            requireArguments(args, 3, 3, "x, y, z");
            player.setVelocity(new Vec3d(args.get(0).asDouble(), args.get(1).asDouble(), args.get(2).asDouble()));
        });
        this.registerAction("swingHand", (player, args) -> {
            requireArguments(args, 1, 1, "hand (main/off)");
            String hand = args.get(0).toString().toLowerCase();
            if (hand.equals("main")) {
                player.swingHand(Hand.MAIN_HAND);
            } else if (hand.equals("off")) {
                player.swingHand(Hand.OFF_HAND);
            } else {
                throw new IllegalArgumentException("argument must be 'main' or 'off'");
            }
        });
        this.registerAction("setRotations", (player, args) -> {
            requireArguments(args, 1, 1, "[yaw, pitch]");
            if (!(args.get(0).getRaw() instanceof List<?> rotation) || rotation.size() != 2) {
                throw new IllegalArgumentException("requires a list of size 2: [yaw, pitch]");
            }

            if (!(rotation.get(0) instanceof Number yaw) || !(rotation.get(1) instanceof Number pitch)) {
                throw new IllegalArgumentException("list must contain numbers");
            }

            player.setYaw(yaw.floatValue());
            player.setPitch(pitch.floatValue());
        });
    }

    private void registerCombatFunctions() {
        this.runtime.registerNative("attack", args -> {
            if (!this.isInWorld()) {
                return ScriptValue.nullValue();
            }

            requireArguments(args, 0, 2, "[target], [cooldown]");
            Entity target = VoilClient.mc.targetedEntity;
            float cooldown = 1.0F;
            if (args.size() >= 1) {
                if (!(args.get(0).getRaw() instanceof Entity entity)) {
                    throw new IllegalArgumentException("expected a target entity as first argument");
                }

                target = entity;
            }

            if (args.size() == 2) {
                if (!(args.get(1).getRaw() instanceof Number number)) {
                    throw new IllegalArgumentException("cooldown must be a number");
                }

                cooldown = number.floatValue();
            }

            if (target != null && VoilClient.mc.player.getAttackCooldownProgress(0.0F) >= cooldown) {
                VoilClient.mc.interactionManager.attackEntity(VoilClient.mc.player, target);
            }

            return ScriptValue.nullValue();
        });
        this.runtime.registerNative("getClosestPlayer", args -> {
            requireArguments(args, 0, 0, "no arguments");
            if (!this.isInWorld()) {
                return ScriptValue.nullValue();
            }

            PlayerEntity closest = null;
            double closestDistance = Double.MAX_VALUE;

            for (PlayerEntity player : VoilClient.mc.world.getPlayers()) {
                if (player != VoilClient.mc.player) {
                    double distance = VoilClient.mc.player.squaredDistanceTo(player);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closest = player;
                    }
                }
            }

            return ScriptValue.of(closest);
        });
        this.runtime.registerNative("getRotationsTo", args -> {
            requireArguments(args, 1, 1, "target entity");
            if (!this.isInWorld()) {
                return ScriptValue.nullValue();
            }

            if (!(args.get(0).getRaw() instanceof Entity entity)) {
                throw new IllegalArgumentException("expected a target entity");
            }

            float[] rotations = AimMathUtil.getRotationsToEntity(entity);
            return ScriptValue.of(List.of(rotations[0], rotations[1]));
        });
    }

    private void registerGetter(String name, Function<ClientPlayerEntity, Object> getter) {
        this.runtime.registerNative(name, args -> this.isInWorld() ? ScriptValue.of(getter.apply(VoilClient.mc.player)) : ScriptValue.nullValue());
    }

    private void registerAction(String name, BiConsumer<ClientPlayerEntity, ScriptArgs> action) {
        this.runtime.registerNative(name, args -> {
            if (this.isInWorld()) {
                action.accept(VoilClient.mc.player, args);
            }

            return ScriptValue.nullValue();
        });
    }

    private static void requireArguments(ScriptArgs args, int min, int max, String usage) {
        if (args.size() < min || args.size() > max) {
            String count = min == max ? String.valueOf(min) : min + " to " + max;
            throw new IllegalArgumentException("expects " + count + " argument(s): " + usage);
        }
    }

    private boolean isInWorld() {
        return VoilClient.mc.player != null && VoilClient.mc.world != null;
    }

    private List<ScriptStatement> parse(String source) {
        return new ScriptParser(new ScriptLexer(source).tokenize()).parse();
    }

    private ScriptInterpreter createRuntime(Script script) {
        ScriptInterpreter interpreter = new ScriptModuleInterpreter(script);
        interpreter.copyFrom(this.runtime);
        return interpreter;
    }

    public Script loadScript(String name, String source) {
        Script script = new Script(name, this.parse(source));
        script.runtime = this.createRuntime(script);
        script.runtime.run(script.sources);
        return script;
    }

    public void reloadScript(String name, String source) {
        Script script = this.scripts.get(name);
        if (script != null) {
            script.sources.clear();
            script.sources.addAll(this.parse(source));
            script.runtime = this.createRuntime(script);
            script.runtime.run(script.sources);
        }
    }

    public void addScript(String name, String source) {
        this.scripts.put(name, this.loadScript(name, source));
    }

    public boolean isScriptEnabled(String name) {
        Script script = this.scripts.get(name);
        return script != null && script.enabled;
    }

    public void enableScript(String name) {
        Script script = this.scripts.get(name);
        if (script != null) {
            script.enabled = true;
        }
    }

    public void disableScript(String name) {
        Script script = this.scripts.get(name);
        if (script != null) {
            script.enabled = false;
        }
    }

    public void disableScripts() {
        this.scripts.values().forEach(script -> script.enabled = false);
    }

    public void runScript(String name) {
        Script script = this.scripts.get(name);
        if (script != null && script.enabled && script.runtime != null) {
            script.runtime.run(script.sources);
        }
    }

    public void dispatchEvent(String event) {
        for (Script script : this.scripts.values()) {
            if (script.enabled && script.runtime != null) {
                script.runtime.runEventHandlers(event);
            }
        }
    }

    public void reportScriptError(String message) {
        ChatUtil.info("[Script Error] " + message);
        this.disableScripts();
    }

    public ScriptInterpreter getRuntime() {
        return this.runtime;
    }

    public Map<String, Script> getScripts() {
        return this.scripts;
    }

    public static int parseHexColor(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (hex.length() != 6 && hex.length() != 8) {
            throw new IllegalArgumentException("Hex color must be 6 or 8 characters long");
        }

        long value = Long.parseLong(hex, 16);
        return hex.length() == 6 ? (int)(OPAQUE_ALPHA_MASK | value) : (int)value;
    }
}
