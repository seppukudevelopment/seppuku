package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.notification.Notification;
import me.rigamortis.seppuku.api.util.ReflectionUtil;
import me.rigamortis.seppuku.impl.config.HudConfig;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;
import me.rigamortis.seppuku.impl.gui.hud.component.*;
import me.rigamortis.seppuku.impl.module.render.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.server.SPacketJoinGame;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Author Seth
 * 7/25/2019 @ 6:20 AM.
 */
public final class HudManager {

    private List<HudComponent> componentList = new CopyOnWriteArrayList<>();
    private List<AnchorPoint> anchorPoints = new ArrayList<>();

    public HudManager() {
        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        final AnchorPoint TOP_LEFT = new AnchorPoint(2, 2, AnchorPoint.Point.TOP_LEFT);
        final AnchorPoint TOP_RIGHT = new AnchorPoint(res.getScaledWidth() - 2, 2, AnchorPoint.Point.TOP_RIGHT);
        final AnchorPoint BOTTOM_LEFT = new AnchorPoint(2, res.getScaledHeight() - 2, AnchorPoint.Point.BOTTOM_LEFT);
        final AnchorPoint BOTTOM_RIGHT = new AnchorPoint(res.getScaledWidth() - 2, res.getScaledHeight() - 2, AnchorPoint.Point.BOTTOM_RIGHT);
        final AnchorPoint TOP_CENTER = new AnchorPoint(res.getScaledWidth() / 2, 2, AnchorPoint.Point.TOP_CENTER);
        this.anchorPoints.add(TOP_LEFT);
        this.anchorPoints.add(TOP_RIGHT);
        this.anchorPoints.add(BOTTOM_LEFT);
        this.anchorPoints.add(BOTTOM_RIGHT);
        this.anchorPoints.add(TOP_CENTER);

        this.componentList.add(new WatermarkComponent());
        this.componentList.add(new ArrayListComponent());
        this.componentList.add(new TpsComponent());
        this.componentList.add(new PotionEffectsComponent());
        this.componentList.add(new FpsComponent());
        this.componentList.add(new CoordsComponent());
        this.componentList.add(new NetherCoordsComponent());
        this.componentList.add(new SpeedComponent());
        this.componentList.add(new ArmorComponent());
        this.componentList.add(new PingComponent());
        this.componentList.add(new ServerBrandComponent());
        this.componentList.add(new BiomeComponent());
        this.componentList.add(new DirectionComponent());
        this.componentList.add(new PacketTimeComponent());
        this.componentList.add(new TimeComponent());
        this.componentList.add(new EnemyPotionsComponent());
        this.componentList.add(new CompassComponent());
        this.componentList.add(new HubComponent());
        this.componentList.add(new InventoryComponent());
        this.componentList.add(new TotemCountComponent());
        this.componentList.add(new TutorialComponent());

        NotificationsComponent notifactionComponent = new NotificationsComponent();
        notifactionComponent.setAnchorPoint(TOP_RIGHT);
        this.componentList.add(notifactionComponent);

        this.loadExternalHudComponents();

        // Organize alphabetically
        this.componentList = this.componentList.stream().sorted((obj1, obj2) -> obj1.getName().compareTo(obj2.getName())).collect(Collectors.toList());

        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    /**
     * Update our anchor point positions when we render
     *
     * @param event
     */
    @Listener
    public void onRender(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

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
                point.setX(event.getScaledResolution().getScaledWidth() / 2);
                point.setY(2);
            }
        }
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage().equals(EventStageable.EventStage.POST)) {
            if (event.getPacket() instanceof SPacketJoinGame) {
                if (HudConfig.FIRST_HUD_RUN) {
                    final Module hudModule = Seppuku.INSTANCE.getModuleManager().find(HudModule.class);
                    final HudComponent tutorialComponent = this.findComponent(TutorialComponent.class);

                    if (hudModule != null) {
                        if (!hudModule.isEnabled()) {
                            hudModule.toggle();
                        }
                    }

                    if (tutorialComponent != null) {
                        tutorialComponent.setVisible(true);
                    }

                    Seppuku.INSTANCE.getNotificationManager().addNotification("New Hud Editor! .bind HudEditor <key>", "New Hud Editor! .bind HudEditor <key>", Notification.Type.INFO, 20000);
                    HudConfig.FIRST_HUD_RUN = false;
                    Seppuku.INSTANCE.getConfigManager().saveAll();
                }
            }
        }
    }

    public void loadExternalHudComponents() {
        try {
            final File dir = new File("Seppuku 1.12.2/Hud");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (Class clazz : ReflectionUtil.getClassesEx(dir.getPath())) {
                if (clazz != null) {
                    if (HudComponent.class.isAssignableFrom(clazz)) {
                        final HudComponent component = (HudComponent) clazz.newInstance();

                        if (component != null) {
                            this.componentList.add(component);
                            System.out.println("[Seppuku] Found external hud component " + component.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void moveToTop(HudComponent component) {
        final Iterator it = this.componentList.iterator();

        while (it.hasNext()) {
            final HudComponent comp = (HudComponent) it.next();
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
