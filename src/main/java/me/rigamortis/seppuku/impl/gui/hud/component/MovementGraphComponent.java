package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.hud.component.ResizableHudComponent;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author noil
 */
public final class MovementGraphComponent extends ResizableHudComponent {

    public final Value<Float> delay = new Value<Float>("Delay", new String[]{"Del"}, "The amount of delay(ms) between updates.", 20.0f, 0.0f, 90.0f, 10.0f);

    private final List<MovementNode> movementNodes = new CopyOnWriteArrayList<MovementNode>();
    private final Timer timer = new Timer();

    public MovementGraphComponent() {
        super("MovementGraph", 60, 27);
        this.setW(60);
        this.setH(27);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null && mc.world != null && mc.getCurrentServerData() != null) {
            final ScaledResolution sr = new ScaledResolution(mc);
            final DecimalFormat decimalFormat = new DecimalFormat("###.##");

            if (this.movementNodes.size() > (this.getW() / 2)) { // overflow protection
                this.movementNodes.clear();
            }

            if (this.timer.passed(this.delay.getValue())) {
                if (this.movementNodes.size() > (this.getW() / 2 - 1)) {
                    this.movementNodes.remove(0); // remove oldest
                }

                final double deltaX = mc.player.posX - mc.player.prevPosX;
                final double deltaZ = mc.player.posZ - mc.player.prevPosZ;
                final float tickRate = (mc.timer.tickLength / 1000.0f);
                float bps = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) / tickRate;
                /*if (bps < 2)
                    bps = 2;*/

                this.movementNodes.add(new MovementNode(bps));

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
            MovementNode lastNode = null;
            for (int i = 0; i < this.movementNodes.size(); i++) {
                final MovementNode movementNode = this.movementNodes.get(i);

                final float mappedX = (float) MathUtil.map((this.getW() / 2 - 1) - i, 0, (this.getW() / 2 - 1), this.getX() + this.getW() - 1, this.getX() + 1);
                final float mappedY = (float) MathUtil.map(movementNode.speed, -2.0f, this.getAverageHeight(), this.getY() + this.getH() - 1, this.getY() + 1) + this.getH() / 2;
                movementNode.mappedX = mappedX;
                movementNode.mappedY = mappedY;

                // gradient of bar
                //RenderUtil.drawGradientRect(mappedX - movementNode.size, mappedY, mappedX + movementNode.size, this.getY() + this.getH(), movementNode.color.getRGB(), 0x00000000);
                // rect on top of bar
                if (lastNode != null) {
                    RenderUtil.drawLine(movementNode.mappedX, movementNode.mappedY, lastNode.mappedX, lastNode.mappedY, 1.0f, -1);
                }

                // draw dot
                RenderUtil.drawRect(mappedX - movementNode.size, mappedY, mappedX + movementNode.size, mappedY + movementNode.size, movementNode.color.getRGB());

                // draw text
                if (i == this.movementNodes.size() - 1) {
                    final String textToDraw = decimalFormat.format(movementNode.speed) + "bps";
                    mc.fontRenderer.drawStringWithShadow(textToDraw, mappedX - mc.fontRenderer.getStringWidth(textToDraw), mappedY + 3, 0xFFAAAAAA);
                }

                // draw hover
                if (mouseX >= mappedX && mouseX <= mappedX + movementNode.size && mouseY >= this.getY() && mouseY <= this.getY() + this.getH()) {
                    // hover bar
                    RenderUtil.drawRect(mappedX - movementNode.size, this.getY(), mappedX + movementNode.size, this.getY() + this.getH(), 0x40101010);
                    // hover red dot
                    RenderUtil.drawRect(mappedX - movementNode.size, mappedY, mappedX + movementNode.size, mappedY + movementNode.size, 0xFFFF0000);

                    // set hovered data
                    hoveredData = String.format("Speed: %s", decimalFormat.format(movementNode.speed));
                }

                lastNode = movementNode;
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
                this.delay.setValue(40.0f);
        }
    }

    public float getAverageHeight() {
        float totalSpeed = 0;

        for (int i = this.movementNodes.size() - 1; i > 0; i--) {
            final MovementNode movementNode = this.movementNodes.get(i);
            if (this.movementNodes.size() > 11) {
                if (movementNode != null && (i > this.movementNodes.size() - 10)) {
                    totalSpeed += movementNode.speed;
                }
            }
        }

        return totalSpeed / 10;
    }

    private class MovementNode {

        public float size = 1.0f;
        public float speed = 0.0f;
        public Color color;

        public float mappedX, mappedY;

        public MovementNode(float speed) {
            this.speed = speed;
            this.color = new Color(255, 255, 255);
        }
    }
}
