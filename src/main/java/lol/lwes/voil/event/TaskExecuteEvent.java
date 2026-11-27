package lol.lwes.voil.event;

public class TaskExecuteEvent extends Event {
    private static final TaskExecuteEvent INSTANCE = new TaskExecuteEvent();

    public static TaskExecuteEvent getInstance() {
        INSTANCE.setCancelled2(false);
        return INSTANCE;
    }
}
