package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ColorUtil;
import me.rigamortis.seppuku.api.util.GLUProjection;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/17/2019 @ 8:45 PM.
 */
public final class StorageESPModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Rendering mode", Mode.THREE_D);

    private enum Mode {
        TWO_D, THREE_D // TWO_DIMENSIONAL, THREE_DIMENSIONAL
    }

    public final Value<Boolean> nametag = new Value<Boolean>("Nametag", new String[]{"Nametag", "Tag", "Tags", "Ntag", "name", "names"}, "Renders the name of the drawn storage object.", false);
    public final Value<Integer> opacity = new Value<Integer>("Opacity", new String[]{"Opacity", "Transparency", "Alpha"}, "Opacity of the rendered esp.", 128, 0, 255, 1);

    private final ICamera camera = new Frustum();

    public StorageESPModule() {
        super("Storage", new String[]{"StorageESP", "ChestFinder", "ChestESP"}, "Highlights different types of storage entities.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void render2D(EventRender2D event) {
        if (this.mode.getValue() == Mode.THREE_D && !this.nametag.getValue()) // if 3D and names are off, return
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        for (TileEntity te : mc.world.loadedTileEntityList) {
            if (te != null) {
                if (this.isTileStorage(te)) {
                    final AxisAlignedBB bb = this.boundingBoxForEnt(te);
                    if (bb != null) {
                        final float[] bounds = this.convertBounds(bb, event.getScaledResolution().getScaledWidth(), event.getScaledResolution().getScaledHeight());
                        if (bounds != null) {
                            if (this.mode.getValue() == Mode.TWO_D) { // 2D
                                RenderUtil.drawOutlineRect(bounds[0], bounds[1], bounds[2], bounds[3], 1.5f, ColorUtil.changeAlpha(0xAA000000, this.opacity.getValue()));
                                RenderUtil.drawOutlineRect(bounds[0] - 0.5f, bounds[1] - 0.5f, bounds[2] + 0.5f, bounds[3] + 0.5f, 0.5f, ColorUtil.changeAlpha(this.getColor(te), this.opacity.getValue()));
                            }

                            if (this.nametag.getValue()) {
                                final String name = te.getBlockType().getLocalizedName();
                                GL11.glEnable(GL11.GL_BLEND);
                                mc.fontRenderer.drawStringWithShadow(name, bounds[0] + (bounds[2] - bounds[0]) / 2 - mc.fontRenderer.getStringWidth(name) / 2, bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 1, ColorUtil.changeAlpha(0xFFFFFFFF, this.opacity.getValue()));
                                GL11.glDisable(GL11.GL_BLEND);
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void render3D(EventRender3D event) {
        if (this.mode.getValue() == Mode.THREE_D) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.getRenderViewEntity() == null)
                return;

            RenderUtil.begin3D();
            for (TileEntity te : mc.world.loadedTileEntityList) {
                if (te != null) {
                    if (this.isTileStorage(te)) {
                        final AxisAlignedBB bb = this.boundingBoxForEnt(te);
                        if (bb != null) {
                            //RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(this.getColor(te), this.opacity.getValue()));
                            //RenderUtil.drawBoundingBox(bb, 1.5f, ColorUtil.changeAlpha(this.getColor(te), this.opacity.getValue()));
                            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                                    bb.minY + mc.getRenderManager().viewerPosY,
                                    bb.minZ + mc.getRenderManager().viewerPosZ,
                                    bb.maxX + mc.getRenderManager().viewerPosX,
                                    bb.maxY + mc.getRenderManager().viewerPosY,
                                    bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                                RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(this.getColor(te), this.opacity.getValue()));
                                RenderUtil.drawBoundingBox(bb, 1.5f, ColorUtil.changeAlpha(this.getColor(te), this.opacity.getValue()));
                            }
                        }
                    }
                }
            }
            RenderUtil.end3D();
        }
    }

    private boolean isTileStorage(TileEntity te) {
        if (te instanceof TileEntityChest) {
            return true;
        }
        if (te instanceof TileEntityDropper) {
            return true;
        }
        if (te instanceof TileEntityDispenser) {
            return true;
        }
        if (te instanceof TileEntityFurnace) {
            return true;
        }
        if (te instanceof TileEntityBrewingStand) {
            return true;
        }
        if (te instanceof TileEntityEnderChest) {
            return true;
        }
        if (te instanceof TileEntityHopper) {
            return true;
        }
        if (te instanceof TileEntityShulkerBox) {
            return true;
        }
        return false;
    }

    private AxisAlignedBB boundingBoxForEnt(TileEntity te) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (te != null) {
            if (te instanceof TileEntityChest) {
                TileEntityChest chest = (TileEntityChest) te;
                if (chest.adjacentChestXNeg != null) {
                    return new AxisAlignedBB(
                            te.getPos().getX() + 0.0625d - 1 - mc.getRenderManager().viewerPosX,
                            te.getPos().getY() - mc.getRenderManager().viewerPosY,
                            te.getPos().getZ() + 0.0625d - mc.getRenderManager().viewerPosZ,

                            te.getPos().getX() + 0.9375d - mc.getRenderManager().viewerPosX,
                            te.getPos().getY() + 0.875d - mc.getRenderManager().viewerPosY,
                            te.getPos().getZ() + 0.9375d - mc.getRenderManager().viewerPosZ);
                } else if (chest.adjacentChestZPos != null) {
                    return new AxisAlignedBB(
                            te.getPos().getX() + 0.0625d - mc.getRenderManager().viewerPosX,
                            te.getPos().getY() - mc.getRenderManager().viewerPosY,
                            te.getPos().getZ() + 0.0625d - mc.getRenderManager().viewerPosZ,

                            te.getPos().getX() + 0.9375d - mc.getRenderManager().viewerPosX,
                            te.getPos().getY() + 0.875d - mc.getRenderManager().viewerPosY,
                            te.getPos().getZ() + 0.9375d + 1 - mc.getRenderManager().viewerPosZ);
                } else if (chest.adjacentChestXPos == null && chest.adjacentChestZNeg == null) {
                    return new AxisAlignedBB(
                            te.getPos().getX() + 0.0625d - mc.getRenderManager().viewerPosX,
                            te.getPos().getY() - mc.getRenderManager().viewerPosY,
                            te.getPos().getZ() + 0.0625d - mc.getRenderManager().viewerPosZ,

                            te.getPos().getX() + 0.9375d - mc.getRenderManager().viewerPosX,
                            te.getPos().getY() + 0.875d - mc.getRenderManager().viewerPosY,
                            te.getPos().getZ() + 0.9375d - mc.getRenderManager().viewerPosZ);
                }
            } else if (te instanceof TileEntityEnderChest) {
                return new AxisAlignedBB(
                        te.getPos().getX() + 0.0625d - mc.getRenderManager().viewerPosX,
                        te.getPos().getY() - mc.getRenderManager().viewerPosY,
                        te.getPos().getZ() + 0.0625d - mc.getRenderManager().viewerPosZ,

                        te.getPos().getX() + 0.9375d - mc.getRenderManager().viewerPosX,
                        te.getPos().getY() + 0.875d - mc.getRenderManager().viewerPosY,
                        te.getPos().getZ() + 0.9375d - mc.getRenderManager().viewerPosZ);
            } else {
                return new AxisAlignedBB(
                        te.getPos().getX() - mc.getRenderManager().viewerPosX,
                        te.getPos().getY() - mc.getRenderManager().viewerPosY,
                        te.getPos().getZ() - mc.getRenderManager().viewerPosZ,

                        te.getPos().getX() + 1 - mc.getRenderManager().viewerPosX,
                        te.getPos().getY() + 1 - mc.getRenderManager().viewerPosY,
                        te.getPos().getZ() + 1 - mc.getRenderManager().viewerPosZ);
            }
        }

        return null;
    }


    private int getColor(TileEntity te) {
        if (te instanceof TileEntityChest) {
            return 0xFFFFC417;
        }
        if (te instanceof TileEntityDropper) {
            return 0xFF4E4E4E;
        }
        if (te instanceof TileEntityDispenser) {
            return 0xFF4E4E4E;
        }
        if (te instanceof TileEntityHopper) {
            return 0xFF4E4E4E;
        }
        if (te instanceof TileEntityFurnace) {
            return 0xFF2D2D2D;
        }
        if (te instanceof TileEntityBrewingStand) {
            return 0xFF17B9D2;
        }
        if (te instanceof TileEntityEnderChest) {
            return 0xFF17A25C;
        }
        if (te instanceof TileEntityShulkerBox) {
            final TileEntityShulkerBox shulkerBox = (TileEntityShulkerBox) te;
            return (255 << 24) | shulkerBox.getColor().getColorValue();
        }
        return 0xFFFFFFFF;
    }

    private float[] convertBounds(AxisAlignedBB bb, int width, int height) {
        float x = -1;
        float y = -1;
        float w = width + 1;
        float h = height + 1;

        camera.setPosition(Minecraft.getMinecraft().getRenderViewEntity().posX, Minecraft.getMinecraft().getRenderViewEntity().posY, Minecraft.getMinecraft().getRenderViewEntity().posZ);

        if (!camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + Minecraft.getMinecraft().getRenderManager().viewerPosX,
                bb.minY + Minecraft.getMinecraft().getRenderManager().viewerPosY,
                bb.minZ + Minecraft.getMinecraft().getRenderManager().viewerPosZ,
                bb.maxX + Minecraft.getMinecraft().getRenderManager().viewerPosX,
                bb.maxY + Minecraft.getMinecraft().getRenderManager().viewerPosY,
                bb.maxZ + Minecraft.getMinecraft().getRenderManager().viewerPosZ))) {
            return null;
        }

        final Vec3d corners[] = {
                new Vec3d(bb.minX, bb.minY, bb.minZ),
                new Vec3d(bb.maxX, bb.maxY, bb.maxZ),
                new Vec3d(bb.minX, bb.maxY, bb.maxZ),
                new Vec3d(bb.minX, bb.minY, bb.maxZ),
                new Vec3d(bb.maxX, bb.minY, bb.maxZ),
                new Vec3d(bb.maxX, bb.minY, bb.minZ),
                new Vec3d(bb.maxX, bb.maxY, bb.minZ),
                new Vec3d(bb.minX, bb.maxY, bb.minZ)
        };

        for (Vec3d vec : corners) {
            final GLUProjection.Projection projection = GLUProjection.getInstance().project(vec.x, vec.y, vec.z, GLUProjection.ClampMode.NONE, true);

            x = Math.max(x, (float) projection.getX());
            y = Math.max(y, (float) projection.getY());

            w = Math.min(w, (float) projection.getX());
            h = Math.min(h, (float) projection.getY());
        }

        if (x != -1 && y != -1 && w != width + 1 && h != height + 1) {
            return new float[]{x, y, w, h};
        }

        return null;
    }

}
