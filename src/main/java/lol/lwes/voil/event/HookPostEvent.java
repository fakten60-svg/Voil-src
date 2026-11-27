package lol.lwes.voil.event;

public class HookPostEvent extends HookEvent {
    public static HookPostEvent create(String s) {
        HookPostEvent hookpostevent = new HookPostEvent();
        hookpostevent.name = s;
        return hookpostevent;
    }
}
