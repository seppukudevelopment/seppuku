package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.gui.hud.component.ResizableHudComponent;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.Timer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author noil
 */
public final class TpsGraphComponent extends ResizableHudComponent {

    private final List<TpsNode> tpsNodes = new CopyOnWriteArrayList<TpsNode>();
    private final Timer timer = new Timer();

    public TpsGraphComponent() {
        super("TpsGraph", 40, 18);
        this.setW(40);
        this.setH(18);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world != null && mc.getCurrentServerData() != null) {
            if (this.tpsNodes.size() > (this.getW() / 2)) { // overflow protection
                this.tpsNodes.clear();
            }

            if (this.timer.passed(1000/* 1 sec */)) {
                if (this.tpsNodes.size() > (this.getW() / 2 - 1)) {
                    this.tpsNodes.remove(0); // remove oldest
                }

                this.tpsNodes.add(new TpsNode(Seppuku.INSTANCE.getTickRateManager().getTickRate()));
                this.timer.reset();
            }

            // background
            RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x75101010);

            // tps bars
            for (int i = 0; i < this.tpsNodes.size(); i++) {
                final TpsNode tpsNode = this.tpsNodes.get(i);
                final float mappedX = (float) MathUtil.map((this.getW() / 2 - 1) - i, 0, (this.getW() / 2 - 1), this.getX() + this.getW() - 1, this.getX() + 1);
                final float mappedY = (float) MathUtil.map(tpsNode.tps, 0, 20, this.getY() + this.getH() - 1, this.getY() + 1);
                RenderUtil.drawGradientRect(mappedX - tpsNode.size, mappedY, mappedX + tpsNode.size, this.getY() + this.getH(), tpsNode.color.getRGB(), 0x000FF0000);
                RenderUtil.drawRect(mappedX - tpsNode.size, mappedY, mappedX + tpsNode.size, mappedY + tpsNode.size, tpsNode.color.getRGB());
            }

            // border
            RenderUtil.drawBorderedRectBlurred(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 2.0f, 0x00000000, 0x90101010);

            /*GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            final String avg = String.format(ChatFormatting.WHITE + "AVG: %.2f", Seppuku.INSTANCE.getTickRateManager().getTickRate());
            final String last = String.format(ChatFormatting.WHITE + "LAST: %.2f", Seppuku.INSTANCE.getTickRateManager().getLastTick());
            mc.fontRenderer.drawStringWithShadow(avg, this.getX() * 2.0f + 4, this.getY() * 2.0f + 4, -1);
            mc.fontRenderer.drawStringWithShadow(last, this.getX() * 2.0f + 4, this.getY() * 2.0f + 4 + mc.fontRenderer.FONT_HEIGHT, -1);
            //GlStateManager.scale(-0.5f, -0.5f, -0.5f);
            GlStateManager.popMatrix();*/
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(tps graph)"));
            mc.fontRenderer.drawStringWithShadow("(tps graph)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

    private class TpsNode extends Vec2f {

        public float size = 1.0f;
        public float tps = 0.0f;
        public Color color;

        public TpsNode(float tps) {
            super(0, 0);
            this.tps = tps;

            int colorR = (int) MathUtil.map(tps, 0, 20, 255, 0);
            int colorG = (int) MathUtil.map(tps, 0, 20, 0, 255);
            int colorB = 0;

            this.color = new Color(colorR, colorG, colorB);
        }
    }


}
