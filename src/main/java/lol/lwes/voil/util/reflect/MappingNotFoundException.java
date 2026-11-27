package lol.lwes.voil.util.reflect;

public class MappingNotFoundException extends RuntimeException {
    public MappingNotFoundException(Class oclass) {
        super(new String("No registered lambda listener for '" + oclass.getName() + "'."));
    }
}
