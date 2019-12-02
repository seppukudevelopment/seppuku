package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 5/11/2019 @ 10:43 AM.
 */
public final class ChatManager {

    private Timer timer = new Timer();

    private List<String> chatBuffer = new ArrayList<>();

    private World world;

    public ChatManager() {
        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    public void add(String s) {
        this.chatBuffer.add(s);
    }

    public void unload() {
        this.chatBuffer.clear();
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {

            if (this.world != Minecraft.getMinecraft().world) {
                this.world = Minecraft.getMinecraft().world;
                this.chatBuffer.clear();
            }

            for (int i = 0; i < this.chatBuffer.size(); i++) {
                final String s = this.chatBuffer.get(i);
                if (s != null) {
                    if (this.timer.passed(200.0f)) {
                        Minecraft.getMinecraft().player.sendChatMessage(s);
                        this.chatBuffer.remove(s);
                        this.timer.reset();
                        i--;
                    }
                }
            }
        }
    }

}
