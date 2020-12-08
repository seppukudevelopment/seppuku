package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

/**
 * Author Seth
 * 8/7/2019 @ 12:48 PM.
 */
public final class BiomeComponent extends DraggableHudComponent {

    public BiomeComponent() {
        super("Biome");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world != null) {
            final BlockPos pos = mc.player.getPosition();
            final Chunk chunk = mc.world.getChunk(pos);
            final Biome biome = chunk.getBiome(pos, mc.world.getBiomeProvider());

            this.setW(mc.fontRenderer.getStringWidth(biome.getBiomeName()));
            mc.fontRenderer.drawStringWithShadow(biome.getBiomeName(), this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(biome)"));
            mc.fontRenderer.drawStringWithShadow("(biome)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}