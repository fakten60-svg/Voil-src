package lol.lwes.voil.event;

import net.minecraft.util.Hand;

public class HandSwingEvent extends Event {
    private Hand hand;

    public HandSwingEvent(Hand hand) {
        this.hand = hand;
    }

    public Hand getHand() {
        return this.hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }
}
