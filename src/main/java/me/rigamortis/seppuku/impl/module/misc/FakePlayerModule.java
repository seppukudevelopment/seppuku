package me.rigamortis.seppuku.impl.module.misc;

import com.mojang.authlib.GameProfile;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;

/**
 * @author Seth
 * @author noil
 */
public final class FakePlayerModule extends Module {

    public final Value<String> username = new Value<String>("Username", new String[]{"name", "uname", "u"}, "The username of the fake player", "Notch");

    private EntityOtherPlayerMP entity;
    private final Minecraft mc = Minecraft.getMinecraft();

    public FakePlayerModule() {
        super("FakePlayer", new String[]{"FakeP", "FPlayer"}, "Adds a fake player to your game", "NONE", -1, Module.ModuleType.MISC);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null && mc.world != null) {
            entity = new EntityOtherPlayerMP(mc.world, new GameProfile(mc.player.getUniqueID(), username.getValue()));
            entity.copyLocationAndAnglesFrom(mc.player);
            entity.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(6942069, entity);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.world != null) {
            if (entity != null) {
                mc.world.removeEntity(entity);
            }
        }
    }
}
