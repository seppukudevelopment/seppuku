package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.gui.hud.EventUIValueChanged;
import me.rigamortis.seppuku.api.event.gui.hud.modulelist.EventUIListValueChanged;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.render.*;
import me.rigamortis.seppuku.api.event.world.EventAddEntity;
import me.rigamortis.seppuku.api.event.world.EventLightUpdate;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.event.world.EventSpawnParticle;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.server.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 4/11/2019 @ 3:48 AM.
 */
public final class NoLagModule extends Module {

    public final Value<Boolean> blocks = new Value<Boolean>("Blocks", new String[]{"NoLagBlocks", "Block", "b"}, "Manual override for block renders", false);
    public final Value<Boolean> blocksAll = new Value<Boolean>("BlocksAll", new String[]{"NoLagBlocksAll", "AllBlocks", "ba"}, "Disables the rendering of all blocks", false);
    public final Value<List<Block>> blocksList = new Value<List<Block>>("BlocksList", new String[]{"NoLagBlocksList", "BlockIds", "blockid"}, "Blocks to disable rendering");
    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]{"Item", "i"}, "Manual override for dropped item renders", false);
    public final Value<Boolean> itemsAll = new Value<Boolean>("ItemsAll", new String[]{"AllItems", "ia"}, "Disables the rendering of all items", false);
    public final Value<Boolean> itemsItemBlocks = new Value<Boolean>("ItemsItemBlocks", new String[]{"AllItemBlocks", "itemblocks"}, "Disables the rendering of dropped item-block stacks", false);
    public final Value<List<Item>> itemsList = new Value<List<Item>>("ItemsList", new String[]{"ItemIds", "itemid"}, "Items to disable rendering");

    public final Value<Boolean> light = new Value<Boolean>("Light", new String[]{"Lit", "l"}, "Choose to enable the lighting lag fix. Disables lighting updates", false);
    public final Value<Boolean> signs = new Value<Boolean>("Signs", new String[]{"Sign", "si"}, "Choose to enable the sign lag fix. Disables the rendering of sign text", false);
    public final Value<Boolean> sounds = new Value<Boolean>("Sounds", new String[]{"Sound", "s"}, "Choose to enable the sound lag fix. Disable entity swap-item/equip sound", true);
    public final Value<Boolean> fluids = new Value<Boolean>("Fluids", new String[]{"Fluid", "f", "Liquids", "liq", "Water", "Lava"}, "Disables the rendering of all fluids", false);
    public final Value<Boolean> pistons = new Value<Boolean>("Pistons", new String[]{"Piston", "p"}, "Choose to enable the piston lag fix. Disables pistons from rendering", false);
    public final Value<Boolean> slimes = new Value<Boolean>("Slimes", new String[]{"Slime", "sl"}, "Choose to enable the slime lag fix. Disables slimes from spawning", false);
    public final Value<Boolean> particles = new Value<Boolean>("Particles", new String[]{"Part", "par"}, "Disables the spawning of all particles", true);
    public final Value<Boolean> particlesPackets = new Value<Boolean>("ParticlesPackets", new String[]{"PartPacket", "parpac"}, "Disables particle packets and effect packets", false);
    public final Value<Boolean> particlesEntityPackets = new Value<Boolean>("ParticlesEntityPackets", new String[]{"PartEntPacket", "parentpac"}, "Disables entity effect packets (usually particles)", false);
    public final Value<Boolean> sky = new Value<Boolean>("Sky", new String[]{"Skies", "ski"}, "Disables the rendering of the sky", true);
    public final Value<Boolean> names = new Value<Boolean>("Names", new String[]{"Name", "n"}, "Disables the rendering of vanilla name-tags", false);
    public final Value<Boolean> withers = new Value<Boolean>("Withers", new String[]{"Wither", "w"}, "Disables the rendering of withers", false);
    public final Value<Boolean> withersForce = new Value<Boolean>("WithersForce", new String[]{"WithersF", "wf"}, "Force disables the rendering of withers", false);
    public final Value<Boolean> skulls = new Value<Boolean>("Skulls", new String[]{"WitherSkull", "skulls", "skull", "ws"}, "Disables the rendering of flying wither skulls", false);
    public final Value<Boolean> crystals = new Value<Boolean>("Crystals", new String[]{"Crystal", "cr", "c"}, "Disables the rendering of crystals", false);
    public final Value<Boolean> tnt = new Value<Boolean>("TNT", new String[]{"Dynamite", "explosives", "tn"}, "Disables the rendering of (primed) TNT", false);
    public final Value<Boolean> torches = new Value<Boolean>("Torches", new String[]{"Torch", "t"}, "Disables the rendering of torches", false);
    public final Value<Boolean> fireworks = new Value<Boolean>("Fireworks", new String[]{"FireW", "Fworks", "fw"}, "Disables the rendering of fireworks", false);
    public final Value<Boolean> fireworksEffects = new Value<Boolean>("FireworksEffects", new String[]{"FireWE", "Fworkfx", "fwe"}, "Disables the rendering of firework effects", false);
    public final Value<Boolean> redstone = new Value<Boolean>("Redstone", new String[]{"Red", "r"}, "Disables the rendering of redstone dust", false);
    public final Value<Boolean> redstoneTorch = new Value<Boolean>("RedstoneTorch", new String[]{"RedTorch", "rt"}, "Disables the rendering of redstone torches", false);
    public final Value<Boolean> redstoneLogic = new Value<Boolean>("RedstoneLogic", new String[]{"RedLogic", "rl"}, "Disables the rendering of redstone logic blocks", false);
    public final Value<Boolean> storms = new Value<Boolean>("Storms", new String[]{"Lightning"}, "Disables the rendering of lightning strikes", false);

    public NoLagModule() {
        super("NoLag", new String[]{"AntiLag", "NoRender"}, "Fixes malicious lag exploits and bugs that cause lag", "NONE", -1, ModuleType.RENDER);

        this.blocksList.setValue(new ArrayList<>());
        this.itemsList.setValue(new ArrayList<>());
    }

    @Override
    public void onToggle() {
        super.onToggle();

        if (Minecraft.getMinecraft().world != null)
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSpawnMob) {
                final SPacketSpawnMob packet = (SPacketSpawnMob) event.getPacket();

                if (this.slimes.getValue()) {
                    if (packet.getEntityType() == 55) {
                        event.setCanceled(true);
                    }
                }

                if (this.withersForce.getValue()) {
                    if (packet.getEntityType() == 64) {
                        event.setCanceled(true);
                    }
                }

                if (this.skulls.getValue()) {
                    if (packet.getEntityType() == 19) {
                        event.setCanceled(true);
                    }
                }
            }

            if (this.sounds.getValue()) {
                if (event.getPacket() instanceof SPacketSoundEffect) {
                    final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                    if (packet.getCategory() == SoundCategory.PLAYERS && packet.getSound() == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
                        event.setCanceled(true);
                    }
                    if (packet.getSound() == SoundEvents.ENTITY_FIREWORK_LAUNCH) {
                        event.setCanceled(true);
                    }
                }
            }

            if (this.particlesPackets.getValue()) {
                if (event.getPacket() instanceof SPacketParticles || event.getPacket() instanceof SPacketEffect) {
                    event.setCanceled(true);
                }
            }

            if (this.particlesEntityPackets.getValue()) {
                if (event.getPacket() instanceof SPacketEntityEffect) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Listener
    public void onRenderWorld(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (this.signs.getValue()) {
            for (TileEntity te : mc.world.loadedTileEntityList) {
                if (te instanceof TileEntitySign) {
                    final TileEntitySign sign = (TileEntitySign) te;
                    sign.signText = new ITextComponent[]{new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")};
                }
            }
        }
    }

    @Listener
    public void onSpawnParticle(EventSpawnParticle event) {
        if (this.particles.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onRenderBlock(EventRenderBlock event) {
        final BlockPos pos = event.getPos();
        final Block block = Minecraft.getMinecraft().world.getBlockState(pos).getBlock();
        if (block != Blocks.AIR) {
            if (this.blocks.getValue()) {
                if (this.blocksAll.getValue()) {
                    event.setCanceled(true);
                } else {
                    this.blocksList.getValue().forEach(listBlock -> {
                        if (Block.getIdFromBlock(block) == Block.getIdFromBlock(listBlock)) {
                            event.setCanceled(true);
                        }
                    });
                }
            }

            if (this.fluids.getValue()) {
                if (block instanceof BlockLiquid) {
                    event.setCanceled(true);
                }
            }

            if (this.pistons.getValue()) {
                if (block instanceof BlockPistonMoving || block instanceof BlockPistonExtension) {
                    event.setCanceled(true);
                }
            }

            if (this.redstone.getValue()) {
                if (block instanceof BlockRedstoneDiode || block instanceof BlockRedstoneWire) {
                    event.setCanceled(true);
                }
            }

            if (this.redstoneTorch.getValue()) {
                if (block instanceof BlockRedstoneTorch) {
                    event.setCanceled(true);
                }
            }

            if (this.redstoneLogic.getValue()) {
                if (block instanceof BlockRedstoneComparator || block instanceof BlockRedstoneRepeater) {
                    event.setCanceled(true);
                }
            }

            if (this.torches.getValue()) {
                if (block instanceof BlockTorch) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Listener
    public void onUpdateLighting(EventLightUpdate event) {
        if (this.light.getValue()) {
            event.setCanceled(true);
        }
    }

//    @Listener
//    public void onRenderBlockModel(EventRenderBlockModel event) {
//        if (this.pistons.getValue()) {
//            final Block block = event.getBlockState().getBlock();
//            if (block instanceof BlockPistonMoving || block instanceof BlockPistonExtension) {
//                event.setRenderable(false);
//                event.setCanceled(true);
//            }
//        }
//    }

//    @Listener
//    public void onRenderFluid(EventRenderFluid event) {
//        if (this.fluids.getValue()) {
//            event.setRenderable(false);
//            event.setCanceled(true);
//        }
//    }

    @Listener
    public void onRenderEntity(EventRenderEntity event) {
        if (event.getEntity() != null) {
            if (this.items.getValue()) {
                if (event.getEntity() instanceof EntityItem) {
                    if (this.itemsAll.getValue()) {
                        event.setCanceled(true);
                    } else {
                        final EntityItem entityItem = (EntityItem) event.getEntity();

                        if (this.itemsItemBlocks.getValue()) {
                            if (entityItem.getItem().getItem() instanceof ItemBlock) {
                                event.setCanceled(true);
                            }
                        }

                        this.itemsList.getValue().forEach(item -> {
                            if (Item.getIdFromItem(item) == Item.getIdFromItem(entityItem.getItem().getItem())) {
                                event.setCanceled(true);
                            }
                        });
                    }
                }
            }

            if (this.withers.getValue()) {
                if (event.getEntity() instanceof EntityWither)
                    event.setCanceled(true);
            }

            if (this.skulls.getValue()) {
                if (event.getEntity() instanceof EntityWitherSkull)
                    event.setCanceled(true);
            }

            if (this.crystals.getValue()) {
                if (event.getEntity() instanceof EntityEnderCrystal)
                    event.setCanceled(true);
            }

            if (this.tnt.getValue()) {
                if (event.getEntity() instanceof EntityTNTPrimed)
                    event.setCanceled(true);
            }

            if (this.fireworks.getValue()) {
                if (event.getEntity() instanceof EntityFireworkRocket)
                    event.setCanceled(true);
            }
        }
    }

    @Listener
    public void onSpawnEffectParticle(EventSpawnEffect event) {
        if (this.fireworksEffects.getValue()) {
            if (event.getParticleID() == EnumParticleTypes.FIREWORKS_SPARK.getParticleID() ||
                    event.getParticleID() == EnumParticleTypes.EXPLOSION_HUGE.getParticleID() ||
                    event.getParticleID() == EnumParticleTypes.EXPLOSION_LARGE.getParticleID() ||
                    event.getParticleID() == EnumParticleTypes.EXPLOSION_NORMAL.getParticleID()) {
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void onAddEffect(EventAddEffect event) {
        if (this.fireworksEffects.getValue()) {
            if (event.getParticle() instanceof ParticleFirework.Starter ||
                    event.getParticle() instanceof ParticleFirework.Spark ||
                    event.getParticle() instanceof ParticleFirework.Overlay) {
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void onRenderSky(EventRenderSky event) {
        if (this.sky.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onRenderName(EventRenderName event) {
        if (this.names.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onEntityAdd(EventAddEntity event) {
        if (this.fireworks.getValue()) {
            if (event.getEntity() instanceof EntityFireworkRocket) {
                event.setCanceled(true);
            }
        }

        if (this.withersForce.getValue()) {
            if (event.getEntity() instanceof EntityWither) {
                event.setCanceled(true);
            }
        }

        if (this.skulls.getValue()) {
            if (event.getEntity() instanceof EntityWitherSkull) {
                event.setCanceled(true);
            }
        }

        if (this.tnt.getValue()) {
            if (event.getEntity() instanceof EntityTNTPrimed) {
                event.setCanceled(true);
            }
        }

        if (this.storms.getValue()) { // fix this
            if (event.getEntity() instanceof EntityLightningBolt) {
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void onWorldLoad(EventLoadWorld event) {
        if (event.getWorld() == null)
            return;

        if (this.blocksList.getValue().isEmpty())
            this.blocksList.getValue().add(Block.getBlockFromName("stone"));

        if (this.itemsList.getValue().isEmpty())
            this.itemsList.getValue().add(Item.getByNameOrId("stick"));
    }

    @Listener
    public void onValueChanged(EventUIValueChanged event) {
        if (event.getValue().getAlias()[0].toLowerCase().startsWith("nolagblocks")) {
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
        }
    }

    @Listener
    public void onListValueChanged(EventUIListValueChanged event) {
        if (event.getValue().getAlias()[0].equalsIgnoreCase("nolagblockslist")) {
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
        }
    }
}
