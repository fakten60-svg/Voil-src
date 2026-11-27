package lol.lwes.voil.module.combat;

import java.util.function.Predicate;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.RaycastEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class Piercing extends Module {
    private final MultiSelectSetting targets;

    public Piercing() {
        super("Piercing", "Entity raycasts ignore blocks", Category.Combat);
        String[] astring2 = new String[]{"Players", "Living"};
        String[] astring3 = new String[]{"Players"};
        String s2 = "What entities to pierce";
        String[] astring = astring3;
        String[] astring1 = astring2;
        this.targets = new MultiSelectSetting("Targets", astring1, astring, s2);
        Setting[] asetting = new Setting[]{this.targets};
        this.addSettings(asetting);
    }

    private boolean canPierce(Entity entity, Entity entity1) {
        if (entity1 == entity) {
            return false;
        }

        boolean flag = this.targets.getSelectedOptions().contains("Players");
        boolean flag1 = this.targets.getSelectedOptions().contains("Living");
        return !flag && !flag1 ? true : flag && entity1 instanceof PlayerEntity || flag1 && entity1 instanceof LivingEntity;
    }

    @EventHandler
    public void onRaycast(RaycastEvent raycastevent) {
        Entity entity = raycastevent.getEntity();
        if (entity == mc.player || entity == mc.getCameraEntity()) {
            Predicate<Entity> predicate = entity2 -> this.canPierce(entity, entity2);
            raycastevent.setFilter(predicate);
        }
    }
}
