package lol.lwes.voil.event;

public interface EventListener {
    void invoke(Object[] aobject);

    boolean isStatic();

    Class getEventType();

    int getPriority();

    Object getInstance();

    void invoke(Object object);
}
