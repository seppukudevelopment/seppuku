package me.rigamortis.seppuku.api.gui.hud.component;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.gui.hud.modulelist.EventUIListValueChanged;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author noil
 */
public final class BlocksComponent extends HudComponent {

    private final TextComponent searchBox;
    private final List<Block> blocks = new ArrayList<>();
    private List<Block> displayedBlocks;

    private Value<List<Block>> value;

    public BlocksComponent(Value<List<Block>> value) {
        super(value.getName());
        this.value = value;
        this.searchBox = new TextComponent(value.getName(), "...", false);
        this.setW(80);
        this.setH(80);
        this.searchBox.setW(this.getW());
        this.searchBox.setH(10);

        this.blocks.clear();
        for (Block block : Block.REGISTRY) {
            if (block instanceof BlockAir)
                continue;

            if (!(block instanceof BlockLiquid || block instanceof IFluidBlock) && Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(block)) == Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getMissingModel()) {
                continue;
            }

            this.blocks.add(block);
        }

        this.displayedBlocks = new ArrayList<>(blocks);
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

        if (this.blocks.size() > 0 && this.searchBox.focused) {
            final float renderPaddingX = 1.0f;
            final float renderPaddingY = this.searchBox.getH() + 1.0f;
            int xOffset = 0;
            int yOffset = 0;
            int counter = 0;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            for (Block block : this.displayedBlocks) {
                if (yOffset <= 56) {
                    int color = 0xFF303030;
                    int borderColor = 0xFF404040;
                    if (this.value.getValue().contains(block)) {
                        color = 0xFF3B005F;
                        borderColor = 0xFF9900EE;
                    }

                    final float rectX = renderPaddingX + this.getX() + xOffset;
                    final float rectY = renderPaddingY + this.getY() + yOffset;
                    RenderUtil.drawBorderedRect(rectX, rectY, rectX + 16, rectY + 16, 0.5f, color, borderColor);
                    if (block instanceof BlockLiquid || block instanceof IFluidBlock) {
                        final Fluid fluid = FluidRegistry.lookupFluidForBlock(block);

                        final ResourceLocation fluidStill = fluid.getStill();
                        final TextureAtlasSprite sprite;
                        if (fluidStill == null) {
                            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
                        } else {
                            // getTextureExtry (instead of getTextureEntry) is a forge typo, not a typo on our side
                            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(fluidStill.toString());
                        }

                        RenderUtil.glColor(fluid.getColor());
                        // Note that fluids are a full quad; extra padding is added so that you can see the background to check whether the block is selected or not
                        RenderUtil.drawTexture(rectX + 1, rectY + 1, 14, 14, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
                    } else {
                        final ItemStack itemStack = new ItemStack(block);
                        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, (int) renderPaddingX + (int) this.getX() + xOffset, (int) renderPaddingY + (int) this.getY() + yOffset);
                        Minecraft.getMinecraft().getRenderItem().renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, itemStack, (int) renderPaddingX + (int) this.getX() + xOffset, (int) renderPaddingY + (int) this.getY() + yOffset, null);
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

        if (this.searchBox.getText().equals("..."))
            this.searchBox.setText("");

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

                for (Block block : this.displayedBlocks) {
                    if (yOffset <= 56) {
                        if (mouseX > renderPaddingX + this.getX() + xOffset && mouseX < renderPaddingX + this.getX() + xOffset + 16 && mouseY > renderPaddingY + this.getY() + yOffset && mouseY < renderPaddingY + this.getY() + yOffset + 16) {
                            if (this.value.getValue().contains(block))
                                this.value.getValue().remove(block);
                            else
                                this.value.getValue().add(block);

                            Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventUIListValueChanged(this.value));
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

        if (this.searchBox.getText().equals("") && this.displayedBlocks.size() != 0) {
            this.displayedBlocks.clear();
            this.displayedBlocks.addAll(this.blocks);
        } else {
            this.displayedBlocks.clear();
        }

        for (Block block : this.blocks) {
            if (CharUtils.isAsciiNumeric(typedChar) && NumberUtils.isDigits(this.searchBox.getText())) {
                final int blockID = Block.getIdFromBlock(block);
                if (blockID == Integer.parseInt(this.searchBox.getText())) {
                    if (!this.displayedBlocks.contains(block)) {
                        this.displayedBlocks.add(block);
                    }
                }
            } else {
                final ResourceLocation registryName = block.getRegistryName();
                if (registryName != null) {
                    if (registryName.toString().contains(this.searchBox.getText())) {
                        if (!this.displayedBlocks.contains(block)) {
                            this.displayedBlocks.add(block);
                        }
                    }
                }
            }
        }

        for (Block valueBlock : this.value.getValue()) {
            if (!this.displayedBlocks.contains(valueBlock)) {
                this.displayedBlocks.add(valueBlock);
            }
        }
    }
}
