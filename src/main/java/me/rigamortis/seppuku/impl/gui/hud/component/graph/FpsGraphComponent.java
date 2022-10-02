package me.rigamortis.seppuku.impl.gui.hud.component.graph;

import me.rigamortis.seppuku.api.gui.hud.component.ResizableHudComponent;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @author noil
 */
public final class FpsGraphComponent extends ResizableHudComponent {

    public final Value<Float> delay = new Value<Float>("Delay", new String[]{"Del"}, "The amount of delay(ms) between updates.", 500.0f, 0.0f, 2500.0f, 100.0f);

    private final ArrayList<FpsNode> fpsNodes = new ArrayList<FpsNode>();
    private final Timer timer = new Timer();

    public FpsGraphComponent() {
        super("FpsGraph", 60, 27, 600, 400);
        this.setW(60);
        this.setH(27);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null && mc.world != null) {
            final ScaledResolution sr = new ScaledResolution(mc);
            final DecimalFormat decimalFormat = new DecimalFormat("###.##");

            if (this.fpsNodes.size() > (this.getW() / 2)) { // overflow protection
                this.fpsNodes.clear();
            }

            if (this.timer.passed(this.delay.getValue())) {
                if (this.fpsNodes.size() > (this.getW() / 2 - 1)) {
                    this.fpsNodes.remove(0); // remove oldest
                }

                final float fps = Minecraft.getDebugFPS();
                this.fpsNodes.add(new FpsNode(fps));

                this.timer.reset();
            }

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

            // create temporary hovered data string
            String hoveredData = "";

            // begin scissoring
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.glScissor(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), sr);

            // movement bars
            FpsNode lastNode = null;
            for (int i = 0; i < this.fpsNodes.size(); i++) {
                final FpsNode fpsNode = this.fpsNodes.get(i);

                final float mappedX = (float) MathUtil.map((this.getW() / 2 - 1) - i, 0, (this.getW() / 2 - 1), this.getX() + this.getW() - 1, this.getX() + 1);
                final float mappedY = (float) MathUtil.map(fpsNode.speed, -2.0f, this.getAverageHeight(), this.getY() + this.getH() - 1, this.getY() + 1) + this.getH() / 2;

                // set node's mapped coordinates
                fpsNode.mappedX = mappedX;
                fpsNode.mappedY = mappedY;

                // gradient of bar
                //RenderUtil.drawGradientRect(mappedX - fpsNode.size, mappedY, mappedX + fpsNode.size, this.getY() + this.getH(), fpsNode.color.getRGB(), 0x00000000);
                // rect on top of bar
                if (lastNode != null) {
                    RenderUtil.drawLine(fpsNode.mappedX, fpsNode.mappedY, lastNode.mappedX, lastNode.mappedY, 1.0f, -1);
                }

                // draw dot
                RenderUtil.drawRect(fpsNode.mappedX - fpsNode.size, fpsNode.mappedY - fpsNode.size, fpsNode.mappedX + fpsNode.size, fpsNode.mappedY + fpsNode.size, fpsNode.color.getRGB());

                // draw text
                if (i == this.fpsNodes.size() - 1) {
                    final String textToDraw = decimalFormat.format(fpsNode.speed) + "fps";
                    mc.fontRenderer.drawStringWithShadow(textToDraw, fpsNode.mappedX - mc.fontRenderer.getStringWidth(textToDraw), fpsNode.mappedY + 3, 0xFFAAAAAA);
                }

                // draw hover
                if (mouseX >= fpsNode.mappedX && mouseX <= fpsNode.mappedX + fpsNode.size && mouseY >= this.getY() && mouseY <= this.getY() + this.getH()) {
                    // hover bar
                    RenderUtil.drawRect(fpsNode.mappedX - fpsNode.size, this.getY(), fpsNode.mappedX + fpsNode.size, this.getY() + this.getH(), 0x40101010);
                    // hover red dot
                    RenderUtil.drawRect(fpsNode.mappedX - fpsNode.size, fpsNode.mappedY - fpsNode.size, fpsNode.mappedX + fpsNode.size, fpsNode.mappedY + fpsNode.size, 0xFFFF0000);

                    // set hovered data
                    hoveredData = String.format("FPS: %s", decimalFormat.format(fpsNode.speed));
                }

                lastNode = fpsNode;
            }

            if (this.isMouseInside(mouseX, mouseY)) { // mouse is inside
                // draw delay
                mc.fontRenderer.drawStringWithShadow(this.delay.getValue() + "ms", this.getX() + 2, this.getY() + this.getH() - mc.fontRenderer.FONT_HEIGHT - 1, 0xFFAAAAAA);
            }

            // draw hovered data
            if (!hoveredData.equals("")) {
                mc.fontRenderer.drawStringWithShadow(hoveredData, this.getX() + 2, this.getY() + this.getH() - mc.fontRenderer.FONT_HEIGHT * 2 - 1, 0xFFAAAAAA);
            }

            // disable scissor
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            // border
            RenderUtil.drawBorderedRectBlurred(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 2.0f, 0x00000000, 0x90101010);
        } else {
            mc.fontRenderer.drawStringWithShadow("(movement)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);
        if (this.isMouseInside(mouseX, mouseY) && button == 1/* right click */) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                this.delay.setValue(this.delay.getValue() + this.delay.getInc());
            } else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                this.delay.setValue(this.delay.getValue() - 1.0f);
            } else {
                this.delay.setValue(this.delay.getValue() - this.delay.getInc());
            }

            if (this.delay.getValue() <= this.delay.getMin() || this.delay.getValue() > this.delay.getMax())
                this.delay.setValue(500.0f);
        }
    }

    public float getAverageHeight() {
        float totalSpeed = 0;

        for (int i = this.fpsNodes.size() - 1; i > 0; i--) {
            final FpsNode fpsNode = this.fpsNodes.get(i);
            if (this.fpsNodes.size() > 11) {
                if (fpsNode != null && (i > this.fpsNodes.size() - 10)) {
                    totalSpeed += fpsNode.speed;
                }
            }
        }

        return totalSpeed / 10;
    }

    static class FpsNode {

        public float size = 0.5f;
        public float speed = 0.0f;
        public Color color;

        public float mappedX, mappedY;

        public FpsNode(float speed) {
            this.speed = speed;
            this.color = new Color(255, 255, 255);
        }

        public FpsNode() {

        }
    }
}

