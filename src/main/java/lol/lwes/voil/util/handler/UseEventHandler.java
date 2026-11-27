package lol.lwes.voil.util.handler;

import lol.lwes.voil.event.UseKeyEvent;

public interface UseEventHandler extends HandlerNode {
    default UseEventHandler applyUse(UseKeyEvent usekeyevent) {
        UseHandlerTransformer usehandlertransformer = UseHandlerTransformer.INSTANCE;
        return (UseEventHandler)this.applyTransformer(usehandlertransformer, usekeyevent, true);
    }

    default UseEventHandler getDefaultUseHandler() {
        return null;
    }

    default UseEventHandler getUseHandler(UseKeyEvent usekeyevent) {
        return this.getDefaultUseHandler();
    }

    boolean hasUseHandler();
}
