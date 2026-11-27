package lol.lwes.voil.util.target;

import java.util.Optional;
import net.minecraft.entity.Entity;

public interface TargetSupplier<T> {
    Optional<T> get(Entity entity);
}
