package lol.lwes.voil.util.handler;

import lol.lwes.voil.event.AttackKeyEvent;

public class AttackHandlerTransformer implements HandlerTransformer {
    public static final AttackHandlerTransformer INSTANCE = new AttackHandlerTransformer();

    @Override
    public HandlerNode transform(HandlerNode handlernode, Object object) {
        AttackEventHandler attackeventhandler1 = (AttackEventHandler)handlernode;
        AttackKeyEvent attackkeyevent = (AttackKeyEvent)object;
        AttackEventHandler attackeventhandler = attackeventhandler1;
        return this.transformAttack(attackeventhandler, attackkeyevent);
    }

    public boolean canTransformAttack(AttackEventHandler attackeventhandler) {
        return attackeventhandler.hasAttackHandler();
    }

    public AttackEventHandler transformAttack(AttackEventHandler attackeventhandler, AttackKeyEvent attackkeyevent) {
        return attackeventhandler.getAttackHandler(attackkeyevent);
    }

    @Override
    public boolean canTransform(HandlerNode handlernode) {
        AttackEventHandler attackeventhandler = (AttackEventHandler)handlernode;
        return this.canTransformAttack(attackeventhandler);
    }
}
