package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.gui.hud.modulelist.EventUIListValueChanged;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author noil
 */
public final class ItemsComponent extends HudComponent {

    private final TextComponent searchBox;
    private final List<Item> items = new ArrayList<>();
    private List<Item> displayedItems;

    private Value<List<Item>> value;

    public ItemsComponent(Value<List<Item>> value) {
        super(value.getName());
        this.value = value;
        this.searchBox = new TextComponent(value.getName(), "...", false);
        this.setW(80);
        this.setH(80);
        this.searchBox.setW(this.getW());
        this.searchBox.setH(10);

        this.items.clear();
        for (Item item : Item.REGISTRY) {
            if (item.getRegistryName() != null) {
                if (Block.REGISTRY.containsKey(item.getRegistryName())) {
                    continue;
                }
            }

            if (Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(item)) == Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getMissingModel()) {
                continue;
            }

            this.items.add(item);
        }

        this.displayedItems = new ArrayList<>(items);
    }


    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (this.searchBox.focused) {
            this.setH(82);
            // background
            RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xFF202020);
        } else {
            this.setH(10);
        }

        this.searchBox.setX(this.getX());
        this.searchBox.setY(this.getY());
        this.searchBox.setW(this.getW());
        this.searchBox.setH(10);
        this.searchBox.render(mouseX, mouseY, partialTicks);

        if (this.items.size() > 0 && this.searchBox.focused) {
            final float renderPaddingX = 1.0f;
            final float renderPaddingY = this.searchBox.getH() + 1.0f;
            int xOffset = 0;
            int yOffset = 0;
            int counter = 0;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            for (Item item : this.displayedItems) {
                if (yOffset <= 56) {
                    int color = 0xFF303030;
                    int borderColor = 0xFF404040;
                    if (this.value.getValue().contains(item)) {
                        color = 0xFF3B005F;
                        borderColor = 0xFF9900EE;
                    }
                    final ItemStack itemStack = new ItemStack(item);
                    RenderUtil.drawBorderedRect(renderPaddingX + this.getX() + xOffset, renderPaddingY + this.getY() + yOffset, renderPaddingX + this.getX() + xOffset + 16, renderPaddingY + this.getY() + yOffset + 16, 0.5f, color, borderColor);
                    Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, (int) renderPaddingX + (int) this.getX() + xOffset, (int) renderPaddingY + (int) this.getY() + yOffset);
                    Minecraft.getMinecraft().getRenderItem().renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, itemStack, (int) renderPaddingX + (int) this.getX() + xOffset, (int) renderPaddingY + (int) this.getY() + yOffset, null);
                }

                xOffset += 18;
                counter++;
                if (counter >= ((this.getW() - 28) / 16)) {
                    xOffset = 0;
                    yOffset += 18;
                    counter = 0;
                }
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int button) {
        super.mouseClickMove(mouseX, mouseY, button);
        this.searchBox.mouseClickMove(mouseX, mouseY, button);
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        super.mouseClick(mouseX, mouseY, button);

        if (this.searchBox.displayValue.equals("..."))
            this.searchBox.displayValue = "";

        this.searchBox.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (this.searchBox.focused) {
            final float renderPaddingX = 1.0f;
            final float renderPaddingY = this.searchBox.getH() + 1.0f;
            if (this.isMouseInside(mouseX, mouseY) && mouseY > this.getY() + renderPaddingY) {
                int xOffset = 0;
                int yOffset = 0;
                int counter = 0;

                for (Item item : this.displayedItems) {
                    if (yOffset <= 56) {
                        if (mouseX > renderPaddingX + this.getX() + xOffset && mouseX < renderPaddingX + this.getX() + xOffset + 16 && mouseY > renderPaddingY + this.getY() + yOffset && mouseY < renderPaddingY + this.getY() + yOffset + 16) {
                            if (this.value.getValue().contains(item))
                                this.value.getValue().remove(item);
                            else
                                this.value.getValue().add(item);

                            Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventUIListValueChanged());
                        }
                    }

                    xOffset += 18;
                    counter++;
                    if (counter >= ((this.getW() - 28) / 16)) {
                        xOffset = 0;
                        yOffset += 18;
                        counter = 0;
                    }
                }
                return;
            }
        }

        this.searchBox.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        this.searchBox.keyTyped(typedChar, keyCode);

        if (this.searchBox.displayValue.equals("") && this.displayedItems.size() != 0) {
            this.displayedItems.clear();
            this.displayedItems.addAll(this.items);
        } else {
            this.displayedItems.clear();
        }

        for (Item item : this.items) {
            if (CharUtils.isAsciiNumeric(typedChar) && NumberUtils.isDigits(this.searchBox.displayValue)) {
                final int itemID = Item.getIdFromItem(item);
                if (itemID == Integer.parseInt(this.searchBox.displayValue)) {
                    if (!this.displayedItems.contains(item)) {
                        this.displayedItems.add(item);
                    }
                }
            } else {
                final ResourceLocation registryName = item.getRegistryName();
                if (registryName != null) {
                    if (registryName.toString().contains(this.searchBox.displayValue)) {
                        if (!this.displayedItems.contains(item)) {
                            this.displayedItems.add(item);
                        }
                    }
                }
            }
        }

        for (Item valueItem : this.value.getValue()) {
            if (!this.displayedItems.contains(valueItem)) {
                this.displayedItems.add(valueItem);
            }
        }
    }
}
