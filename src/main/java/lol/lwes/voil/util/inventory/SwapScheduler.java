package lol.lwes.voil.util.inventory;

import java.util.ArrayDeque;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.TickEvent;

public class SwapScheduler {
    public static final ArrayDeque PENDING_SWAPS = new ArrayDeque();

    public static void clearQueue() {
        PENDING_SWAPS.clear();
    }

    public static SwapBuilder newSwap() {
        return new SwapBuilder();
    }

    @EventHandler
    public void tick(TickEvent tickevent) {
        if (!PENDING_SWAPS.isEmpty() && VoilClient.mc.player != null) {
            int i = PENDING_SWAPS.size();

            for (int j = 0; j < i; j++) {
                PendingSwap pendingswap = (PendingSwap)PENDING_SWAPS.poll();
                if (pendingswap.delay() > 1) {
                    ArrayDeque arraydeque = PENDING_SWAPS;
                    int k = pendingswap.delay();
                    arraydeque.add(new PendingSwap(pendingswap.slot(), k - 1, pendingswap.callback()));
                } else {
                    int l = pendingswap.slot();
                    InventoryUtil.selectSlot(l);
                    if (pendingswap.callback() != null) {
                        pendingswap.callback().run();
                    }
                }
            }
        }
    }
}
