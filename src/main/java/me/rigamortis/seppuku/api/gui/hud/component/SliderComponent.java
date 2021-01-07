package me.rigamortis.seppuku.api.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.config.ModuleConfig;
import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;

/**
 * SliderComponent
 * - strictly for values atm (v3.1), will be used around the entire hud later on.
 *
 * @author noil
 */
public final class SliderComponent extends HudComponent {

    private Value value;
    private ComponentListener mouseClickListener;
    private final SliderBarComponent sliderBar;
    private TextComponent textComponent;

    protected final DecimalFormat decimalFormat = new DecimalFormat("#.#");
    protected boolean sliding = false;
    protected float lastPositionX = -1;

    public SliderComponent(String name, Value value) {
        super(name);
        this.sliding = false;
        this.value = value;
        this.sliderBar = new SliderBarComponent(4, 9, 192, this);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (isMouseInside(mouseX, mouseY))
            RenderUtil.drawGradientRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x30909090, 0x00101010);

        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x45202020);

        if (this.textComponent == null) {
            if (this.sliderBar != null) {
                this.sliderBar.parent.setX(this.getX());
                this.sliderBar.parent.setY(this.getY());
                this.sliderBar.parent.setW(this.getW());
                this.sliderBar.parent.setH(this.getH());

                if (!this.sliding) {
                    this.sliderBar.setDragging(false);
                    if (this.getX() != this.lastPositionX) {
                        this.sliderBar.updatePositionToValue();
                        this.lastPositionX = this.getX();
                    }
                } else {
                    if (mouseX < this.getX() || mouseX > this.getX() + this.getW()) { // mouse must be inside X at all times to slide
                        this.sliding = false;
                        this.sliderBar.setDragging(false);
                        this.sliderBar.setValueFromPosition();
                    }
                }

                this.sliderBar.render(mouseX, mouseY, partialTicks);
            }

            Minecraft.getMinecraft().fontRenderer.drawString(this.getName(), (int) this.getX() + 1, (int) this.getY() + 1, 0xFFAAAAAA);

            String displayedValue = this.decimalFormat.format(this.value.getValue());
            if (this.sliding) {
                final String draggedValue = this.sliderBar.getValueFromPosition();
                if (draggedValue != null)
                    displayedValue = draggedValue;
            }

            Minecraft.getMinecraft().fontRenderer.drawString(displayedValue, (int) (this.getX() + this.getW()) - Minecraft.getMinecraft().fontRenderer.getStringWidth(displayedValue) - 1, (int) this.getY() + 1, 0xFFAAAAAA);
        } else {
            this.textComponent.setX(this.getX());
            this.textComponent.setY(this.getY());
            this.textComponent.setW(this.getW());
            this.textComponent.setH(this.getH());
            this.textComponent.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        super.mouseClick(mouseX, mouseY, button);

        if (!this.isMouseInside(mouseX, mouseY))
            return;

        if (button == 0) {
            if (this.textComponent == null) {
                this.sliding = true;
                this.sliderBar.mouseClick(mouseX, mouseY, button);
            } else {
                this.textComponent.mouseClick(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int button) {
        super.mouseClickMove(mouseX, mouseY, button);

        if (!this.isMouseInside(mouseX, mouseY))
            return;

        if (button == 0) {
            if (this.textComponent == null) {
                this.sliderBar.mouseClickMove(mouseX, mouseY, button);
            } else {
                this.textComponent.mouseClickMove(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (!this.isMouseInside(mouseX, mouseY)) {
            if (this.textComponent != null)
                this.textComponent = null;

            this.sliding = false;
            return;
        }

        switch (button) {
            case 0:
                if (this.textComponent == null) {
                    if (mouseClickListener != null)
                        mouseClickListener.onComponentEvent();

                    if (this.sliding) {
                        this.sliderBar.mouseRelease(mouseX, mouseY, button);
                    }
                } else {
                    if (this.textComponent.onCheckButtonPress(mouseX, mouseY)) {
                        this.textComponent = null;
                        return;
                    }

                    this.textComponent.mouseRelease(mouseX, mouseY, button);
                }
                break;
            case 1:
                if (this.textComponent == null) {
                    TextComponent valueNumberText = new TextComponent(value.getName(), value.getValue().toString(), true);
                    valueNumberText.setTooltipText(value.getDesc() + " " + ChatFormatting.GRAY + "(" + value.getMin() + " - " + value.getMax() + ")");
                    valueNumberText.returnListener = new ComponentListener() {
                        @Override
                        public void onComponentEvent() {
                            try {
                                if (value.getValue() instanceof Integer) {
                                    value.setValue(Integer.parseInt(valueNumberText.displayValue));
                                } else if (value.getValue() instanceof Double) {
                                    value.setValue(Double.parseDouble(valueNumberText.displayValue));
                                } else if (value.getValue() instanceof Float) {
                                    value.setValue(Float.parseFloat(valueNumberText.displayValue));
                                } else if (value.getValue() instanceof Long) {
                                    value.setValue(Long.parseLong(valueNumberText.displayValue));
                                } else if (value.getValue() instanceof Byte) {
                                    value.setValue(Byte.parseByte(valueNumberText.displayValue));
                                }
                                Seppuku.INSTANCE.getConfigManager().save(ModuleConfig.class); // save module configs
                            } catch (NumberFormatException e) {
                                Seppuku.INSTANCE.logfChat("%s - %s: Invalid number format.", getName(), value.getName());
                            }
                        }
                    };
                    valueNumberText.focus();
                    this.textComponent = valueNumberText;
                } else {
                    this.textComponent = null;
                }
                break;
        }

        this.sliding = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        if (this.textComponent != null) {
            this.textComponent.keyTyped(typedChar, keyCode);

            if (!this.textComponent.focused) {
                this.textComponent = null;
                this.sliderBar.updatePositionToValue();
            }
        }
    }

    public static class SliderBarComponent extends DraggableHudComponent {
        public int width, height, alpha;
        public SliderComponent parent;

        public SliderBarComponent(int width, int height, int alpha, SliderComponent parent) {
            super("SliderBar");
            this.width = width;
            this.height = height;
            this.alpha = alpha;
            this.parent = parent;
            this.setX(parent.getX());
            this.setY(parent.getY());
            this.setW(width);
            this.setH(height);
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            super.render(mouseX, mouseY, partialTicks);

            this.clampSlider();

            RenderUtil.drawRect(this.parent.getX(), this.getY(), this.getX(), this.getY() + this.getH(), ColorUtil.changeAlpha(0x453B005F, this.alpha));
            RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), ColorUtil.changeAlpha(0xFFC255FF, this.alpha));
        }

        @Override
        public void mouseClick(int mouseX, int mouseY, int button) {
            super.mouseClick(mouseX, mouseY, button);

            this.setX(mouseX);
            this.setDragging(true);
            this.clampSlider();
        }

        @Override
        public void mouseRelease(int mouseX, int mouseY, int button) {
            super.mouseRelease(mouseX, mouseY, button);

            if (this.isMouseInside(mouseX, mouseY)) {
                if (button == 0) {
                    this.setValueFromPosition();
                    this.setDragging(false);
                }
            }
        }

        private void clampSlider() {
            if (this.getX() < this.parent.getX())
                this.setX(this.parent.getX());

            if (this.getY() < this.parent.getY())
                this.setY(this.parent.getY());

            if (this.getX() > (this.parent.getX() + this.parent.getW()) - this.getW())
                this.setX((this.parent.getX() + this.parent.getW()) - this.getW());

            if ((this.getY() + getH()) > (this.parent.getY() + this.parent.getH()))
                this.setY((this.parent.getY() + this.parent.getH()) - this.getH());
        }

        public void updatePositionToValue() {
            this.setX(this.getPositionFromValue());
        }

        /*
        //TODO: below this line is ugly and i want to do it better, but it works for our value system right now -noil
         */

        private float getPositionFromValue() {
            float position = -1;

            if (this.parent.value.getValue() instanceof Integer) {
                final int finishedInt = (int) MathUtil.map((int) this.parent.value.getValue(), (int) this.parent.value.getMin(), (int) this.parent.value.getMax(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW());
                position = (float) finishedInt;
            } else if (this.parent.value.getValue() instanceof Double) {
                position = (float) MathUtil.map((double) this.parent.value.getValue(), (double) this.parent.value.getMin(), (double) this.parent.value.getMax(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW());
            } else if (this.parent.value.getValue() instanceof Float) {
                position = (float) MathUtil.map((float) this.parent.value.getValue(), (float) this.parent.value.getMin(), (float) this.parent.value.getMax(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW());
            } else if (this.parent.value.getValue() instanceof Long) {
                position = (float) (long) MathUtil.map((long) this.parent.value.getValue(), (long) this.parent.value.getMin(), (long) this.parent.value.getMax(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW());
            } else if (this.parent.value.getValue() instanceof Byte) {
                position = (float) MathUtil.map((byte) this.parent.value.getValue(), (byte) this.parent.value.getMin(), (byte) this.parent.value.getMax(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW());
            }

            return position;
        }

        private String getValueFromPosition() {
            if (this.parent.value.getValue() instanceof Integer) {
                return (int) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (int) this.parent.value.getMin(), (int) this.parent.value.getMax()) + "";
            } else if (this.parent.value.getValue() instanceof Double) {
                return this.parent.decimalFormat.format((double) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (double) this.parent.value.getMin(), (double) this.parent.value.getMax()));
            } else if (this.parent.value.getValue() instanceof Float) {
                return this.parent.decimalFormat.format((float) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (float) this.parent.value.getMin(), (float) this.parent.value.getMax()));
            } else if (this.parent.value.getValue() instanceof Long) {
                return this.parent.decimalFormat.format((long) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (long) this.parent.value.getMin(), (long) this.parent.value.getMax()));
            } else if (this.parent.value.getValue() instanceof Byte) {
                return this.parent.decimalFormat.format((byte) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (byte) this.parent.value.getMin(), (byte) this.parent.value.getMax()));
            }

            return "?";
        }

        private void setValueFromPosition() {
            if (this.parent.value.getValue() instanceof Integer) {
                final int finishedInt = (int) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (int) this.parent.value.getMin(), (int) this.parent.value.getMax());
                this.parent.value.setValue(finishedInt);
            } else if (this.parent.value.getValue() instanceof Double) {
                final double finishedDouble = (double) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (double) this.parent.value.getMin(), (double) this.parent.value.getMax());
                this.parent.value.setValue(Double.valueOf(this.parent.decimalFormat.format(finishedDouble)));
            } else if (this.parent.value.getValue() instanceof Float) {
                final float finishedFloat = (float) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (float) this.parent.value.getMin(), (float) this.parent.value.getMax());
                this.parent.value.setValue(Float.valueOf(this.parent.decimalFormat.format(finishedFloat)));
            } else if (this.parent.value.getValue() instanceof Long) {
                final long finishedLong = (long) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (long) this.parent.value.getMin(), (long) this.parent.value.getMax());
                this.parent.value.setValue(Long.valueOf(this.parent.decimalFormat.format(finishedLong)));
            } else if (this.parent.value.getValue() instanceof Byte) {
                final byte finishedByte = (byte) MathUtil.map(this.getX(), this.parent.getX(), this.parent.getX() + this.parent.getW() - this.getW(), (byte) this.parent.value.getMin(), (byte) this.parent.value.getMax());
                this.parent.value.setValue(finishedByte);
            }
        }
    }
}
