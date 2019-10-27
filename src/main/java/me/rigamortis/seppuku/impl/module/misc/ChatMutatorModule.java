package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.OptionalValue;
import me.rigamortis.seppuku.impl.module.hidden.CommandsModule;
import net.minecraft.network.play.client.CPacketChatMessage;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Random;

/**
 * Author Seth
 * 4/19/2019 @ 11:59 PM.
 */
public final class ChatMutatorModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"L33t", "Fancy", "Retard", "Console"});

    public ChatMutatorModule() {
        super("ChatMutator", new String[]{"ChatMutate", "ChatM"}, "Modify your outgoing chat messages", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketChatMessage) {
                final CPacketChatMessage packet = (CPacketChatMessage) event.getPacket();

                final CommandsModule cmds = (CommandsModule) Seppuku.INSTANCE.getModuleManager().find(CommandsModule.class);

                if (cmds != null) {
                    if (packet.getMessage().startsWith("/") || packet.getMessage().startsWith(cmds.prefix.getString())) {
                        return;
                    }

                    switch (this.mode.getInt()) {
                        case 0:
                            packet.message = leetSpeak(packet.message);
                            break;
                        case 1:
                            packet.message = fancy(packet.message);
                            break;
                        case 2:
                            packet.message = retard(packet.message);
                            break;
                        case 3:
                            packet.message = console(packet.message);
                            break;
                    }
                }
            }
        }
    }

    public String leetSpeak(String input) {
        input = input.toLowerCase().replace("a", "4");
        input = input.toLowerCase().replace("e", "3");
        input = input.toLowerCase().replace("g", "9");
        input = input.toLowerCase().replace("h", "1");
        input = input.toLowerCase().replace("o", "0");
        input = input.toLowerCase().replace("s", "5");
        input = input.toLowerCase().replace("t", "7");
        input = input.toLowerCase().replace("i", "1");
        return input;
    }

    public String fancy(String input) {
        final StringBuilder sb = new StringBuilder();

        for (char c : input.toCharArray()) {
            if(c >= 0x21 && c <= 0x80) {
                sb.append(Character.toChars(c + 0xFEE0));
            }else{
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public String retard(String input) {
        final StringBuilder sb = new StringBuilder(input);

        for (int i = 0; i < sb.length(); i += 2) {
            sb.replace(i, i + 1, sb.substring(i, i + 1).toUpperCase());
        }

        return sb.toString();
    }

    public String console(String input) {
        String ret = "";

        final char[] unicodeChars = new char[] {'\u2E3B',
                '\u26D0',
                '\u26E8',
                '\u26BD',
                '\u26BE',
                '\u26F7',
                '\u23EA',
                '\u23E9',
                '\u23EB',
                '\u23EC',
                '\u2705',
                '\u274C',
                '\u26C4'};

        final int length = input.length();

        for (int i = 1, current = 0; i <= length || current < length; current = i, i += 1) {
            if(current != 0) {
                final Random random = new Random();

                for(int j = 0; j <= 2; j++) {
                    ret += unicodeChars[random.nextInt(unicodeChars.length)];
                }
            }
            if(i <= length) {
                ret += input.substring(current, i);
            } else {
                ret += input.substring(current);
            }
        }

        return ret;
    }

}
