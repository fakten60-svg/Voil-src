package lol.lwes.voil.event;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface LookupProvider {
    Lookup lookup(Method method, Class oclass) throws InvocationTargetException, IllegalAccessException;
}
