package me.rigamortis.seppuku.api.module.notebot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author noil
 */
public class NotePlayer {

    private final List<Integer> notesToPlay = new ArrayList<>();

    public void play(int index) {
        this.notesToPlay.add(index);
    }

    public void rest(int index) {
        for (int i = 0; i < index; i++)
            this.notesToPlay.add(-1);
    }

    public void chord(int index) {
        switch (index) {
            case 1:
                play(1);
                play(5);
                play(8);
                break;
            case 2:
                play(2);
                play(6);
                play(9);
                break;
            case 3:
                play(3);
                play(6);
                play(10);
                break;
            case 4:
                play(4);
                play(6);
                play(10);
                break;
            case 5:
                play(5);
                play(7);
                play(12);
                break;
            case 6:
                play(6);
                play(10);
                play(13);
                break;
            case 7:
                play(7);
                play(10);
                play(14);
                break;
            case 8:
                play(8);
                play(12);
                play(15);
                break;
            case 9:
                play(9);
                play(13);
                play(16);
                break;
            case 10:
                play(10);
                play(13);
                play(17);
                break;
            case 11:
                play(11);
                play(15);
                play(18);
                break;
            case 12:
                play(12);
                play(16);
                play(19);
                break;
            case 13:
                play(13);
                play(17);
                play(20);
                break;
        }
    }

    public List<Integer> getNotesToPlay() {
        return this.notesToPlay;
    }
}
