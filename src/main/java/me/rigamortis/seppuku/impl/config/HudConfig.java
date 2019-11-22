package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.gui.hud.component.DraggableHudComponent;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.impl.gui.hud.anchor.AnchorPoint;
import me.rigamortis.seppuku.impl.management.ConfigManager;

import java.io.*;

/**
 * created by noil on 8/25/2019 at 12:23 PM
 */
public final class HudConfig extends Configurable {

    public HudConfig() {
        super(ConfigManager.CONFIG_PATH + "Hud.cfg");
    }

    @Override
    public void load() {
        try {
            final File file = new File(this.getPath());

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            final BufferedReader reader = new BufferedReader(new FileReader(this.getPath()));

            String line;
            while ((line = reader.readLine()) != null) {
                final String[] split = line.split(":");

                final HudComponent component = Seppuku.INSTANCE.getHudManager().findComponent(split[0]);
                if (component != null) {
                    if (!split[1].equals("")) {
                        component.setX(Float.valueOf(split[1]));
                    }
                    if (!split[2].equals("")) {
                        component.setY(Float.valueOf(split[2]));
                    }
                    if (!split[3].equals("")) {
                        component.setVisible(Boolean.valueOf(split[3]));
                    }
                    if (!split[4].equals("")) {
                        final DraggableHudComponent draggable = (DraggableHudComponent) component;
                        if (!split[4].equals("NULL_ANCHOR")) {
                            for (AnchorPoint anchorPoint : Seppuku.INSTANCE.getHudManager().getAnchorPoints()) {
                                if (anchorPoint.getPoint().equals(AnchorPoint.Point.valueOf(split[4]))) {
                                    draggable.setAnchorPoint(anchorPoint);
                                }
                            }
                        }
                        if (!split[5].equals("NULL_GLUED") && !split[6].equals("NULL_GLUE_SIDE")) {
                            draggable.setGlued((DraggableHudComponent) Seppuku.INSTANCE.getHudManager().findComponent(split[5]));
                            draggable.setGlueSide(DraggableHudComponent.GlueSide.valueOf(split[6]));
                        }
                    }
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try {
            final File file = new File(this.getPath());

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            final BufferedWriter writer = new BufferedWriter(new FileWriter(this.getPath()));

            if (Seppuku.INSTANCE.getHudManager().getComponentList() != null) {
                for (HudComponent component : Seppuku.INSTANCE.getHudManager().getComponentList()) {
                    writer.write(component.getName() + ":" + component.getX() + ":" + component.getY() + ":" + component.isVisible());
                    if (component instanceof DraggableHudComponent) {
                        final DraggableHudComponent draggable = (DraggableHudComponent) component;

                        // Anchor Point
                        if (draggable.getAnchorPoint() != null) {
                            writer.write(":" + draggable.getAnchorPoint().getPoint().name());
                        } else {
                            writer.write(":" + "NULL_ANCHOR");
                        }

                        // Glued
                        if (draggable.getGlued() != null) {
                            writer.write(":" + draggable.getGlued().getName() + ":" + draggable.getGlueSide());
                        } else {
                            writer.write(":" + "NULL_GLUED" + ":" + "NULL_GLUE_SIDE");
                        }
                    }
                    writer.newLine();
                }
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
