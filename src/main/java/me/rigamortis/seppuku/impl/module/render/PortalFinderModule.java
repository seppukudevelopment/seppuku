package me.rigamortis.seppuku.impl.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRender3D;
import me.rigamortis.seppuku.api.event.world.EventChunk;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.GLUProjection;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * created by noil on 11/3/2019 at 1:55 PM
 */
public final class PortalFinderModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode"}, "Rendering mode to use for drawing found portals.", Mode.TWO_DIMENSIONAL);

    private enum Mode {
        TWO_DIMENSIONAL, THREE_DIMENSIONAL
    }

    public final Value<Boolean> chat = new Value<Boolean>("Chat", new String[]{"Chat", "ChatMessages", "ChatNotifications"}, "Display a message in chat when a portal is found (Hover the message for more info).", true);

    public final Value<Boolean> remove = new Value<Boolean>("Remove", new String[]{"R", "Delete"}, "Removes a portal from being drawn if the player is a distance aways from it.", true);
    public final Value<Integer> removeDistance = new Value<Integer>("RemoveDistance", new String[]{"RD", "RemoveRange"}, "Minimum distance in blocks the player must be from a portal for it to stop being drawn.", 200, 1, 2000, 1);

    public final Value<Boolean> showInfo = new Value<Boolean>("ShowInfo", new String[]{"SI", "DrawInfo", "DrawText"}, "Draws information about the portal at it's location.", true);
    public final Value<Float> infoScale = new Value<Float>("InfoScale", new String[]{"IS", "Scale", "TextScale"}, "Scale of the text size on the drawn information.", 1.0f, 0.0f, 3.0f, 0.25f);

    public final Value<Float> width = new Value<Float>("Width", new String[]{"W", "Width"}, "Width of each line that is drawn to indicate a portal's location.", 0.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Float> red = new Value<Float>("Red", new String[]{"R"}, "Red value for each drawn line.", 255.0f, 0.0f, 255.0f, 1.0f);
    public final Value<Float> green = new Value<Float>("Green", new String[]{"G"}, "Green value for each drawn line.", 255.0f, 0.0f, 255.0f, 1.0f);
    public final Value<Float> blue = new Value<Float>("Blue", new String[]{"B"}, "Blue value for each drawn line.", 255.0f, 0.0f, 255.0f, 1.0f);

    private final List<Vec3d> portals = new CopyOnWriteArrayList<>();

    private static final int COLOR = 0xFFFFFFFF;

    public PortalFinderModule() {
        super("PortalFinder", new String[]{"PortalFinder", "PFinder"}, "Highlights nearby portals.", "NONE", -1, Module.ModuleType.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.portals.clear();
    }

    @Listener
    public void render2D(EventRender2D event) {
        if (this.mode.getValue() == Mode.TWO_DIMENSIONAL) {
            final Minecraft mc = Minecraft.getMinecraft();

            for (Vec3d portal : this.portals) {
                final GLUProjection.Projection projection = GLUProjection.getInstance().project(portal.x - mc.getRenderManager().viewerPosX, portal.y - mc.getRenderManager().viewerPosY, portal.z - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, true);
                if (projection != null) {
                    RenderUtil.drawLine((float) projection.getX(), (float) projection.getY(), event.getScaledResolution().getScaledWidth() / 2, event.getScaledResolution().getScaledHeight() / 2, this.width.getValue(), new Color(red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f).getRGB());

                    if (this.showInfo.getValue() && projection.isType(GLUProjection.Projection.Type.INSIDE)) {
                        final float scale = this.infoScale.getValue();
                        GlStateManager.pushMatrix();
                        GlStateManager.scale(scale, scale, scale);
                        this.drawPortalInfoText(portal, (float) projection.getX() / scale, (float) projection.getY() / scale);
                        GlStateManager.scale(-scale, -scale, -scale);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }

    @Listener
    public void render3D(EventRender3D event) {
        if (this.mode.getValue() == Mode.THREE_DIMENSIONAL) {
            final Minecraft mc = Minecraft.getMinecraft();

            for (Vec3d portal : this.portals) {
                GlStateManager.pushMatrix();
                final boolean bobbing = mc.gameSettings.viewBobbing;
                mc.gameSettings.viewBobbing = false;
                mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);

                final Vec3d forward = new Vec3d(0, 0, 1).rotatePitch(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationPitch)).rotateYaw(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));

                // Line
                RenderUtil.drawLine3D((float) forward.x, (float) forward.y + mc.player.getEyeHeight(), (float) forward.z, (float) (portal.x - mc.getRenderManager().renderPosX), (float) (portal.y - mc.getRenderManager().renderPosY), (float) (portal.z - mc.getRenderManager().renderPosZ), this.width.getValue(), new Color(red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f).getRGB());

                // Info
                if (this.showInfo.getValue()) {
                    RenderUtil.glBillboardDistanceScaled((float) portal.x, (float) portal.y, (float) portal.z, mc.player, this.infoScale.getValue());
                    GlStateManager.disableDepth();
                    this.drawPortalInfoText(portal, 0, 0);
                    GlStateManager.enableDepth();
                }

                mc.gameSettings.viewBobbing = bobbing;
                mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                GlStateManager.popMatrix();
            }
        }
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketJoinGame) {
                this.portals.clear();
            }
        }
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        this.portals.clear();
    }

    @Listener
    public void onChunkLoad(EventChunk event) {
        final Minecraft mc = Minecraft.getMinecraft();
        switch (event.getType()) {
            case LOAD:
                final Chunk chunk = event.getChunk();
                final ExtendedBlockStorage[] blockStoragesLoad = chunk.getBlockStorageArray();
                for (int i = 0; i < blockStoragesLoad.length; i++) {
                    final ExtendedBlockStorage extendedBlockStorage = blockStoragesLoad[i];
                    if (extendedBlockStorage == null) {
                        continue;
                    }

                    for (int x = 0; x < 16; ++x) {
                        for (int y = 0; y < 16; ++y) {
                            for (int z = 0; z < 16; ++z) {
                                final IBlockState blockState = extendedBlockStorage.get(x, y, z);
                                final int worldY = y + extendedBlockStorage.getYLocation();
                                if (blockState.getBlock().equals(Blocks.PORTAL)) {
                                    BlockPos position = new BlockPos(event.getChunk().getPos().getXStart() + x, worldY, event.getChunk().getPos().getZStart() + z);
                                    if (!isPortalCached(position.getX(), position.getY(), position.getZ(), 0)) {
                                        final Vec3d portal = new Vec3d(position.getX(), position.getY(), position.getZ());
                                        this.portals.add(portal);
                                        if (this.chat.getValue()) {
                                            this.printPortalToChat(portal);
                                        }
                                        return;
                                    }
                                }
                                if (blockState.getBlock().equals(Blocks.END_PORTAL)) {
                                    BlockPos position = new BlockPos(event.getChunk().getPos().getXStart() + x, worldY, event.getChunk().getPos().getZStart() + z);
                                    if (!isPortalCached(position.getX(), position.getY(), position.getZ(), 3)) {
                                        final Vec3d portal = new Vec3d(position.getX(), position.getY(), position.getZ());
                                        this.portals.add(portal);
                                        if (this.chat.getValue()) {
                                            this.printEndPortalToChat(portal);
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case UNLOAD:
                if (this.remove.getValue()) {
                    for (Vec3d portal : this.portals) {
                        if (mc.player.getDistance(portal.x, portal.y, portal.z) > this.removeDistance.getValue()) {
                            this.portals.remove(portal);
                        }
                    }
                }
                break;
        }
    }

    private boolean isPortalCached(int x, int y, int z, float dist) {
        for (int i = this.portals.size() - 1; i >= 0; i--) {
            Vec3d searchPortal = this.portals.get(i);

            if (searchPortal.distanceTo(new Vec3d(x, y, z)) <= dist)
                return true;

            if (searchPortal.x == x && searchPortal.y == y && searchPortal.z == z)
                return true;
        }
        return false;
    }

    private void printEndPortalToChat(Vec3d portal) {
        final TextComponentString portalTextComponent = new TextComponentString("End Portal found!");

        String coords = String.format("X: %s, Y: %s, Z: %s", (int) portal.x, (int) portal.y, (int) portal.z);
        int playerDistance = (int) Minecraft.getMinecraft().player.getDistance(portal.x, portal.y, portal.z);
        String distance = ChatFormatting.GRAY + "" + playerDistance + "m away";

        String hoverText = coords + "\n" + distance;
        portalTextComponent.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(hoverText))));

        Seppuku.INSTANCE.logcChat(portalTextComponent);
    }

    private void printPortalToChat(Vec3d portal) {
        final TextComponentString portalTextComponent = new TextComponentString("Portal found!");

        String overworld = "";
        String nether = "";

        if (Minecraft.getMinecraft().player.dimension == 0) { // overworld
            overworld = String.format("Overworld: X: %s, Y: %s, Z: %s", (int) portal.x, (int) portal.y, (int) portal.z);
            nether = String.format("Nether: X: %s, Y: %s, Z: %s", (int) portal.x / 8, (int) portal.y, (int) portal.z / 8);
        } else if (Minecraft.getMinecraft().player.dimension == -1) { // nether
            overworld = String.format("Overworld: X: %s, Y: %s, Z: %s", (int) portal.x * 8, (int) portal.y, (int) portal.z * 8);
            nether = String.format("Nether: X: %s, Y: %s, Z: %s", (int) portal.x, (int) portal.y, (int) portal.z);
        }

        int playerDistance = (int) Minecraft.getMinecraft().player.getDistance(portal.x, portal.y, portal.z);
        String distance = ChatFormatting.GRAY + "" + playerDistance + "m away";

        String hoverText = overworld + "\n" + nether + "\n" + distance;
        portalTextComponent.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(hoverText))));

        Seppuku.INSTANCE.logcChat(portalTextComponent);
    }

    private void drawPortalInfoText(Vec3d portal, float x, float y) {
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow((int) Minecraft.getMinecraft().player.getDistance(portal.x, portal.y, portal.z) + "m", x, y, 0xFFAAAAAA);
    }

    public List<Vec3d> getPortals() {
        return portals;
    }
}