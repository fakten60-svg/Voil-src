package lol.lwes.voil.util.inventory;

public record PendingSwap(int slot, int delay, Runnable callback) {
}
