package me.rigamortis.seppuku.api.module.notebot;

import me.rigamortis.seppuku.impl.module.world.NoteBotModule;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.io.File;

/**
 * @author noil
 */
public class NotePlayer {

    /*
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    */

    private Sequencer sequencer;
    private Sequence currentSequence;

    private String currentSongName = "";

    public NotePlayer() {
        //Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    public void begin(File file, NoteBotModule module) throws Exception {
        this.end();

        this.sequencer = MidiSystem.getSequencer(false);
        this.currentSequence = MidiSystem.getSequence(file);
        this.currentSongName = file.getName();
        this.sequencer.getTransmitter().setReceiver(module.getReceiver());
        this.sequencer.open();
        this.sequencer.setSequence(this.currentSequence);
        this.sequencer.start();
        this.sequencer.setMasterSyncMode(Sequencer.SyncMode.INTERNAL_CLOCK);
        this.sequencer.setTempoInBPM(120);

        //int resolution = this.currentSequence.getResolution();
    }

    public void end() {
        if (this.sequencer != null) {
            if (this.sequencer.isRunning())
                this.sequencer.stop();

            if (this.sequencer.isOpen())
                this.sequencer.close();

            this.sequencer = null;
        }

        if (this.currentSequence != null) {
            this.currentSequence = null;
        }
    }

    private String getNote(int note) {
        int octaveNote = note % 12;
        switch (octaveNote) {
            case 0:
                return "F#";
            case 1:
                return "G";
            case 2:
                return "G#";
            case 3:
                return "A";
            case 4:
                return "A#";
            case 5:
                return "B";
            case 6:
                return "C";
            case 7:
                return "C#";
            case 8:
                return "D";
            case 9:
                return "D#";
            case 10:
                return "E";
            case 11:
                return "F";
            case 12:
                return "Gb";
        }
        return "null";
    }

    public int getTempoInBPM(MetaMessage mm) {
        byte[] data = mm.getData();
        if (mm.getType() != 81 || data.length != 3) {
            throw new IllegalArgumentException("mm=" + mm);
        }
        int mspq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
        return Math.round(60000001f / mspq);
    }

    public Sequencer getSequencer() {
        return sequencer;
    }

    public Sequence getCurrentSequence() {
        return currentSequence;
    }

    public String getCurrentSongName() {
        return currentSongName;
    }
}
