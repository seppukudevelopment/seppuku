package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.friend.Friend;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/22/2019 @ 5:37 AM.
 */
public final class MiddleClickFriendsModule extends Module {

    private boolean clicked;

    public MiddleClickFriendsModule() {
        super("MiddleClick", new String[]{"MCF", "MiddleClickFriends", "MClick"}, "Allows you to middle click players to add them as a friend", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.currentScreen == null) {
                if (Mouse.isButtonDown(2)) {
                    if (!this.clicked) {
                        final RayTraceResult result = mc.objectMouseOver;
                        if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY) {
                            final Entity entity = result.entityHit;
                            if (entity != null && entity instanceof EntityPlayer) {
                                final Friend friend = Seppuku.INSTANCE.getFriendManager().isFriend(entity);

                                if (friend != null) {
                                    Seppuku.INSTANCE.getFriendManager().getFriendList().remove(friend);
                                    Seppuku.INSTANCE.logChat("Removed \247c" + friend.getAlias() + " \247f");
                                } else {
                                    Seppuku.INSTANCE.getFriendManager().add(entity.getName(), entity.getName(), true);
                                    Seppuku.INSTANCE.logChat("Added \247c" + entity.getName() + " \247f");
                                }
                            }
                        }
                    }
                    this.clicked = true;
                } else {
                    this.clicked = false;
                }
            }
        }
    }

}
