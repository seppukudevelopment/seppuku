package me.rigamortis.seppuku.impl.module.render;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.event.render.EventRenderName;
import me.rigamortis.seppuku.api.friend.Friend;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.*;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author Seth
 * 4/20/2019 @ 10:07 AM.
 */
public final class WallHackModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"None", "Box"});

    public final BooleanValue players = new BooleanValue("Players", new String[]{"Player"}, true);
    public final BooleanValue mobs = new BooleanValue("Mobs", new String[]{"Mob"}, true);
    public final BooleanValue animals = new BooleanValue("Animals", new String[]{"Animal"}, true);
    public final BooleanValue vehicles = new BooleanValue("Vehicles", new String[]{"Vehic", "Vehicle"}, true);
    public final BooleanValue local = new BooleanValue("Local", new String[]{"Self"}, true);
    public final BooleanValue items = new BooleanValue("Items", new String[]{"Item"}, true);
    public final BooleanValue crystals = new BooleanValue("Crystals", new String[]{"crystal", "crystals", "endcrystal", "endcrystals"}, true);
    public final BooleanValue footsteps = new BooleanValue("FootSteps", new String[]{"FootStep", "Steps"}, false);
    public final BooleanValue armorStand = new BooleanValue("ArmorStands", new String[]{"ArmorStand", "ArmourStand", "ArmourStands", "ArmStand"}, true);

    public final BooleanValue name = new BooleanValue("Name", new String[]{"Nam"}, true);
    public final BooleanValue ping = new BooleanValue("Ping", new String[]{"Ms"}, true);
    public final BooleanValue armor = new BooleanValue("Armor", new String[]{"Arm"}, true);
    public final BooleanValue hearts = new BooleanValue("Hearts", new String[]{"Hrts"}, true);
    public final BooleanValue enchants = new BooleanValue("Enchants", new String[]{"Ench"}, true);
    public final OptionalValue potions = new OptionalValue("Potions", new String[]{"Pot", "Pots", "PotsMode"}, 1, new String[]{"None", "Icons", "Text"});

    public final BooleanValue background = new BooleanValue("Background", new String[]{"Bg"}, true);

    public final OptionalValue hpMode = new OptionalValue("Hp", new String[]{"Health", "HpMode"}, 0, new String[]{"None", "Bar", "BarText"});

    private ICamera camera = new Frustum();
    private final ResourceLocation inventory = new ResourceLocation("textures/gui/container/inventory.png");

    //i cba
    private List<FootstepData> footstepDataList = new CopyOnWriteArrayList<>();

    public WallHackModule() {
        super("WallHack", new String[]{"Esp"}, "Highlights entities", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void render2D(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (this.footsteps.getBoolean()) {
            for (FootstepData data : this.footstepDataList) {
                final GLUProjection.Projection projection = GLUProjection.getInstance().project(data.x - mc.getRenderManager().viewerPosX, data.y - mc.getRenderManager().viewerPosY, data.z - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);
                if (projection != null && projection.getType() == GLUProjection.Projection.Type.INSIDE) {
                    mc.fontRenderer.drawStringWithShadow("*step*", (float) projection.getX() - mc.fontRenderer.getStringWidth("*step*") / 2, (float) projection.getY(), -1);
                }

                if (Math.abs(System.currentTimeMillis() - data.getTime()) >= 3000) {
                    this.footstepDataList.remove(data);
                }
            }
        }

        for (Entity e : mc.world.loadedEntityList) {
            if (e != null) {
                if (this.checkFilter(e)) {
                    final float[] bounds = this.convertBounds(e, event.getPartialTicks(), event.getScaledResolution().getScaledWidth(), event.getScaledResolution().getScaledHeight());

                    if (bounds != null) {
                        if (this.mode.getInt() == 1) {
                            RenderUtil.drawOutlineRect(bounds[0], bounds[1], bounds[2], bounds[3], 1.5f, 0xAA000000);
                            RenderUtil.drawOutlineRect(bounds[0] - 0.5f, bounds[1] - 0.5f, bounds[2] + 0.5f, bounds[3] + 0.5f, 0.5f, this.getColor(e));
                        }

                        String name = StringUtils.stripControlCodes(getNameForEntity(e));
                        String heartsFormatted = null;
                        String pingFormatted = null;

                        if (this.name.getBoolean()) {
                            int color = -1;

                            final Friend friend = Seppuku.INSTANCE.getFriendManager().isFriend(e);

                            if (friend != null) {
                                name = friend.getAlias();
                                color = 0xFF9900EE;
                            }

                            if (this.background.getBoolean()) {
                                RenderUtil.drawRect(bounds[0] + (bounds[2] - bounds[0]) / 2 - mc.fontRenderer.getStringWidth(name) / 2 - 1, bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 2, bounds[0] + (bounds[2] - bounds[0]) / 2 + mc.fontRenderer.getStringWidth(name) / 2 + 1, bounds[1] + (bounds[3] - bounds[1]) - 1, 0x75101010);
                            }

                            mc.fontRenderer.drawStringWithShadow(name, bounds[0] + (bounds[2] - bounds[0]) / 2 - mc.fontRenderer.getStringWidth(name) / 2, bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 1, color);
                        }

                        if (e instanceof EntityPlayer) {
                            final EntityPlayer player = (EntityPlayer) e;
                            if (this.ping.getBoolean()) {
                                int responseTime = -1;
                                try {
                                    responseTime = (int) MathUtil.clamp(mc.getConnection().getPlayerInfo(player.getUniqueID()).getResponseTime(), 0, 300);
                                } catch (NullPointerException np) {
                                }
                                pingFormatted = responseTime + "ms";

                                float startX = -mc.fontRenderer.getStringWidth(pingFormatted) / 2.0f;
                                if (this.name.getBoolean())
                                    startX = (mc.fontRenderer.getStringWidth(name) / 2.0f) + 2.0f;
                                else if (this.hearts.getBoolean())
                                    startX = (mc.fontRenderer.getStringWidth(heartsFormatted) / 2.0f) + (mc.fontRenderer.getStringWidth(heartsFormatted) / 2.0f);

                                int pingRounded = Math.round(255.0f - (responseTime * 255.0f / 300.0f)); // 300 = max response time (red, laggy)
                                int pingColor = 255 - pingRounded << 16 | pingRounded << 8;

                                if (this.background.getBoolean()) {
                                    RenderUtil.drawRect(bounds[0] + (bounds[2] - bounds[0]) / 2 + startX, bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 2, bounds[0] + (bounds[2] - bounds[0]) / 2 + startX + mc.fontRenderer.getStringWidth(pingFormatted), bounds[1] + (bounds[3] - bounds[1]) - 1, 0x75101010);
                                }

                                mc.fontRenderer.drawStringWithShadow(pingFormatted, bounds[0] + (bounds[2] - bounds[0]) / 2 + startX, bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 1, pingColor);
                            }
                        }

                        if (e instanceof EntityLivingBase) {
                            final EntityLivingBase entityLiving = (EntityLivingBase) e;

                            if (this.hearts.getBoolean()) {
                                final float hearts = entityLiving.getHealth() / 2.0f;

                                if (hearts <= 0)
                                    heartsFormatted = "*DEAD*";
                                else
                                    heartsFormatted = (Math.floor(hearts) == hearts ? (int) hearts : String.format("%.1f", hearts)) + "";

                                if (e instanceof EntityPlayer) {
                                    if (((EntityPlayer) e).isCreative())
                                        heartsFormatted = ChatFormatting.YELLOW + "*";
                                }

                                float startX = -mc.fontRenderer.getStringWidth(heartsFormatted) / 2.0f;
                                if (this.name.getBoolean())
                                    startX = -(mc.fontRenderer.getStringWidth(name) / 2.0f) - 2.0f - mc.fontRenderer.getStringWidth(heartsFormatted);
                                else if (this.ping.getBoolean() && entityLiving instanceof EntityPlayer)
                                    startX = -(mc.fontRenderer.getStringWidth(pingFormatted) / 2.0f) - (mc.fontRenderer.getStringWidth(heartsFormatted) / 2.0f);

                                int heartsRounded = Math.round(255.0f - (hearts * 255.0f / (entityLiving.getMaxHealth() / 2)));
                                int heartsColor = 255 - heartsRounded << 8 | heartsRounded << 16;

                                if (this.background.getBoolean()) {
                                    RenderUtil.drawRect(bounds[0] + (bounds[2] - bounds[0]) / 2 + startX, bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 2, bounds[0] + (bounds[2] - bounds[0]) / 2 + startX + mc.fontRenderer.getStringWidth(heartsFormatted), bounds[1] + (bounds[3] - bounds[1]) - 1, 0x75101010);
                                }

                                mc.fontRenderer.drawStringWithShadow(heartsFormatted, bounds[0] + (bounds[2] - bounds[0]) / 2 + startX, bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 1, heartsColor);
                            }

                            if (this.hpMode.getInt() != 0) {
                                RenderUtil.drawRect(bounds[2] - 0.5f, bounds[1], bounds[2] - 2, bounds[3], 0xAA000000);
                                final float hpHeight = ((((EntityLivingBase) e).getHealth() * (bounds[3] - bounds[1])) / ((EntityLivingBase) e).getMaxHealth());

                                RenderUtil.drawRect(bounds[2] - 1, bounds[1] - 0.5f, bounds[2] - 1.5f, (bounds[1] - bounds[3]) + bounds[3] + hpHeight + 0.5f, getHealthColor(e));

                                if (this.hpMode.getInt() == 2) {
                                    if (((EntityLivingBase) e).getHealth() < ((EntityLivingBase) e).getMaxHealth() && ((EntityLivingBase) e).getHealth() > 0) {
                                        final String hp = new DecimalFormat("#.#").format((int) ((EntityLivingBase) e).getHealth());
                                        mc.fontRenderer.drawStringWithShadow(hp, (bounds[2] - 1 - mc.fontRenderer.getStringWidth(hp) / 2), ((bounds[1] - bounds[3]) + bounds[3] + hpHeight + 0.5f - mc.fontRenderer.FONT_HEIGHT / 2), -1);
                                    }
                                }
                            }

                            if (this.potions.getInt() != 0) {
                                float scale = 0.5f;
                                int offsetX = 0;
                                int offsetY = 0;

                                for (PotionEffect effect : entityLiving.getActivePotionEffects()) {
                                    if (effect.getDuration() <= 0)
                                        continue;
                                    final Potion potion = effect.getPotion();
                                    if (this.potions.getInt() == 1) {
                                        if (potion.hasStatusIcon()) {
                                            GlStateManager.pushMatrix();
                                            mc.getTextureManager().bindTexture(this.inventory);// bind the inventory texture
                                            int iconIndex = potion.getStatusIconIndex();
                                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                                            GlStateManager.translate(bounds[0] + 1, bounds[3], 0);
                                            GlStateManager.scale(scale, scale, 0);

                                            // scoot it over to the right
                                            if (offsetY > 16) {
                                                offsetY = 0;
                                                offsetX += 16;
                                            }

                                            // check to draw the transparent background behind the icon
                                            if (this.background.getBoolean()) {
                                                RenderUtil.drawRect(offsetX, offsetY, offsetX + 16, offsetY + 16, 0x75101010);
                                            }

                                            // draw the textured icon
                                            RenderUtil.drawTexture(offsetX, offsetY, iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18);
                                            GlStateManager.popMatrix();

                                            offsetY += 16;
                                        }
                                    } else if (this.potions.getInt() == 2) {
                                        final List<String> potStringsToDraw = Lists.newArrayList();
                                        final String effectString = PotionUtil.getNameDurationString(effect);

                                        if (effectString != null) { // will return null if it doesn't exist as a valid formatted name
                                            potStringsToDraw.add(effectString);
                                        }

                                        for (String pString : potStringsToDraw) {
                                            GlStateManager.pushMatrix();
                                            GlStateManager.disableDepth();
                                            GlStateManager.translate(bounds[0] + 1, bounds[3] + offsetY, 0);

                                            GlStateManager.scale(0.5f, 0.5f, 0.5f);

                                            if (offsetY > 16) {
                                                offsetY = 0;
                                                offsetX += 16;
                                            }
                                            if (this.background.getBoolean()) {
                                                RenderUtil.drawRect(0, 0, mc.fontRenderer.getStringWidth(pString), mc.fontRenderer.FONT_HEIGHT - 1, 0x75101010);
                                            }
                                            mc.fontRenderer.drawStringWithShadow(pString, 0, 0, -1);

                                            GlStateManager.scale(2, 2, 2);
                                            GlStateManager.enableDepth();
                                            GlStateManager.popMatrix();

                                            offsetY += 4;
                                        }
                                    }
                                }
                            }
                        }

                        if (this.armor.getBoolean()) {
                            final Iterator<ItemStack> items = e.getEquipmentAndArmor().iterator();
                            final ArrayList<ItemStack> stacks = new ArrayList<>();

                            while (items.hasNext()) {
                                final ItemStack stack = items.next();
                                if (stack != null && stack.getItem() != Items.AIR) {
                                    stacks.add(stack);
                                }
                            }

                            Collections.reverse(stacks);

                            int x = 0;

                            for (ItemStack stack : stacks) {
                                if (stack != null) {
                                    final Item item = stack.getItem();
                                    if (item != Items.AIR) {
                                        GlStateManager.pushMatrix();
                                        GlStateManager.enableBlend();
                                        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                                        RenderHelper.enableGUIStandardItemLighting();
                                        GlStateManager.translate(bounds[0] + (bounds[2] - bounds[0]) / 2 + x - (16 * stacks.size() / 2), bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 19, 0);
                                        if (this.background.getBoolean()) {
                                            RenderUtil.drawRect(0, 0, 16, 16, 0x75101010);
                                        }
                                        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
                                        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, 0, 0);
                                        RenderHelper.disableStandardItemLighting();
                                        GlStateManager.disableBlend();
                                        GlStateManager.color(1, 1, 1, 1);
                                        GlStateManager.popMatrix();
                                        x += 16;

                                        if (this.enchants.getBoolean()) {
                                            final List<String> stringsToDraw = Lists.newArrayList();
                                            int y = 0;

                                            if (stack.getEnchantmentTagList() != null) {
                                                final NBTTagList tags = stack.getEnchantmentTagList();
                                                for (int i = 0; i < tags.tagCount(); i++) {
                                                    final NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
                                                    if (tagCompound != null && Enchantment.getEnchantmentByID(tagCompound.getByte("id")) != null) {
                                                        final Enchantment enchantment = Enchantment.getEnchantmentByID(tagCompound.getShort("id"));
                                                        final short lvl = tagCompound.getShort("lvl");
                                                        if (enchantment != null) {
                                                            String ench = "";
                                                            if (enchantment.isCurse()) {
                                                                ench = ChatFormatting.RED + enchantment.getTranslatedName(lvl).substring(11).substring(0, 3) + ChatFormatting.GRAY + lvl;
                                                            } else if (ItemUtil.isIllegalEnchant(enchantment, lvl)) {
                                                                ench = ChatFormatting.AQUA + enchantment.getTranslatedName(lvl).substring(0, 3) + ChatFormatting.GRAY + lvl;
                                                            } else {
                                                                ench = enchantment.getTranslatedName(lvl).substring(0, 3) + ChatFormatting.GRAY + lvl;
                                                            }
                                                            stringsToDraw.add(ench);
                                                        }
                                                    }
                                                }
                                            }

                                            // Enchanted gapple
                                            if (item == Items.GOLDEN_APPLE) {
                                                if (stack.getItemDamage() == 1) {
                                                    stringsToDraw.add(ChatFormatting.YELLOW + "God");
                                                }
                                            }

                                            for (String string : stringsToDraw) {
                                                GlStateManager.pushMatrix();
                                                GlStateManager.disableDepth();
                                                GlStateManager.translate(bounds[0] + (bounds[2] - bounds[0]) / 2 + x - ((16.0f * stacks.size()) / 2.0f) - (16.0f / 2.0f) - (mc.fontRenderer.getStringWidth(string) / 4.0f), bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 23 - y, 0);
                                                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                                                if (this.background.getBoolean()) {
                                                    RenderUtil.drawRect(0, 0, mc.fontRenderer.getStringWidth(string), mc.fontRenderer.FONT_HEIGHT - 1, 0x75101010);
                                                }
                                                mc.fontRenderer.drawStringWithShadow(string, 0, 0, -1);
                                                GlStateManager.scale(2, 2, 2);
                                                GlStateManager.enableDepth();
                                                GlStateManager.popMatrix();
                                                y += 4;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

                if (packet.getCategory() == SoundCategory.NEUTRAL || packet.getCategory() == SoundCategory.PLAYERS) {
                    final String sound = packet.getSound().getSoundName().getPath();
                    if (sound.endsWith(".step") || sound.endsWith(".paddle_land") || sound.endsWith(".gallop")) {
                        this.footstepDataList.add(new FootstepData(packet.getX(), packet.getY(), packet.getZ(), System.currentTimeMillis()));
                    }
                }
            }
        }
    }

    private int getHealthColor(Entity entity) {
        int scale = (int) Math.round(255.0 - (double) ((EntityLivingBase) entity).getHealth() * 255.0 / (double) ((EntityLivingBase) entity).getMaxHealth());
        int damageColor = 255 - scale << 8 | scale << 16;

        return (255 << 24) | damageColor;
    }

    @Listener
    public void renderName(EventRenderName event) {
        if (event.getEntity() instanceof EntityPlayer) {
            event.setCanceled(true);
        }
    }

    private String getNameForEntity(Entity entity) {
        if (entity instanceof EntityItem) {
            final EntityItem item = (EntityItem) entity;
            String itemName = "";

            final int stackSize = item.getItem().getCount();
            if (stackSize > 1) {
                itemName = item.getItem().getDisplayName() + "(" + item.getItem().getCount() + ")";
            } else {
                itemName = item.getItem().getDisplayName();
            }
            return itemName;
        }
        if (entity instanceof EntityEnderCrystal) {
            return "End Crystal";
        }
        if (entity instanceof EntityMinecart) {
            final EntityMinecart minecart = (EntityMinecart) entity;
            return minecart.getCartItem().getDisplayName();
        }
        return entity.getName();
    }

    private boolean checkFilter(Entity entity) {
        boolean ret = false;

        if (this.local.getBoolean() && (entity == Minecraft.getMinecraft().player) && (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0)) {
            ret = true;
        }
        if (this.players.getBoolean() && entity instanceof EntityPlayer && entity != Minecraft.getMinecraft().player) {
            ret = true;
        }
        if (this.mobs.getBoolean() && entity instanceof IMob) {
            ret = true;
        }
        if (this.animals.getBoolean() && entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = true;
        }
        if (this.items.getBoolean() && entity instanceof EntityItem) {
            ret = true;
        }
        if (this.crystals.getBoolean() && entity instanceof EntityEnderCrystal) {
            ret = true;
        }
        if (this.vehicles.getBoolean() && (entity instanceof EntityBoat || entity instanceof EntityMinecart || entity instanceof EntityMinecartContainer)) {
            ret = true;
        }
        if (this.armorStand.getBoolean() && entity instanceof EntityArmorStand) {
            ret = true;
        }
        if (Minecraft.getMinecraft().player.getRidingEntity() != null && entity == Minecraft.getMinecraft().player.getRidingEntity()) {
            ret = false;
        }

        return ret;
    }

    private int getColor(Entity entity) {
        int ret = -1;

        if (entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = 0xFF00FF44;
        }
        if (entity instanceof IMob) {
            ret = 0xFFFFAA00;
        }
        if (entity instanceof EntityBoat || entity instanceof EntityMinecart || entity instanceof EntityMinecartContainer) {
            ret = 0xFF00FFAA;
        }
        if (entity instanceof EntityItem) {
            ret = 0xFF00FFAA;
        }
        if (entity instanceof EntityEnderCrystal) {
            ret = 0xFFCD00CD;
        }
        if (entity instanceof EntityPlayer) {
            ret = 0xFFFF4444;

            if (entity == Minecraft.getMinecraft().player) {
                ret = -1;
            }

            if (entity.isSneaking()) {
                ret = 0xFFEE9900;
            }

            if (Seppuku.INSTANCE.getFriendManager().isFriend(entity) != null) {
                ret = 0xFF9900EE;
            }
        }
        return ret;
    }

    private float[] convertBounds(Entity e, float partialTicks, int width, int height) {
        float x = -1;
        float y = -1;
        float w = width + 1;
        float h = height + 1;

        final Vec3d pos = MathUtil.interpolateEntity(e, partialTicks);

        if (pos == null) {
            return null;
        }

        AxisAlignedBB bb = e.getEntityBoundingBox();

        if (e instanceof EntityEnderCrystal) {
            bb = new AxisAlignedBB(bb.minX + 0.3f, bb.minY + 0.2f, bb.minZ + 0.3f, bb.maxX - 0.3f, bb.maxY, bb.maxZ - 0.3f);
        }

        if (e instanceof EntityItem) {
            bb = new AxisAlignedBB(bb.minX, bb.minY + 0.7f, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        }

        bb = bb.expand(0.15f, 0.1f, 0.15f);

        camera.setPosition(Minecraft.getMinecraft().getRenderViewEntity().posX, Minecraft.getMinecraft().getRenderViewEntity().posY, Minecraft.getMinecraft().getRenderViewEntity().posZ);

        if (!camera.isBoundingBoxInFrustum(bb)) {
            return null;
        }

        final Vec3d corners[] = {
                new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),

                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2)
        };

        for (Vec3d vec : corners) {
            final GLUProjection.Projection projection = GLUProjection.getInstance().project(pos.x + vec.x - Minecraft.getMinecraft().getRenderManager().viewerPosX, pos.y + vec.y - Minecraft.getMinecraft().getRenderManager().viewerPosY, pos.z + vec.z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

            if (projection == null) {
                return null;
            }

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

    public static class FootstepData {
        private double x;
        private double y;
        private double z;
        private long time;

        public FootstepData(double x, double y, double z, long time) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }

}
