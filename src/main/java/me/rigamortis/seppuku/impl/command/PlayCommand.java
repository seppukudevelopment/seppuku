package me.rigamortis.seppuku.impl.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.impl.module.world.NoteBotModule;

import java.io.File;

/**
 * @author noil
 */
public final class PlayCommand extends Command {

    private final File directory;

    public PlayCommand() {
        super("Play", new String[]{"playsong", "begin"}, "Plays a song file from your /Seppuku/Songs/ directory.", ".play <song file name>");

        this.directory = new File(Seppuku.INSTANCE.getConfigManager().getConfigDir(), "Songs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final NoteBotModule notebotModule = (NoteBotModule) Seppuku.INSTANCE.getModuleManager().find(NoteBotModule.class);
        if (notebotModule != null) {
            try {
                // check for .mid or .midi
                if (split[1].contains(".mid"))
                    split[1] = split[1].replaceAll(".mid", "");

                if (split[1].contains(".midi"))
                    split[1] = split[1].replaceAll(".midi", "");

                File midiFile = new File(directory, split[1] + ".mid");
                if (!midiFile.exists()) {
                    midiFile = new File(directory, split[1] + ".midi");
                }

                // now check if the midi file exists
                if (midiFile.exists()) {
                    notebotModule.getState().setEnumValue("PLAYING");
                    notebotModule.getNotePlayer().begin(midiFile, notebotModule);
                    Seppuku.INSTANCE.logChat("Playing '" + ChatFormatting.YELLOW + midiFile.getName() + ChatFormatting.RESET + "'");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
