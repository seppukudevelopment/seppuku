package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.util.Timer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.text.DecimalFormat;

/**
 * Author Seth
 * 8/31/2019 @ 3:07 AM.
 */
public final class PacketTimeComponent extends DraggableHudComponent {

    private final Timer timer = new Timer();

    public PacketTimeComponent() {
        super("PacketTime");
        this.setH(mc.fontRenderer.FONT_HEIGHT);

        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() != null) {
                this.timer.reset();
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null && mc.world != null) {
            final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;
            final String delay = ChatFormatting.GRAY + "Packet " + ChatFormatting.RESET + (seconds >= 3.0f ? "\2474" : "\247f") + new DecimalFormat("0.0").format(seconds);

            this.setW(mc.fontRenderer.getStringWidth(delay));
            mc.fontRenderer.drawStringWithShadow(delay, this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(packet delay)"));
            mc.fontRenderer.drawStringWithShadow("(packet delay)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}
