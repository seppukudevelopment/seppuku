package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ReflectionUtil;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.gui.hud.GuiHudEditor;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;
import me.rigamortis.seppuku.impl.gui.hud.component.*;
import me.rigamortis.seppuku.impl.gui.hud.component.graph.FpsGraphComponent;
import me.rigamortis.seppuku.impl.gui.hud.component.graph.MovementGraphComponent;
import me.rigamortis.seppuku.impl.gui.hud.component.graph.TpsGraphComponent;
import me.rigamortis.seppuku.impl.gui.hud.component.module.ModuleListComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Author Seth
 * 7/25/2019 @ 6:20 AM.
 */
public final class HudManager {

    private final FirstLaunchComponent firstLaunchComponent;
    private List<HudComponent> componentList = new CopyOnWriteArrayList<>();
    private List<AnchorPoint> anchorPoints = new ArrayList<>();

    public HudManager() {
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        final AnchorPoint TOP_LEFT = new AnchorPoint(AnchorPoint.Point.TOP_LEFT);
        final AnchorPoint TOP_RIGHT = new AnchorPoint(AnchorPoint.Point.TOP_RIGHT);
        final AnchorPoint BOTTOM_LEFT = new AnchorPoint(AnchorPoint.Point.BOTTOM_LEFT);
        final AnchorPoint BOTTOM_RIGHT = new AnchorPoint(AnchorPoint.Point.BOTTOM_RIGHT);
        final AnchorPoint TOP_CENTER = new AnchorPoint(AnchorPoint.Point.TOP_CENTER);
        final AnchorPoint BOTTOM_CENTER = new AnchorPoint(AnchorPoint.Point.BOTTOM_CENTER);
        this.anchorPoints.add(TOP_LEFT);
        this.anchorPoints.add(TOP_RIGHT);
        this.anchorPoints.add(BOTTOM_LEFT);
        this.anchorPoints.add(BOTTOM_RIGHT);
        this.anchorPoints.add(TOP_CENTER);
        this.anchorPoints.add(BOTTOM_CENTER);

        for (AnchorPoint anchorPoint : this.anchorPoints)
            anchorPoint.updatePosition(sr);

        int moduleListXOffset = 0;
        int moduleListYOffset = 0;
        for (Module.ModuleType type : Module.ModuleType.values()) {
            if (type.equals(Module.ModuleType.HIDDEN) || type.equals(Module.ModuleType.UI))
                continue;

            final ModuleListComponent moduleList = new ModuleListComponent(type);
            if ((moduleList.getX() + moduleListXOffset) > sr.getScaledWidth()) {
                moduleListXOffset = 0;
                moduleListYOffset += moduleList.getH() + 4 /* gap above and below each column */;
            }

            moduleList.setX(moduleList.getX() + moduleListXOffset);
            if (moduleListYOffset != 0) {
                moduleList.setY(moduleList.getY() + moduleListYOffset);
            }

            add(moduleList);

            moduleListXOffset += moduleList.getW() + 4 /* gap between each list */;
        }

        add(new ParticlesComponent());
        add(new WatermarkComponent());
        add(new EnabledModsComponent(TOP_RIGHT)); // creates the enabled mods component & by default anchors in the top right (to aid new users)
        add(new TpsComponent());
        add(new PotionEffectsComponent());
        add(new FpsComponent());
        add(new CoordsComponent());
        add(new NetherCoordsComponent());
        add(new SpeedComponent());
        add(new ArmorComponent());
        add(new PingComponent());
        add(new ServerBrandComponent());
        add(new BiomeComponent());
        add(new DirectionComponent());
        add(new PacketTimeComponent());
        add(new TimeComponent());
        add(new EnemyPotionsComponent());
        add(new CompassComponent());
        add(new HubComponent());
        add(new InventoryComponent());
        add(new TotemCountComponent());
        add(new TutorialComponent());
        add(new HoleOverlayComponent());
        add(new PlayerCountComponent());
        add(new OverViewComponent());
        add(new RearViewComponent());
        add(new EntityListComponent());
        add(new TpsGraphComponent());
        add(new MovementGraphComponent());
        add(new ColorsComponent());
        add(new BattleInfoComponent());
        add(new FpsGraphComponent());
        add(new KeyboardComponent());

        TrayComponent trayComponent = new TrayComponent();
        trayComponent.setAnchorPoint(BOTTOM_CENTER);
        add(trayComponent);

        NotificationsComponent notificationsComponent = new NotificationsComponent();
        notificationsComponent.setAnchorPoint(TOP_CENTER);
        add(notificationsComponent);

        this.loadExternalHudComponents();

        // Organize alphabetically
        this.componentList = this.componentList.stream().sorted((obj1, obj2) -> obj1.getName().compareTo(obj2.getName())).collect(Collectors.toList());

        // Create first launch component
        this.firstLaunchComponent = new FirstLaunchComponent();

        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    /**
     * Find all fields within the hud component that are values
     * and add them to the list of values inside of the hud component
     *
     * @param component the HudComponent to add
     */
    public void add(HudComponent component) {
        try {
            for (Field field : component.getClass().getDeclaredFields()) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    final Value val = (Value) field.get(component);
                    component.getValueList().add(val);
                }
            }
            this.componentList.add(component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update our anchor point positions when we render
     *
     * @param event
     */
    @Listener
    public void onRender(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (this.firstLaunchComponent != null && mc.world != null) {
            if (Seppuku.INSTANCE.getConfigManager().isFirstLaunch()) {
                if (mc.currentScreen instanceof GuiHudEditor) {
                    firstLaunchComponent.onClose();
                } else if (firstLaunchComponent.isVisible()) {
                    firstLaunchComponent.render(0, 0, event.getPartialTicks());
                }
            }
        }

        final int chatHeight = (mc.currentScreen instanceof GuiChat) ? 14 : 0;

        for (AnchorPoint point : this.anchorPoints) {
            if (point.getPoint() == AnchorPoint.Point.TOP_LEFT) {
                point.setX(2);
                point.setY(2);
            }
            if (point.getPoint() == AnchorPoint.Point.TOP_RIGHT) {
                point.setX(event.getScaledResolution().getScaledWidth() - 2);
                point.setY(2);
            }
            if (point.getPoint() == AnchorPoint.Point.BOTTOM_LEFT) {
                point.setX(2);
                point.setY(event.getScaledResolution().getScaledHeight() - chatHeight - 2);
            }
            if (point.getPoint() == AnchorPoint.Point.BOTTOM_RIGHT) {
                point.setX(event.getScaledResolution().getScaledWidth() - 2);
                point.setY(event.getScaledResolution().getScaledHeight() - chatHeight - 2);
            }
            if (point.getPoint() == AnchorPoint.Point.TOP_CENTER) {
                point.setX(event.getScaledResolution().getScaledWidth() / 2.0f);
                point.setY(2);
            }
            if (point.getPoint() == AnchorPoint.Point.BOTTOM_CENTER) {
                point.setX(event.getScaledResolution().getScaledWidth() / 2.0f);
                point.setY(event.getScaledResolution().getScaledHeight() - 2);
            }
        }
    }

    public void loadExternalHudComponents() {
        try {
            final File dir = new File("Seppuku/Hud");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (Class clazz : ReflectionUtil.getClassesEx(dir.getPath())) {
                if (clazz != null) {
                    if (HudComponent.class.isAssignableFrom(clazz)) {
                        final HudComponent component = (HudComponent) clazz.newInstance();
                        this.componentList.add(component);
                        Seppuku.INSTANCE.getLogger().log(Level.INFO, "Found external hud component " + component.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void moveToTop(HudComponent component) {
        for (HudComponent comp : this.componentList) {
            if (comp != null && comp == component) {
                this.componentList.remove(comp);
                this.componentList.add(comp);
                break;
            }
        }
    }

    public void unload() {
        this.anchorPoints.clear();
        this.componentList.clear();
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }

    public AnchorPoint findPoint(AnchorPoint.Point point) {
        for (AnchorPoint anchorPoint : this.anchorPoints) {
            if (anchorPoint.getPoint() == point) {
                return anchorPoint;
            }
        }
        return null;
    }

    public HudComponent findComponent(String componentName) {
        for (HudComponent component : this.componentList) {
            if (componentName.equalsIgnoreCase(component.getName())) {
                return component;
            }
        }
        return null;
    }

    public HudComponent findComponent(Class componentClass) {
        for (HudComponent component : this.componentList) {
            if (component.getClass() == componentClass) {
                return component;
            }
        }
        return null;
    }

    public List<AnchorPoint> getAnchorPoints() {
        return anchorPoints;
    }

    public void setAnchorPoints(List<AnchorPoint> anchorPoints) {
        this.anchorPoints = anchorPoints;
    }

    public List<HudComponent> getComponentList() {
        return this.componentList.stream().sorted((obj1, obj2) -> obj1.getName().compareTo(obj2.getName())).collect(Collectors.toList());
    }

    public void setComponentList(List<HudComponent> componentList) {
        this.componentList = componentList;
    }
}
