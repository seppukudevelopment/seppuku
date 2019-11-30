package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/23/2019 @ 7:41 PM.
 */
public final class AutoDisconnectModule extends Module {

    public final Value<Float> health = new Value("Health", new String[]{"Hp"}, "The amount of health needed to disconnect.", 8.0f, 0.0f, 20.0f, 0.5f);

    public AutoDisconnectModule() {
        super("AutoDisconnect", new String[]{"Disconnect"}, "Automatically disconnects when health is low enough", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (Minecraft.getMinecraft().player.getHealth() <= this.health.getValue()) {
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketHeldItemChange(420));
                this.toggle();
            }
        }
    }

}
