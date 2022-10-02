package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author Seth
 * @author noil
 */
public final class GuiMoveModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"mod", "m"}, "Change between modes", Mode.NEW);
    public final Value<Boolean> allowSignMove = new Value<Boolean>("AllowSignMove", new String[]{"sign", "allowsign", "as"}, "If enabled you will be able to move while in a sign GUI", false);
    public GuiMoveModule() {
        super("GUIMove", new String[]{"InvMove", "InventoryMove", "GUIM"}, "Allows you to move while guis are open", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onUpdateWalkingPlayer(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.currentScreen instanceof GuiChat || mc.currentScreen == null || (!this.allowSignMove.getValue() && (mc.currentScreen instanceof GuiEditSign || mc.currentScreen instanceof GuiScreenBook || mc.currentScreen instanceof GuiRepair))) {
                return;
            }

            // handle mouse
            if (Mouse.isButtonDown(2)) {
                Mouse.setGrabbed(true);
                mc.inGameHasFocus = true;
            } else {
                Mouse.setGrabbed(false);
                mc.inGameHasFocus = false;
            }

            // handle movement
            switch (this.mode.getValue()) {
                case NEW:
                    final KeyBinding[] keyBindings = {
                            mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight,
                            mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindSprint};
                    for (KeyBinding keyBinding : keyBindings) {
                        KeyBinding.setKeyBindState(keyBinding.getKeyCode(), Keyboard.isKeyDown(keyBinding.getKeyCode()));
                    }
                    break;
                case OLD:
                    mc.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
                    mc.gameSettings.keyBindBack.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
                    mc.gameSettings.keyBindRight.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
                    mc.gameSettings.keyBindLeft.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
                    mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
                    mc.gameSettings.keyBindSneak.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
                    mc.gameSettings.keyBindSprint.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode());
                    break;
            }

            // handle y
            /*if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
                if (mc.player.isInLava() || mc.player.isInWater()) {
                    mc.player.motionY += 0.039f;
                } else {
                    if (mc.player.onGround) {
                        mc.player.jump();
                    }
                }
            }*/
        }
    }

    public enum Mode {
        NEW, OLD
    }

}
