package lol.lwes.voil.util.handler;

import lol.lwes.voil.event.AttackKeyEvent;

public interface AttackEventHandler extends HandlerNode {
    default AttackEventHandler applyAttack(AttackKeyEvent attackkeyevent) {
        AttackHandlerTransformer attackhandlertransformer = AttackHandlerTransformer.INSTANCE;
        return (AttackEventHandler)this.applyTransformer(attackhandlertransformer, attackkeyevent, true);
    }

    default AttackEventHandler getDefaultAttackHandler() {
        return null;
    }

    default AttackEventHandler getAttackHandler(AttackKeyEvent attackkeyevent) {
        return this.getDefaultAttackHandler();
    }

    boolean hasAttackHandler();
}
