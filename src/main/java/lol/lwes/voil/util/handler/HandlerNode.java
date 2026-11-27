package lol.lwes.voil.util.handler;

public interface HandlerNode {
    default HandlerNode applyTransformer(HandlerTransformer transformer, Object event, boolean chain) {
        HandlerNode current = this;
        HandlerNode previous;
        do {
            previous = current;
            if (transformer.canTransform(current)) {
                current = transformer.transform(current, event);
            }
        } while (chain && current != null && current != previous);
        return current != null ? current : previous;
    }
}
