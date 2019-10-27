package me.rigamortis.seppuku.impl.fml.core;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

/**
 * Author Seth
 * 4/5/2019 @ 1:24 AM.
 */
public final class SeppukuAccessTransformer extends AccessTransformer {

    public SeppukuAccessTransformer() throws IOException {
        super("seppuku_at.cfg");
    }

}