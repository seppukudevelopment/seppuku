package me.rigamortis.seppuku.api.gui.menu;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.util.AuthUtil;
import me.rigamortis.seppuku.impl.gui.menu.GuiAltManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author noil
 */
public final class GuiListAlt extends GuiListExtended {

    private final List<GuiEntryAlt> entries = new ArrayList<>();
    private int selected = -1;

    public GuiListAlt(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, 24);
    }

    public void login(final GuiAltManager guiAltManager) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final GuiEntryAlt guiEntryAlt = GuiListAlt.this.getSelected();
                String status = "Logging in...";

                if (guiEntryAlt != null) {
                    if (guiEntryAlt.getAlt().getEmail().isEmpty()) {
                        if (guiEntryAlt.getAlt().isPremium()) {
                            status = AuthUtil.loginPassword(guiEntryAlt.getAlt().getUsername(), guiEntryAlt.getAlt().getPassword());
                        } else {
                            AuthUtil.loginPasswordOffline(guiEntryAlt.getAlt().getUsername());
                            status = "Success (offline account)";
                        }
                    } else {
                        status = AuthUtil.loginPassword(guiEntryAlt.getAlt().getEmail(), guiEntryAlt.getAlt().getPassword());
                    }
                }

                guiAltManager.setStatus(ChatFormatting.GRAY + status);
            }
        });
    }

    public void setAlts(List<AltData> alts) {
        this.selected = -1;
        this.entries.clear();

        for (AltData alt : alts) {
            this.entries.add(new GuiEntryAlt(this, alt));
        }
    }

    public GuiEntryAlt getListEntry(int index) {
        return this.entries.get(index);
    }

    protected boolean isSelected(int index) {
        return (index == this.selected);
    }

    public boolean hasSelected() {
        return (this.selected >= 0 && this.selected < this.entries.size());
    }

    public GuiEntryAlt getSelected() {
        return this.hasSelected() ? this.entries.get(this.selected) : null;
    }

    public void setSelected(int index) {
        this.selected = index;
    }

    public int getSize() {
        return this.entries.size();
    }
}
