package me.rigamortis.seppuku.impl.command;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/20/2019 @ 12:25 AM.
 */
public final class InvSeeCommand extends Command {

    private String entity;

    public InvSeeCommand() {
        super("InvSee", new String[] {"InventorySee"}, "Allows you to see another players inventory", "InvSee <Player>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        try {
            this.entity = split[1];
            Seppuku.INSTANCE.getEventManager().addEventListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void render(EventRender2D event) {
        try{
            final Minecraft mc = Minecraft.getMinecraft();

            EntityPlayer player = null;

            for(Entity e : mc.world.loadedEntityList) {
                if(e != null && e instanceof EntityPlayer) {
                    if(e.getName().equalsIgnoreCase(this.entity)) {
                        player = (EntityPlayer)e;
                        break;
                    }
                }
            }

            if(player != null) {
                mc.displayGuiScreen(new GuiInventory(player));
            }else{
                Seppuku.INSTANCE.errorChat("\"" + this.entity + "\" is not within range");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }

}
