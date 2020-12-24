package me.rigamortis.seppuku.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.gui.hud.component.ResizableHudComponent;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author noil
 */
public final class TpsGraphComponent extends ResizableHudComponent {

    private final List<TpsNode> tpsNodes = new CopyOnWriteArrayList<TpsNode>();
    private final Timer timer = new Timer();

    private float timerDelay = 500.0f;

    public TpsGraphComponent() {
        super("TpsGraph", 60, 27);
        this.setW(60);
        this.setH(27);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world != null && mc.getCurrentServerData() != null) {
            if (this.tpsNodes.size() > (this.getW() / 2)) { // overflow protection
                this.tpsNodes.clear();
            }

            if (this.timer.passed(this.timerDelay)) {
                if (this.tpsNodes.size() > (this.getW() / 2 - 1)) {
                    this.tpsNodes.remove(0); // remove oldest
                }

                this.tpsNodes.add(new TpsNode(Seppuku.INSTANCE.getTickRateManager().getTickRate()));
                this.timer.reset();
            }

            String hoveredData = "";

            // grid
            if (mc.currentScreen instanceof GuiHudEditor) {
                for (float j = this.getX() + this.getW(); j > this.getX(); j -= 20) {
                    if (j <= this.getX())
                        continue;

                    if (j >= this.getX() + this.getW())
                        continue;

                    RenderUtil.drawLine(j, this.getY(), j, this.getY() + this.getH(), 2.0f, 0x75101010);
                }
            } else {
                // background
                RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x75101010);
            }

            // tps bars
            for (int i = 0; i < this.tpsNodes.size(); i++) {
                final TpsNode tpsNode = this.tpsNodes.get(i);
                final float mappedX = (float) MathUtil.map((this.getW() / 2 - 1) - i, 0, (this.getW() / 2 - 1), this.getX() + this.getW() - 1, this.getX() + 1);
                final float mappedY = (float) MathUtil.map(tpsNode.tps, 0, 20, this.getY() + this.getH() - 1, this.getY() + 1);
                RenderUtil.drawGradientRect(mappedX - tpsNode.size, mappedY, mappedX + tpsNode.size, this.getY() + this.getH(), tpsNode.color.getRGB(), 0x00FF0000);
                RenderUtil.drawRect(mappedX - tpsNode.size, mappedY, mappedX + tpsNode.size, mappedY + tpsNode.size, tpsNode.color.getRGB());
                if (mouseX >= mappedX && mouseX <= mappedX + tpsNode.size && mouseY >= mappedY && mouseY <= this.getY() + this.getH()) {
                    RenderUtil.drawRect(mappedX - tpsNode.size, mappedY, mappedX + tpsNode.size, this.getY() + this.getH(), 0x40101010);

                    final DecimalFormat decimalFormat = new DecimalFormat("###.##");
                    hoveredData = String.format("TPS: %s", decimalFormat.format(tpsNode.tps));
                }
            }

            if (this.isMouseInside(mouseX, mouseY)) {
                // draw delay
                mc.fontRenderer.drawStringWithShadow(this.timerDelay + "ms", this.getX() + 2, this.getY() + this.getH() - mc.fontRenderer.FONT_HEIGHT - 2, 0xFFAAAAAA);
            }

            // hovered data
            if (!hoveredData.equals("")) {
                mc.fontRenderer.drawStringWithShadow(hoveredData, this.getX() + 2, this.getY() + this.getH() - mc.fontRenderer.FONT_HEIGHT * 2 - 2, 0xFFAAAAAA);
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
            mc.fontRenderer.drawStringWithShadow("(tps graph)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);
        if (this.isMouseInside(mouseX, mouseY) && button == 1/* right click */) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                this.timerDelay += 100.0f;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                this.timerDelay -= 10.0f;
            } else {
                this.timerDelay -= 100.0f;
            }

            if (this.timerDelay <= 0.0f || this.timerDelay > 1000.0f)
                this.timerDelay = 1000.0f;
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
