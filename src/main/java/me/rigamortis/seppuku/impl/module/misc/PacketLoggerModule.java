package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import net.minecraft.network.Packet;
import net.minecraft.util.StringUtils;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.lang.reflect.Field;

/**
 * created by noil on 8/3/2019 at 6:32 PM
 */
public final class PacketLoggerModule extends Module {

    private Packet[] packets;

    public final BooleanValue incoming = new BooleanValue("Incoming", new String[]{"in"}, true);

    public final BooleanValue outgoing = new BooleanValue("Outgoing", new String[]{"out"}, true);

    public final BooleanValue chat = new BooleanValue("Chat", new String[]{"ch"}, true);

    public final BooleanValue console = new BooleanValue("Console", new String[]{"con"}, true);

    public final BooleanValue data = new BooleanValue("Data", new String[]{"dat"}, true);

    public PacketLoggerModule() {
        super("PacketLogger", new String[]{"pktlgr"}, "Log incoming and/or outgoing packets to console.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onToggle() {
        super.onToggle();
        this.packets = null;
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (this.incoming.getBoolean()) {
            if (event.getStage() == EventStageable.EventStage.PRE) {
                if (this.console.getBoolean()) {
                    System.out.println("\2477IN: \247r" + event.getPacket().getClass().getSimpleName() + " {");

                    if (this.data.getBoolean()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        System.out.println(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("}");
                }

                if (this.chat.getBoolean()) {
                    Seppuku.INSTANCE.logChat("\2477IN: \247r" + event.getPacket().getClass().getSimpleName() + " {");

                    if (this.data.getBoolean()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        Seppuku.INSTANCE.logChat(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Seppuku.INSTANCE.logChat("}");
                }
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (this.outgoing.getBoolean()) {
            if (event.getStage() == EventStageable.EventStage.PRE) {
                if (this.console.getBoolean()) {
                    System.out.println("\2477OUT: \247r" + event.getPacket().getClass().getSimpleName() + " {");

                    if (this.data.getBoolean()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        System.out.println(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("}");
                }

                if (this.chat.getBoolean()) {
                    Seppuku.INSTANCE.logChat("\2477OUT: \247r" + event.getPacket().getClass().getSimpleName() + " {");

                    if (this.data.getBoolean()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        Seppuku.INSTANCE.logChat(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Seppuku.INSTANCE.logChat("}");
                }
            }
        }
    }
}
