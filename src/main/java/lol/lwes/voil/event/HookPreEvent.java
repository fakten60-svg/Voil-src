package lol.lwes.voil.event;

public class HookPreEvent extends HookEvent {
    public static HookPreEvent create(String s) {
        HookPreEvent hookpreevent = new HookPreEvent();
        hookpreevent.name = s;
        return hookpreevent;
    }
}
