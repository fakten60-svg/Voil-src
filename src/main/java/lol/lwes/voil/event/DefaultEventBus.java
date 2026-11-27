package lol.lwes.voil.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.util.CrashHandler;
import lol.lwes.voil.util.reflect.MappingNotFoundException;
import org.slf4j.LoggerFactory;

public class DefaultEventBus implements EventBus {
    private final Map<Object, List<EventListener>> listenersByInstance = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<EventListener>> staticListeners = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<EventListener>> listenersByEvent = new ConcurrentHashMap<>();
    private final List<LookupProviderEntry> lookupProviders = new ArrayList<>();

    @Override
    public void register(Object object) {
        List<EventListener> list = this.getListeners(object.getClass(), object);
        this.registerAll(list, false);
    }

    @Override
    public void unregisterStatic(Class oclass) {
        this.unregisterAll(this.getListeners(oclass, null), true);
    }

    @Override
    public boolean hasListeners(Class oclass) {
        List<EventListener> list = this.listenersByEvent.get(oclass);
        return list != null && !list.isEmpty();
    }

    private boolean isListenerMethod(Method method) {
        if (!method.isAnnotationPresent(EventHandler.class)) {
            return false;
        } else if (method.getReturnType() != void.class) {
            return false;
        } else {
            return method.getParameterCount() != 1 ? false : !method.getParameters()[0].getType().isPrimitive();
        }
    }

    @Override
    public Object postObject(Object object) {
        List<EventListener> list = this.listenersByEvent.get(object.getClass());
        if (list != null) {
            for (EventListener eventlistener : list) {
                try {
                    eventlistener.invoke(object);
                } catch (Throwable throwable) {
                    this.handleListenerError(eventlistener, throwable);
                }
            }
        }

        return object;
    }

    private void insertByPriority(List<EventListener> list, EventListener eventlistener) {
        int i = 0;

        while (i < list.size() && eventlistener.getPriority() <= list.get(i).getPriority()) {
            i++;
        }

        list.add(i, eventlistener);
    }

    @Override
    public void unregister(Object object) {
        this.unregisterAll(this.getListeners(object.getClass(), object), false);
    }

    @Override
    public void registerStatic(Class oclass) {
        List<EventListener> list = this.getListeners(oclass, null);
        this.registerAll(list, true);
    }

    private void registerAll(List<EventListener> list, boolean flag) {
        for (EventListener eventlistener : list) {
            this.registerOne(eventlistener, flag);
        }
    }

    @Override
    public Cancellable post(Cancellable cancellable) {
        List<EventListener> list = this.listenersByEvent.get(cancellable.getClass());
        Handleable handleable = cancellable instanceof Handleable handleable1 ? handleable1 : null;
        if (list != null) {
            cancellable.setCancelled2(false);

            for (EventListener eventlistener : list) {
                try {
                    eventlistener.invoke(cancellable);
                } catch (Throwable throwable) {
                    this.handleListenerError(eventlistener, throwable);
                    continue;
                }

                if (cancellable.isCancelled() || handleable != null && handleable.isHandled()) {
                    break;
                }
            }
        }

        return cancellable;
    }

    @Override
    public void registerLookupProvider(String s, LookupProvider lookupprovider) {
        synchronized (this.lookupProviders) {
            this.lookupProviders.add(new LookupProviderEntry(s, lookupprovider));
        }
    }

    private void registerOne(EventListener eventlistener, boolean flag) {
        if (flag) {
            if (eventlistener.isStatic()) {
                this.insertByPriority(this.listenersByEvent.computeIfAbsent(eventlistener.getEventType(), oclass -> new CopyOnWriteArrayList<>()), eventlistener);
            }
        } else {
            this.insertByPriority(this.listenersByEvent.computeIfAbsent(eventlistener.getEventType(), oclass -> new CopyOnWriteArrayList<>()), eventlistener);
        }
    }

    private void unregisterAll(List<EventListener> list, boolean flag) {
        for (EventListener eventlistener : list) {
            this.unregisterOne(eventlistener, flag);
        }
    }

    private void unregisterOne(EventListener eventlistener, boolean flag) {
        List<EventListener> list = this.listenersByEvent.get(eventlistener.getEventType());
        if (list != null && (!flag || eventlistener.isStatic())) {
            list.remove(eventlistener);
        }
    }

    private void handleListenerError(EventListener eventlistener, Throwable throwable) {
        Object object = eventlistener.getInstance();
        if (object instanceof Module module) {
            CrashHandler.handleModuleCrash(module, throwable);
        } else {
            LoggerFactory.getLogger("Voil/EventBus").error("Exception in listener owned by {}", object.getClass().getName(), throwable);
        }
    }

    private LookupProvider findLookupProvider(Class<?> oclass) {
        synchronized (this.lookupProviders) {
            for (LookupProviderEntry lookupproviderentry : this.lookupProviders) {
                if (oclass.getName().startsWith(lookupproviderentry.name)) {
                    return lookupproviderentry.provider;
                }
            }
        }

        throw new MappingNotFoundException(oclass);
    }

    @Override
    public void registerListener(EventListener eventlistener) {
        this.registerOne(eventlistener, false);
    }

    @Override
    public void unregisterListener(EventListener eventlistener) {
        this.unregisterOne(eventlistener, false);
    }

    private List<EventListener> getListeners(Class<?> oclass, Object object) {
        Function<Object, List<EventListener>> function = object2 -> {
            List<EventListener> copyonwritearraylist = new CopyOnWriteArrayList<>();
            this.collectListeners(copyonwritearraylist, oclass, object);
            return copyonwritearraylist;
        };
        if (object == null) {
            return this.staticListeners.computeIfAbsent(oclass, function);
        }

        for (Object key : this.listenersByInstance.keySet()) {
            if (key == object) {
                return this.listenersByInstance.get(object);
            }
        }

        List<EventListener> list = function.apply(object);
        this.listenersByInstance.put(object, list);
        return list;
    }

    private void collectListeners(List<EventListener> list, Class<?> oclass, Object object) {
        for (Method method : oclass.getDeclaredMethods()) {
            if (this.isListenerMethod(method)) {
                list.add(new MethodListener(this.findLookupProvider(oclass), oclass, object, method));
            }
        }

        if (oclass.getSuperclass() != null) {
            this.collectListeners(list, oclass.getSuperclass(), object);
        }
    }
}
