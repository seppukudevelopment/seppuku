package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.module.notebot.Note;
import me.rigamortis.seppuku.api.module.notebot.NotePlayer;
import me.rigamortis.seppuku.api.task.rotation.RotationTask;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author noil
 */
public final class NoteBotModule extends Module {

    private final Value<BotState> state = new Value<BotState>("State", new String[]{"State", "s"}, "Current state of the note-bot.", BotState.IDLE);
    private final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"mod", "m"}, "Current mode of the note-bot.", Mode.AUTOMATIC);
    private final Value<Boolean> rotate = new Value<Boolean>("Rotate", new String[]{"rot", "r"}, "Rotate the player's head & body for each note-bot function.", true);
    private final Value<Boolean> swing = new Value<Boolean>("Swing", new String[]{"swingarm", "armswing", "sa"}, "Swing the player's hand for each note-bot function.", true);
    private final Value<Float> clickDelay = new Value<Float>("ClickDelay", new String[]{"Click Delay", "click-delay", "delay", "del", "cd", "d"}, "Delay(ms) to wait between clicks.", 200.0f, 0.0f, 1000.0f, 1.0f);

    private final RotationTask rotationTask = new RotationTask("NoteBot", 2);

    private BlockPos currentBlock;
    private int currentNote;

    private final int[] positionOffsets = new int[]{2, 1, 2};

    private final NotePlayer notePlayer = new NotePlayer();
    private final Timer timer = new Timer();

    private final List<BlockPos> blocks = new ArrayList<>();
    private final List<BlockPos> tunedBlocks = new ArrayList<>();
    private final Map<BlockPos, Note> discoveredBlocks = new HashMap<>();

    private final int BLOCK_AREA = 25;
    private final Minecraft mc = Minecraft.getMinecraft();

    public NoteBotModule() {
        super("NoteBot", new String[]{"NoteBot+", "MusicBot", "MusicPlayer", "MidiPlayer", "MidiBot"}, "Play .midi files on a 5x5 grid of note-blocks.", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mc.world == null)
            return;

        IntStream.range(0, BLOCK_AREA).forEach(note -> {
            int[] area = this.blockArea(note);
            this.blocks.add(new BlockPos(area[0], area[1], area[2]));
        });

        if (this.mode.getValue().equals(Mode.AUTOMATIC)) {
            this.state.setEnumValue("DISCOVERING");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.world == null)
            return;

        this.clearData();
        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
    }

    @Override
    public String getMetaData() {
        return this.state.getValue().name();
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            this.toggle(); // toggle off
            Seppuku.INSTANCE.logChat("\247rToggled\2477 " + this.getDisplayName() + " \247coff\247r, as you've loaded into a new world.");
        }
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() != EventStageable.EventStage.POST)
            return;

        if (!(event.getPacket() instanceof SPacketBlockAction))
            return;

        SPacketBlockAction packetBlockAction = (SPacketBlockAction) event.getPacket();
        BlockPos position = packetBlockAction.getBlockPosition();
        this.blocks.stream().filter(blockPos -> this.correctPosition(position, this.blocks.indexOf(blockPos))).forEach(blockPos -> {
            final Note note = new Note(this.blocks.indexOf(blockPos), position, packetBlockAction.getData1(), packetBlockAction.getData2());
            if (!this.discoveredBlocks.containsKey(blockPos)) {
                this.discoveredBlocks.put(blockPos, note);
            } else {
                if (!this.tunedBlocks.contains(blockPos) && this.blocks.indexOf(blockPos) == packetBlockAction.getData2()) {
                    this.tunedBlocks.add(blockPos);
                }
            }
        });
    }

    @Listener
    public void onMotionUpdate(EventUpdateWalkingPlayer event) {
        if (mc.world == null || mc.player == null)
            return;

        if (mc.player.capabilities.isCreativeMode) {
            if (this.rotationTask.isOnline())
                Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);

            return;
        }

        switch (event.getStage()) {
            case PRE:
                if (this.state.getValue() == BotState.PLAYING && this.notePlayer.getNotesToPlay().size() > 0) {
                    int playingNote = this.notePlayer.getNotesToPlay().get(this.currentNote) % 24;
                    if (playingNote != -1) {
                        this.currentBlock = new BlockPos(this.getPosition(playingNote));
                        this.lookAtPosition(this.currentBlock);
                    }
                }

                if (this.mode.getValue().equals(Mode.AUTOMATIC)) {
                    if ((this.discoveredBlocks.size() == BLOCK_AREA && this.tunedBlocks.size() == BLOCK_AREA)) {
                        this.state.setEnumValue("IDLE");
                    }
                }

                if (this.currentBlock == null) {
                    this.blocks.stream().filter(blockPos -> (!this.discoveredBlocks.containsKey(blockPos) || !this.tunedBlocks.contains(blockPos))).forEach(blockPos -> {
                        final BlockPos workPos = new BlockPos(this.getPosition(this.blocks.indexOf(blockPos)));

                        if (!this.discoveredBlocks.containsKey(blockPos)) {
                            if (this.mode.getValue().equals(Mode.AUTOMATIC)) {
                                this.state.setEnumValue("DISCOVERING");
                            }
                            this.currentBlock = new BlockPos(workPos);
                            if (this.rotate.getValue()) {
                                this.lookAtPosition(workPos);
                            }
                        } else if (this.discoveredBlocks.size() == BLOCK_AREA) {
                            if (this.mode.getValue().equals(Mode.AUTOMATIC)) {
                                this.state.setEnumValue("TUNING");
                            }
                            this.currentBlock = new BlockPos(workPos);
                            if (this.rotate.getValue()) {
                                this.lookAtPosition(workPos);
                            }
                        }
                    });
                }
                break;
            case POST:
                if (this.rotationTask.isOnline() || !this.rotate.getValue()) {
                    final EnumFacing direction = EnumFacing.UP;

                    switch (this.state.getValue()) {
                        case IDLE:
                            if (this.rotationTask.isOnline()) {
                                Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
                            }
                            return;
                        case DISCOVERING:
                            if (this.discoveredBlocks.size() != BLOCK_AREA) {
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.currentBlock, direction));
                                if (this.swing.getValue()) {
                                    mc.player.swingArm(EnumHand.MAIN_HAND);
                                }
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, direction));
                                this.currentBlock = null;
                            }
                            break;
                        case TUNING:
                            if (this.tunedBlocks.size() != BLOCK_AREA && !this.tunedBlocks.contains(this.currentBlock)) {
                                if (this.timer.passed(this.clickDelay.getValue())) {
                                    mc.playerController.processRightClickBlock(mc.player, mc.world, this.currentBlock, direction, new Vec3d(0.5F, 0.5F, 0.5F), EnumHand.MAIN_HAND);
                                    if (this.swing.getValue()) {
                                        mc.player.swingArm(EnumHand.MAIN_HAND);
                                    }
                                    this.currentBlock = null;
                                    this.timer.reset();
                                }
                            } else {
                                this.currentBlock = null;
                            }
                            break;
                        case PLAYING:
                            if (this.currentNote >= this.notePlayer.getNotesToPlay().size()) {
                                this.currentNote = 0;
                                return;
                            }
                            this.currentNote++;
                            if (this.currentNote != -1) {
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.currentBlock, direction));
                                if (this.swing.getValue()) {
                                    mc.player.swingArm(EnumHand.MAIN_HAND);
                                }
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, direction));
                                this.currentBlock = null;
                            }
                            break;
                    }
                }
                break;
        }
    }

    private int[] blockArea(int index) {
        int[] positions = {(int) Math.floor(mc.player.posX) - this.positionOffsets[0], (int) Math.floor(mc.player.posY) - this.positionOffsets[1], (int) Math.floor(mc.player.posZ) - this.positionOffsets[2]};
        return new int[]{positions[0] + index % 5, positions[1], positions[2] + index / 5};
    }

    private void lookAtPosition(BlockPos position) {
        Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
        if (this.rotationTask.isOnline()) {
            final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(position.getX() + 0.5f, position.getY() + 0.5f, position.getZ() + 0.5f));
            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);
        }
    }

    private BlockPos getPosition(int note) {
        int[] blocks = this.blockArea(note);
        return new BlockPos(blocks[0], blocks[1], blocks[2]);
    }

    private boolean correctPosition(BlockPos blockPos, int index) {
        int[] blocks = this.blockArea(index);
        return (blockPos.getX() == blocks[0] && blockPos
                .getY() == blocks[1] && blockPos
                .getZ() == blocks[2]);
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

    private void clearData() {
        this.discoveredBlocks.clear();
        if (!this.mode.getValue().equals(Mode.DEBUG)) { // is not debug, so let's wipe our previously tuned blocks data
            this.tunedBlocks.clear();
        }
        this.blocks.clear();
        this.notePlayer.getNotesToPlay().clear();
        this.currentBlock = null;
    }

    public enum BotState {
        IDLE, DISCOVERING, TUNING, PLAYING;
    }

    public enum Mode {
        AUTOMATIC, MANUAL, DEBUG
    }

    public Value<BotState> getState() {
        return state;
    }

    public NotePlayer getNotePlayer() {
        return notePlayer;
    }

    public int getCurrentNote() {
        return currentNote;
    }

    public void setCurrentNote(int currentNote) {
        this.currentNote = currentNote;
    }
}
