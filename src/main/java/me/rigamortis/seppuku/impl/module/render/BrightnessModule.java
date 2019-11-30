package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/22/2019 @ 5:49 AM.
 */
public final class BrightnessModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The brightness mode to use.", Mode.GAMMA);

    private enum Mode {
        GAMMA, POTION, TABLE
    }

    public final Value<Boolean> effects = new Value<Boolean>("Effects", new String[]{"Eff"}, "Blocks blindness & nausea effects if enabled.", true);

    private float lastGamma;

    private World world;

    public BrightnessModule() {
        super("Brightness", new String[]{"FullBright", "Bright"}, "Makes the world brighter", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (this.mode.getValue() == Mode.GAMMA) {
            this.lastGamma = Minecraft.getMinecraft().gameSettings.gammaSetting;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (this.mode.getValue() == Mode.GAMMA) {
            Minecraft.getMinecraft().gameSettings.gammaSetting = this.lastGamma;
        }

        if (this.mode.getValue() == Mode.POTION && Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.removePotionEffect(MobEffects.NIGHT_VISION);
        }

        if (this.mode.getValue() == Mode.TABLE) {
            if (Minecraft.getMinecraft().world != null) {
                float f = 0.0F;

                for (int i = 0; i <= 15; ++i) {
                    float f1 = 1.0F - (float) i / 15.0F;
                    Minecraft.getMinecraft().world.provider.getLightBrightnessTable()[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 1.0F + 0.0F;
                }
            }
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            switch (this.mode.getValue()) {
                case GAMMA:
                    Minecraft.getMinecraft().gameSettings.gammaSetting = 1000;
                    break;
                case POTION:
                    Minecraft.getMinecraft().player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5210));
                    break;
                case TABLE:
                    if (this.world != Minecraft.getMinecraft().world) {
                        if (Minecraft.getMinecraft().world != null) {
                            for (int i = 0; i <= 15; ++i) {
                                Minecraft.getMinecraft().world.provider.getLightBrightnessTable()[i] = 1.0f;
                            }
                        }
                        this.world = Minecraft.getMinecraft().world;
                    }
                    break;
            }
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketEntityEffect) {
                if (this.effects.getValue()) {
                    final SPacketEntityEffect packet = (SPacketEntityEffect) event.getPacket();
                    if (Minecraft.getMinecraft().player != null && packet.getEntityId() == Minecraft.getMinecraft().player.getEntityId()) {
                        if (packet.getEffectId() == 9 || packet.getEffectId() == 15) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

}
