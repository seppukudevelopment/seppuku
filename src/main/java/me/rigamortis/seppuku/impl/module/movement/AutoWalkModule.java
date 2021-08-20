package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.minecraft.EventDisplayGui;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author Seth
 * @author noil
 */
public final class AutoWalkModule extends Module {

    public final Value<Boolean> pressKeybind = new Value<>("PressKeybind", new String[]{"Keybind", "Key-bind", "PK", "P"}, "Presses the forward key for you.", true);
    public final Value<Boolean> autoDisable = new Value<>("AutoDisable", new String[]{"Disable", "ad"}, "Automatically disables the module on disconnect or death.", true);
    public final Value<Boolean> useBaritone = new Value<>("Baritone", new String[]{"Baritone", "B"}, "Sends a custom baritone command on enable.", false);
    public final Value<String> baritoneCommand = new Value<>("Command", new String[]{"Com", "C", "Text"}, "The message you want to send to communicate with baritone. (include prefix!)", "#explore");
    public final Value<String> baritoneCancelCommand = new Value<>("Cancel", new String[]{"BaritoneCancel", "Cancel", "Stop", "Text"}, "The cancel baritone command to send when disabled. (include prefix!)", "#cancel");
    public final Value<Float> waitTime = new Value<Float>("MsgDelay", new String[]{"MessageDelay", "CommandDelay", "Delay", "Wait", "Time", "md", "d"}, "Delay(ms) between sending baritone commands when standing.", 3000.0f, 0.0f, 8000.0f, 100.0f);
    public final Value<Float> standingTime = new Value<Float>("StandingTime", new String[]{"SDelay", "StandingT", "StandingWait", "StandingW", "SWait", "st"}, "Time(ms) needed to count as standing still. Prevents re-pathing when rubberbanding", 250.0f, 0.0f, 4000.0f, 50.0f);

    private final Timer sendCommandTimer = new Timer();
    private final Timer movementTimer = new Timer();

    public AutoWalkModule() {
        super("AutoWalk", new String[]{"AutomaticWalk"}, "Automatically presses the forward key or sends commands to baritone.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        //if (this.useBaritone.getValue())
        //    this.sendBaritoneCommand();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (this.useBaritone.getValue()) {
            this.cancelBaritoneCommand();
        }

        if (this.pressKeybind.getValue())
            Minecraft.getMinecraft().gameSettings.keyBindForward.pressed = false;
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            if (this.autoDisable.getValue()) {
                this.toggle(); // toggle off
            }
        }
    }

    @Listener
    public void onDisplayGui(EventDisplayGui event) {
        if (event.getScreen() != null) {
            if (this.autoDisable.getValue()) {
                if (event.getScreen() instanceof GuiDisconnected) {
                    this.toggle(); // toggle off
                }
            }
        }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (this.useBaritone.getValue())
            return;

        if (this.pressKeybind.getValue())
            Minecraft.getMinecraft().gameSettings.keyBindForward.pressed = true;
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage().equals(EventStageable.EventStage.PRE)) {
            if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null)
                return;

            if (this.autoDisable.getValue()) {
                if (!Minecraft.getMinecraft().player.isEntityAlive()) { // player is dead, check auto disable
                    Seppuku.INSTANCE.logChat(this.getDisplayName() + ": " + "Disabled automatically.");
                    this.toggle(); // toggle off
                }
            }

            if (this.useBaritone.getValue()) {
                boolean isStanding = true; // you could probably remove this flag now that there's a standing time check
                if (Minecraft.getMinecraft().player.motionX != 0 || Minecraft.getMinecraft().player.motionZ != 0) {
                    this.movementTimer.reset();
                    isStanding = false;
                }

                if (isStanding && this.movementTimer.passed(this.standingTime.getValue()) && this.sendCommandTimer.passed(this.waitTime.getValue())) {
                    this.sendCommandTimer.reset();
                    this.sendBaritoneCommand();
                }
                return;
            }

            if (this.pressKeybind.getValue())
                Minecraft.getMinecraft().gameSettings.keyBindForward.pressed = true;
        }
    }

    private void sendBaritoneCommand() {
        if (this.baritoneCommand.getValue().length() > 0) {
            Minecraft.getMinecraft().player.sendChatMessage(this.baritoneCommand.getValue());
        } else {
            Seppuku.INSTANCE.logChat(this.getDisplayName() + ": " + "Please enter a command to send to baritone.");
            this.toggle(); // toggle off
        }
    }

    private void cancelBaritoneCommand() {
        if (this.baritoneCancelCommand.getValue().length() > 0) {
            Minecraft.getMinecraft().player.sendChatMessage(this.baritoneCancelCommand.getValue());
        } else {
            Seppuku.INSTANCE.logChat(this.getDisplayName() + ": " + "Please check your syntax for the \"" + this.baritoneCancelCommand.getName() + "\" value.");
        }
    }
}
