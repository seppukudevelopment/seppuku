package me.rigamortis.seppuku.impl.gui.hud.component;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.friend.Friend;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityShulkerBox;

import java.util.List;

/**
 * @author noil
 */
public final class EntityListComponent extends DraggableHudComponent {

    public final List<HudComponent> components;
    private final List<EntityGroup> entityGroups;

    public EntityListComponent() {
        super("EntityList");

        this.entityGroups = Lists.newArrayListWithExpectedSize(16);
        this.components = Lists.newArrayList();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final ScaledResolution res = new ScaledResolution(mc);
        boolean isInHudEditor = mc.currentScreen instanceof GuiHudEditor;

        float xOffset = 0;
        float yOffset = 0;
        float maxWidth = 0;

        if (mc.player != null && mc.world != null) {
            this.components.clear();
            this.entityGroups.clear();

            for (Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityLiving || entity instanceof EntityPlayer || entity instanceof EntityItem || entity instanceof EntityThrowable || entity instanceof EntityEnderCrystal) {
                    String entityName = this.getNameForEntity(entity);
                    if (entityName != null) {
                        if (this.hasGroupForName(entityName)) {
                            EntityGroup entityGroup = getGroupFromName(entityName);
                            if (entityGroup != null) {
                                entityGroup.setCount(entityGroup.getCount() + 1);
                            }
                        } else {
                            entityGroups.add(new EntityGroup(entity, entityName));
                        }
                    }
                }
            }

            if (this.getAnchorPoint() == null) {
                this.components.add(new TextLineEntity(null, ChatFormatting.GRAY + "(text radar)"));
            }

            if (entityGroups.size() > 0) {
                for (EntityGroup entityGroup : entityGroups) {
                    String line = entityGroup.getEntityName();
                    if (entityGroup.getCount() > 1) {
                        line = String.format("%s [%s]", entityGroup.getEntityName(), entityGroup.getCount());
                    }
                    this.components.add(new TextLineEntity(entityGroup.getEntity(), line));
                }
            } else {
                this.components.add(new TextLineEntity(null, "No entities nearby"));
            }

            for (HudComponent component : this.components) {
                if (component != null) {
                    String name = component.getName();

                    final float width = mc.fontRenderer.getStringWidth(name);
                    if (width >= maxWidth) {
                        maxWidth = width;
                    }

                    if (this.getAnchorPoint() != null) {
                        switch (this.getAnchorPoint().getPoint()) {
                            case TOP_CENTER:
                            case BOTTOM_CENTER:
                                xOffset = (this.getW() - mc.fontRenderer.getStringWidth(name)) / 2;
                                break;
                            case TOP_LEFT:
                            case BOTTOM_LEFT:
                                xOffset = 0;
                                break;
                            case TOP_RIGHT:
                            case BOTTOM_RIGHT:
                                xOffset = this.getW() - mc.fontRenderer.getStringWidth(name);
                                break;
                        }
                    }

                    // set the width and height to the string size
                    component.setW(mc.fontRenderer.getStringWidth(name));
                    component.setH(mc.fontRenderer.FONT_HEIGHT);

                    if (this.getAnchorPoint() != null) {
                        switch (this.getAnchorPoint().getPoint()) {
                            case TOP_CENTER:
                            case TOP_LEFT:
                            case TOP_RIGHT:
                                component.setX(this.getX() + xOffset);
                                component.setY(this.getY() + yOffset);
                                component.render(mouseX, mouseY, partialTicks);
                                yOffset += (mc.fontRenderer.FONT_HEIGHT);
                                break;
                            case BOTTOM_CENTER:
                            case BOTTOM_LEFT:
                            case BOTTOM_RIGHT:
                                component.setX(this.getX() + xOffset);
                                component.setY(this.getY() + (this.getH() - mc.fontRenderer.FONT_HEIGHT) + yOffset);
                                component.render(mouseX, mouseY, partialTicks);
                                yOffset -= (mc.fontRenderer.FONT_HEIGHT);
                                break;
                        }
                    } else {
                        component.setX(this.getX() + xOffset);
                        component.setY(this.getY() + yOffset);
                        component.render(mouseX, mouseY, partialTicks);
                        yOffset += (mc.fontRenderer.FONT_HEIGHT);
                    }
                }
            }
        } else {
            if (isInHudEditor) {
                // no entities nearby
                final String arraylist = "(entity list)";
                mc.fontRenderer.drawStringWithShadow(arraylist, this.getX(), this.getY(), 0xFFAAAAAA);
                maxWidth = mc.fontRenderer.getStringWidth(arraylist) + 1 /* right side gap */;
                yOffset = mc.fontRenderer.FONT_HEIGHT + 1;
            }
        }

        this.setW(maxWidth);
        this.setH(Math.abs(yOffset));

        if (this.getH() > res.getScaledHeight()) {
            this.setH(res.getScaledHeight() - 4);
        }
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);
        if (this.isMouseInside(mouseX, mouseY)) {
            for (HudComponent component : this.components) {
                component.mouseRelease(mouseX, mouseY, button);
            }
        }
    }

    private boolean hasGroupForName(String entityName) {
        for (EntityGroup group : entityGroups) {
            if (group.getEntityName().equals(entityName)) {
                return true;
            }
        }
        return false;
    }

    private EntityGroup getGroupFromName(String entityName) {
        for (EntityGroup group : entityGroups) {
            if (group.getEntityName().equals(entityName)) {
                return group;
            }
        }
        return null;
    }

    private String getNameForEntity(Entity entity) {
        String name = entity.getDisplayName().getFormattedText();
        if (entity instanceof EntityPlayer) {
            if (entity.getName().equalsIgnoreCase(mc.getSession().getUsername())) {
                return null;
            }
            final Friend friend = Seppuku.INSTANCE.getFriendManager().isFriend(entity);
            if (friend != null) {
                return ChatFormatting.DARK_PURPLE + entity.getName() + ChatFormatting.RESET + " (" + (int) mc.player.getDistance(entity) + "m)";
            }
            return ChatFormatting.RED + entity.getName() + ChatFormatting.RESET + " (" + (int) mc.player.getDistance(entity) + "m)";
        } else if (entity instanceof EntityLiving) {
            final EntityLiving entityLiving = (EntityLiving) entity;
            return EntityList.getEntityString(entity) + " (" + ChatFormatting.GREEN + (int) entityLiving.getHealth() + ChatFormatting.RESET + ")";
        } else if (entity instanceof EntityItem) {
            EntityItem item = (EntityItem) entity;
            ItemStack stack = item.getItem();
            int stackSize = stack.getCount();
            boolean moreThanZero = stackSize > 1;
            if (stack.isItemEnchanted()) {
                name = ChatFormatting.AQUA + stack.getDisplayName() + (moreThanZero ? " (" + ChatFormatting.YELLOW + "x" + stackSize + ChatFormatting.AQUA + ")" : "");
            } else {
                name = ChatFormatting.GRAY + stack.getDisplayName() + (moreThanZero ? " (" + ChatFormatting.YELLOW + "x" + stackSize + ChatFormatting.GRAY + ")" : "");
            }
        } else if (entity instanceof EntityThrowable) {
            EntityThrowable throwable = (EntityThrowable) entity;
            if (throwable instanceof EntityEnderPearl) {
                name = ChatFormatting.DARK_AQUA + "Ender Pearl";
            } else {
                name = throwable.getName();
            }
        } else if (entity instanceof EntityEnderCrystal) {
            name = ChatFormatting.LIGHT_PURPLE + "Ender Crystal";
        }
        return name;
    }

    private static class EntityGroup {
        private final Entity entity;
        private final String entityName;
        private int count;

        public EntityGroup(Entity entity, String entityName) {
            this.entity = entity;
            this.entityName = entityName;
            this.count = 1;
        }

        public Entity getEntity() {
            return entity;
        }

        public String getEntityName() {
            return entityName;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    private class TextLineEntity extends HudComponent {
        private final Entity entity;

        public TextLineEntity(Entity entity, String text) {
            super(text);
            this.entity = entity;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            super.render(mouseX, mouseY, partialTicks);
            mc.fontRenderer.drawStringWithShadow(this.getName(), (int) this.getX(), (int) this.getY(), 0xFFFFFFFF);
        }

        @Override
        public void mouseRelease(int mouseX, int mouseY, int button) {
            super.mouseRelease(mouseX, mouseY, button);
            if (this.isMouseInside(mouseX, mouseY) && button == 1/*right click*/) {
                if (entity == null)
                    return;

                if (entity instanceof EntityPlayer) {
                    final Friend friend = Seppuku.INSTANCE.getFriendManager().isFriend(entity);
                    if (friend != null) {
                        Seppuku.INSTANCE.getFriendManager().getFriendList().remove(friend);
                    } else {
                        Seppuku.INSTANCE.getFriendManager().add(entity.getName(), entity.getName(), false);
                    }
                } else if (entity instanceof EntityItem) {
                    final EntityItem entityItem = (EntityItem) entity;
                    if (!entityItem.getItem().isEmpty()) {
                        final ItemStack itemStack = entityItem.getItem();
                        if (Block.getBlockFromItem(itemStack.getItem()) instanceof BlockShulkerBox) {
                            final NBTTagCompound tag = itemStack.getTagCompound();
                            if (tag != null && tag.hasKey("BlockEntityTag", 10)) {
                                final NBTTagCompound entityTag = tag.getCompoundTag("BlockEntityTag");
                                final TileEntityShulkerBox tileEntityShulkerBox = new TileEntityShulkerBox();
                                tileEntityShulkerBox.setWorld(mc.world);
                                tileEntityShulkerBox.readFromNBT(entityTag);
                                mc.displayGuiScreen(new GuiShulkerBox(mc.player.inventory, tileEntityShulkerBox));
                            } else {
                                Seppuku.INSTANCE.errorChat("This shulker box is empty");
                            }
                        } else if (itemStack.isItemEnchanted()) {
                            final StringBuilder enchantStringBuilder = new StringBuilder();

                            final NBTTagCompound tagCompound = itemStack.getTagCompound();
                            if (tagCompound != null && !tagCompound.isEmpty()) {
                                final NBTTagList nbtEnchantTagList = tagCompound.getTagList("ench", 10);
                                for (NBTBase enchantBaseCompound : nbtEnchantTagList) {
                                    if (enchantBaseCompound instanceof NBTTagCompound) {
                                        final NBTTagCompound enchantCompound = (NBTTagCompound) enchantBaseCompound;
                                        final short enchantID = enchantCompound.getShort("id");
                                        final short enchantLvl = enchantCompound.getShort("lvl");
                                        final Enchantment enchantment = Enchantment.getEnchantmentByID(enchantID);
                                        if (enchantment != null) {
                                            final String enchantName = ChatFormatting.RESET + "[" + ChatFormatting.AQUA + enchantment.getTranslatedName(enchantLvl) + ChatFormatting.RESET + "] ";
                                            enchantStringBuilder.append(enchantName);
                                        }
                                    }
                                }
                            }

                            final String info = String.format("\n%s\n- Key: %s\n- Enchantments: %s\n- Durability: %s", ChatFormatting.AQUA + itemStack.getDisplayName() + ChatFormatting.RESET, itemStack.getTranslationKey(), enchantStringBuilder, itemStack.getMaxDamage() - itemStack.getItemDamage());
                            Seppuku.INSTANCE.logChat(info);
                        } else {
                            final String info = String.format("\n%s\n- Key: %s\n- Count: %s\n- Metadata: %s\n- Damage: %s\n- Max Damage: %s\n- Durability: %s", ChatFormatting.GRAY + itemStack.getDisplayName(), itemStack.getTranslationKey(), itemStack.getCount(), itemStack.getMetadata(), itemStack.getItemDamage(), itemStack.getMaxDamage(), itemStack.getMaxDamage() - itemStack.getItemDamage());
                            Seppuku.INSTANCE.logChat(info);
                            NBTTagCompound tagCompound = itemStack.getTagCompound();
                            if (tagCompound != null && !tagCompound.isEmpty()) {
                                StringBuilder compoundData = new StringBuilder("\n- Compound:");
                                for (String s : tagCompound.getKeySet()) {
                                    compoundData.append("\n-- ").append(s).append(": ");
                                    compoundData.append(tagCompound.getTag(s));
                                }
                                Seppuku.INSTANCE.logChat(compoundData.toString());
                            }
                        }
                    }
                }
            }
        }
    }
}
