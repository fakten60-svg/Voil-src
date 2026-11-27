package lol.lwes.voil.event;

import net.minecraft.util.PlayerInput;

public class PlayerInputState {
    public boolean forward;
    public boolean backward;
    public boolean left;
    public boolean right;
    public boolean jump;
    public boolean sneak;
    public boolean sprint;

    public PlayerInput toPlayerInput() {
        return new PlayerInput(this.forward, this.backward, this.left, this.right, this.jump, this.sneak, this.sprint);
    }

    public static PlayerInputState fromPlayerInput(PlayerInput playerinput) {
        PlayerInputState playerinputstate = new PlayerInputState();
        playerinputstate.forward = playerinput.forward();
        playerinputstate.backward = playerinput.backward();
        playerinputstate.left = playerinput.left();
        playerinputstate.right = playerinput.right();
        playerinputstate.jump = playerinput.jump();
        playerinputstate.sneak = playerinput.sneak();
        playerinputstate.sprint = playerinput.sprint();
        return playerinputstate;
    }
}
