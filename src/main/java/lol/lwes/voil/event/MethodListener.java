package lol.lwes.voil.event;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public class MethodListener implements EventListener {
    private static boolean LEGACY_JAVA;
    private static Constructor LOOKUP_CONSTRUCTOR;
    private static Method PRIVATE_LOOKUP;
    private final Class eventType;
    private final boolean isStatic;
    private final int priority;
    private Consumer handler;
    private Object instance;

    public MethodListener(LookupProvider lookupprovider, Class oclass, Object object, Method method) {
        this.eventType = method.getParameters()[0].getType();
        this.isStatic = Modifier.isStatic(method.getModifiers());
        this.priority = method.getAnnotation(EventHandler.class).priority();
        this.instance = object;

        try {
            String s = method.getName();
            Lookup lookup;
            if (LEGACY_JAVA) {
                boolean flag = LOOKUP_CONSTRUCTOR.isAccessible();
                LOOKUP_CONSTRUCTOR.setAccessible(true);
                lookup = (Lookup)LOOKUP_CONSTRUCTOR.newInstance(oclass);
                LOOKUP_CONSTRUCTOR.setAccessible(flag);
            } else {
                lookup = lookupprovider.lookup(PRIVATE_LOOKUP, oclass);
            }

            MethodType methodtype3 = MethodType.methodType(void.class, method.getParameters()[0].getType());
            MethodHandle methodhandle;
            MethodType methodtype;
            if (this.isStatic) {
                methodhandle = lookup.findStatic(oclass, s, methodtype3);
                methodtype = MethodType.methodType(Consumer.class);
            } else {
                methodhandle = lookup.findVirtual(oclass, s, methodtype3);
                methodtype = MethodType.methodType(Consumer.class, oclass);
            }

            MethodHandle methodhandle1 = LambdaMetafactory.metafactory(
                    lookup, "accept", methodtype, MethodType.methodType(void.class, Object.class), methodhandle, methodtype3
                )
                .getTarget();
            if (this.isStatic) {
                this.handler = (Consumer)methodhandle1.invoke();
            } else {
                this.handler = (Consumer)methodhandle1.invoke((Object)object);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    static {
        try {
            LEGACY_JAVA = System.getProperty("java.version").startsWith("1.8");
            if (LEGACY_JAVA) {
                LOOKUP_CONSTRUCTOR = Lookup.class.getDeclaredConstructor(Class.class);
            } else {
                PRIVATE_LOOKUP = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, Lookup.class);
            }
        } catch (NoSuchMethodException nosuchmethodexception) {
            nosuchmethodexception.printStackTrace();
        }
    }

    @Override
    public boolean isStatic() {
        return this.isStatic;
    }

    @Override
    public Class getEventType() {
        return this.eventType;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public Object getInstance() {
        return this.instance;
    }

    @Override
    public void invoke(Object object) {
        this.handler.accept(object);
    }

    @Override
    public void invoke(Object[] aobject) {
        this.invoke(aobject[0]);
    }
}
