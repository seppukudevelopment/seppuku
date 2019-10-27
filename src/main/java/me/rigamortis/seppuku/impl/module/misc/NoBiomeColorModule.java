package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.world.EventFoliageColor;
import me.rigamortis.seppuku.api.event.world.EventGrassColor;
import me.rigamortis.seppuku.api.event.world.EventWaterColor;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.NumberValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 8/11/2019 @ 1:27 AM.
 */
public final class NoBiomeColorModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Default", "Custom"});

    public final NumberValue red = new NumberValue("Red", new String[]{"R"}, 255.0f, Float.class, 0.0f, 255.0f, 1.0f);
    public final NumberValue green = new NumberValue("Green", new String[]{"G"}, 255.0f, Float.class, 0.0f, 255.0f, 1.0f);
    public final NumberValue blue = new NumberValue("Blue", new String[]{"B"}, 255.0f, Float.class, 0.0f, 255.0f, 1.0f);

    private float prevRed;
    private float prevGreen;
    private float prevBlue;

    private int prevMode;

    public NoBiomeColorModule() {
        super("NoBiomeColor", new String[]{"AntiBiomeColor", "NoBiomeC", "NoBiome"}, "Prevents the game from altering the color of foliage, water and grass in biomes", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.reload();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.reload();
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    private void reload() {
        final Minecraft mc = Minecraft.getMinecraft();

        if(mc.world != null) {
            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) mc.player.posX - 256,
                    (int) mc.player.posY - 256,
                    (int) mc.player.posZ - 256,
                    (int) mc.player.posX + 256,
                    (int) mc.player.posY + 256,
                    (int) mc.player.posZ + 256);
        }
    }

    private int getHex() {
        return (255 << 24) | ((int)this.red.getFloat() << 16) | ((int)this.green.getFloat() << 8 | (int)this.blue.getFloat());
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.prevRed != this.red.getFloat()) {
                this.prevRed = this.red.getFloat();
                this.reload();
            }
            if (this.prevGreen != this.green.getFloat()) {
                this.prevGreen = this.green.getFloat();
                this.reload();
            }
            if (this.prevBlue != this.blue.getFloat()) {
                this.prevBlue = this.blue.getFloat();
                this.reload();
            }
            if(this.prevMode != this.mode.getInt()) {
                this.prevMode = this.mode.getInt();
                this.reload();
            }
        }
    }

    @Listener
    public void getGrassColor(EventGrassColor event) {
        switch (this.mode.getInt()) {
            case 0:
                event.setColor(0x79c05a);
                break;
            case 1:
                event.setColor(this.getHex());
                break;
        }
        event.setCanceled(true);
    }

    @Listener
    public void getFoliageColor(EventFoliageColor event) {
        switch (this.mode.getInt()) {
            case 0:
                event.setColor(0x59ae30);
                break;
            case 1:
                event.setColor(this.getHex());
                break;
        }
        event.setCanceled(true);
    }

    @Listener
    public void getWaterColor(EventWaterColor event) {
        switch (this.mode.getInt()) {
            case 0:
                event.setColor(0x1E97F2);
                break;
            case 1:
                event.setColor(this.getHex());
                break;
        }
        event.setCanceled(true);
    }

}
