package me.rigamortis.seppuku.impl.gui.hud.component;

import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

/**
 * @author cookiedragon234 10/Dec/2019
 */
public class PlayerPreviewComponent extends DraggableHudComponent {
	
	private static final int INVENTORY_WIDTH = 49;
	private static final int INVENTORY_HEIGHT = 70;
	
	public PlayerPreviewComponent() {
		super("Player Preview");
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
		
		final Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer ent = mc.player;
		
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1);
		GlStateManager.enableAlpha();
		//GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableDepth();
		
		//GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(getX() + getW(), getY() + getH(), 50.0F);
		GlStateManager.scale((-50), 50, 50);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-((float)Math.atan((double)(getY() / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(0.0F, 0.0F, 0.0F);
		RenderManager renderManager = mc.getRenderManager();
		renderManager.setPlayerViewY(180.0F);
		renderManager.setRenderShadow(false);
		renderManager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
		renderManager.setRenderShadow(true);
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		//GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		//GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		
		//GlStateManager.depthFunc(GL11.GL_LEQUAL);
		//GlStateManager.disableDepth();
		GlStateManager.disableAlpha();
		GlStateManager.popMatrix();
		
		this.setW(INVENTORY_WIDTH);
		this.setH(INVENTORY_HEIGHT);
	}
}
