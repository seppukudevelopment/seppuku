package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.minecraft.EventRunTick;
import me.rigamortis.seppuku.api.event.world.EventRemoveEntity;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;
import java.util.ArrayList;
import java.util.List;


/**
 * @author jvyden
 * @since 6/24/20
 */
public class TotemNotifierModule extends Module {
    final Minecraft mc = Minecraft.getMinecraft();

    public TotemNotifierModule() {
        super("TotemNotifier", new String[]{"tm"}, "Notifies you when others pop totems.", "NONE", -1, ModuleType.COMBAT);
    }

    public List<Integer> EntitiesWithTotems = new ArrayList<>();

    @Listener
    public void runTick(EventRunTick event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            for(Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityLivingBase) {
                    final Iterable<ItemStack> stacks = entity.getEquipmentAndArmor();
                    for(ItemStack stack : stacks) {
                        final Item offhandItem = ((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).getItem();
                        if(offhandItem == Items.TOTEM_OF_UNDYING) {
                            if(!EntitiesWithTotems.contains(entity.getEntityId())) {
                                EntitiesWithTotems.add(entity.getEntityId());
                            }
                        } else if(offhandItem == Items.AIR) {
                            if(EntitiesWithTotems.contains(entity.getEntityId())) {
                                Seppuku.INSTANCE.getNotificationManager().addNotification("", entity.getName() + " just popped a totem.");
                                EntitiesWithTotems.removeIf(i -> i.equals(entity.getEntityId()));
                            }
                        }
                    }
                }
            }
        }
    }
    public void onEntityRemove(EventRemoveEntity event) {
        if(EntitiesWithTotems.contains(event.getEntity().getEntityId())) {
            EntitiesWithTotems.removeIf(i -> i.equals(event.getEntity().getEntityId()));
        }
    }
}
