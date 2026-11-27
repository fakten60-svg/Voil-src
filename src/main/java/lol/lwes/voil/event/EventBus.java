package lol.lwes.voil.event;

public interface EventBus {
    void register(Object object);

    void unregisterStatic(Class oclass);

    void unregister(Object object);

    void registerStatic(Class oclass);

    void registerListener(EventListener eventlistener);

    void unregisterListener(EventListener eventlistener);

    boolean hasListeners(Class oclass);

    Object postObject(Object object);

    Cancellable post(Cancellable cancellable);

    void registerLookupProvider(String s, LookupProvider lookupprovider);
}
