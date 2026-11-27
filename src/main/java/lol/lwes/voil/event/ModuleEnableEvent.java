package lol.lwes.voil.event;

import lol.lwes.voil.module.Module;

public class ModuleEnableEvent extends Event {
    public Module module;

    public ModuleEnableEvent(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return this.module;
    }
}
