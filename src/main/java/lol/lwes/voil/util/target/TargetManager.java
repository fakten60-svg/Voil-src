package lol.lwes.voil.util.target;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Targets;
import lol.lwes.voil.setting.ListSetting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.rotation.Rotation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class TargetManager {
    private static List<Entity> allTargets = List.of();
    private static List<Entity> filteredTargets = List.of();
    private static final int DEFAULT_ARMOR_VALUE = Integer.MAX_VALUE;

    public static boolean isTarget(Entity entity, boolean flag) {
        return entity != null && (flag ? allTargets : filteredTargets).contains(entity);
    }

    private static Comparator<Entity> createSortComparator(String s, ListSetting listsetting, ClientPlayerEntity clientplayerentity, Vec3d vec3d, Rotation rotation) {
        Comparator<Entity> comparator;
        switch (s) {
            case "Distance" -> {
                if (listsetting.isEnabled(s)) {
                    double d0 = listsetting.getMinThreshold(s);
                    comparator = Comparator.comparingInt(entity -> clientplayerentity.distanceTo(entity) <= d0 ? 0 : 1);
                } else {
                    comparator = Comparator.comparingDouble(clientplayerentity::distanceTo);
                }
            }
            case "Health" -> comparator = Comparator.comparingDouble(TargetManager::getHealthScore);
            case "Armor" -> comparator = Comparator.comparingInt(TargetManager::getArmorValue);
            case "Angle" -> comparator = Comparator.comparingDouble(entity -> {
                Vec3d vec3d2 = AimMathUtil.closestPointOnBoxFromCamera(entity.getBoundingBox());
                return Rotation.angularDistance(rotation, Rotation.fromPositions(vec3d, vec3d2));
            });
            default -> comparator = null;
        }

        return comparator;
    }

    private static int getArmorValue(Entity entity) {
        return entity instanceof LivingEntity ? ((LivingEntity)entity).getArmor() : DEFAULT_ARMOR_VALUE;
    }

    public static LivingEntity getPrimaryTargetDefault() {
        return getPrimaryTarget(false);
    }

    public static ArrayList<Entity> getTargetListDefault() {
        return getTargetList(false);
    }

    private static Comparator<Entity> buildComparator(ListSetting listsetting, ClientPlayerEntity clientplayerentity) {
        Vec3d vec3d = clientplayerentity.getCameraPosVec(SystemUtil.getTickDelta());
        Rotation rotation = Rotation.fromEntity(clientplayerentity);
        Comparator<Entity> comparator = null;
        Iterator iterator = listsetting.getItems().iterator();

        while (iterator.hasNext()) {
            Comparator<Entity> comparator1 = createSortComparator((String)iterator.next(), listsetting, clientplayerentity, vec3d, rotation);
            if (comparator1 != null) {
                comparator = comparator == null ? comparator1 : comparator.thenComparing(comparator1);
            }
        }

        Comparator<Entity> comparator2 = Comparator.comparingDouble(clientplayerentity::distanceTo);
        return comparator == null ? comparator2 : comparator.thenComparing(comparator2);
    }

    public static <T> ArrayList<T> getMappedTargets(boolean flag, TargetSupplier<T> targetsupplier) {
        return getMappedTargetsSorted(flag, targetsupplier, null);
    }

    public static ArrayList<Entity> getTargetList(boolean flag) {
        return new ArrayList<>(flag ? allTargets : filteredTargets);
    }

    private static double getHealthScore(Entity entity) {
        return entity instanceof LivingEntity livingentity
            ? Math.floor((livingentity.getHealth() + livingentity.getAbsorptionAmount()) / 2.0)
            : Double.MAX_VALUE;
    }

    public static LivingEntity getPrimaryTarget(boolean flag) {
        ArrayList<Entity> arraylist = getTargetList(flag);
        if (!arraylist.isEmpty()) {
            Object object = arraylist.get(0);
            if (object instanceof LivingEntity) {
                return (LivingEntity)object;
            }
        }

        return null;
    }

    public static boolean isTargetedType(Entity entity, Targets targets) {
        if (!targets.friends.isEnabled() && entity instanceof PlayerEntity playerentity && PlayerTargetUtil.isFriend(playerentity.getUuid())) {
            return false;
        } else if (!targets.teammates.isEnabled() && entity instanceof PlayerEntity playerentity1 && PlayerTargetUtil.isIgnored(playerentity1.getUuid())) {
            return false;
        } else {
            if (targets.ignoreInvisible.isEnabled() && entity instanceof LivingEntity livingentity && livingentity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                boolean flag = false;

                for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                    if (equipmentslot.isArmorSlot() || equipmentslot == EquipmentSlot.MAINHAND || equipmentslot == EquipmentSlot.OFFHAND) {
                        ItemStack itemstack = livingentity.getEquippedStack(equipmentslot);
                        if (!itemstack.isEmpty()) {
                            flag = true;
                            break;
                        }
                    }
                }

                if (!flag) {
                    return false;
                }
            }

            if (targets.ignoreBlocked.isEnabled()) {
                Vec3d vec3d4 = VoilClient.mc.player.getCameraPosVec(SystemUtil.getTickDelta());
                Vec3d vec3d = entity.getEyePos();
                Vec3d vec3d1 = vec3d4;
                if (!CombatUtil.canSee(vec3d1, vec3d)) {
                    vec3d4 = VoilClient.mc.player.getCameraPosVec(SystemUtil.getTickDelta());
                    Vec3d vec3d2 = entity.getEntityPos();
                    Vec3d vec3d3 = vec3d4;
                    if (!CombatUtil.canSee(vec3d3, vec3d2)) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    public static <T> ArrayList<T> getMappedTargetsSorted(boolean flag, TargetSupplier<T> targetsupplier, Comparator<T> comparator) {
        ArrayList<Entity> arraylist = getTargetList(flag);
        ArrayList<T> arraylist1 = new ArrayList<>(arraylist.size());

        for (Entity entity : arraylist) {
            targetsupplier.get(entity).ifPresent(arraylist1::add);
        }

        if (comparator != null) {
            arraylist1.sort(comparator);
        }

        return arraylist1;
    }

    public static boolean isValidTarget(Entity entity, ClientPlayerEntity clientplayerentity, Targets targets) {
        if (entity != null && entity != clientplayerentity) {
            if (entity instanceof PlayerEntity playerentity && (!targets.players.isEnabled() || PlayerTargetUtil.isTracked(playerentity))) {
                return false;
            } else if (entity instanceof AnimalEntity && !targets.animals.isEnabled()) {
                return false;
            } else if (entity instanceof MobEntity && !targets.mobs.isEnabled()) {
                return false;
            } else if (entity instanceof EndCrystalEntity && !targets.crystals.isEnabled()) {
                return false;
            } else {
                return entity instanceof ArmorStandEntity ? false : !entity.getName().equals(clientplayerentity.getName());
            }
        } else {
            return false;
        }
    }

    @EventHandler
    public static void onUpdate(TickEvent tickevent) {
        Targets targets = (Targets)ModuleManager.INSTANCE.getModule(Targets.class);
        if (targets != null) {
            ClientWorld clientworld = VoilClient.mc.world;
            ClientPlayerEntity clientplayerentity = VoilClient.mc.player;
            if (clientworld != null && clientplayerentity != null) {
                ArrayList<Entity> arraylist = new ArrayList<>();
                ArrayList<Entity> arraylist1 = new ArrayList<>();

                for (Entity entity : clientworld.getEntities()) {
                    if (isValidTarget(entity, clientplayerentity, targets)) {
                        arraylist.add(entity);
                        if (isTargetedType(entity, targets)) {
                            arraylist1.add(entity);
                        }
                    }
                }

                Comparator<Entity> comparator = buildComparator(targets.targetSort, clientplayerentity);
                arraylist1.sort(comparator);
                arraylist.sort(comparator);
                filteredTargets = List.copyOf(arraylist1);
                allTargets = List.copyOf(arraylist);
            }
        }
    }
}
