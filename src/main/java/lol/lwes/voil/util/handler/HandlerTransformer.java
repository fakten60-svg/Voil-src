package lol.lwes.voil.util.handler;

public interface HandlerTransformer {
    HandlerNode transform(HandlerNode handlernode, Object object);

    boolean canTransform(HandlerNode handlernode);
}
