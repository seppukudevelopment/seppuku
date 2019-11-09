package me.rigamortis.seppuku.api.event.world;

import net.minecraft.world.chunk.Chunk;

/**
 * created by noil on 11/3/19 at 5:05 PM
 */
public class EventChunk {

    private final ChunkType type;

    private final Chunk chunk;

    public EventChunk(ChunkType type, Chunk chunk) {
        this.type = type;
        this.chunk = chunk;
    }

    public ChunkType getType() {
        return type;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public static enum ChunkType {
        LOAD, UNLOAD;

        private ChunkType() {
        }
    }

}
