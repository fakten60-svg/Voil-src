package lol.lwes.voil.event;

import lol.lwes.voil.module.Module;

public class ModuleDisableEvent extends Event {
    public Module module;

    public ModuleDisableEvent(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return this.module;
    }
}
