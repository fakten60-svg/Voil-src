package lol.lwes.voil.event;

import lol.lwes.voil.VoilClient;
import net.minecraft.client.input.Input;

public class InputEvent extends Event {
    public MovementInput input;
    public boolean localPlayer;

    public InputEvent(Input input) {
        this.input = MovementInput.fromInput(input);
        this.localPlayer = VoilClient.mc.player != null && input == VoilClient.mc.player.input;
    }
}
