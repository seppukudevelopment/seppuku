package me.rigamortis.seppuku.impl.gui.menu;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.gui.menu.AltData;
import me.rigamortis.seppuku.api.gui.menu.GuiAddAlt;
import me.rigamortis.seppuku.api.gui.menu.GuiListAlt;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author noil
 */
public final class GuiAltManager extends GuiScreen implements GuiYesNoCallback {

    private final GuiScreen parent;
    private GuiListAlt guiListAlt;
    private GuiButton login;
    private GuiButton remove;
    private String status = "Idle";

    public GuiAltManager(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.guiListAlt = new GuiListAlt(this.mc, this.width, this.height, 32, this.height - 64, 36);
        this.updateAlts();

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 60 - 2, this.height - 54, 60, 20, "Add"));
        this.buttonList.add(this.remove = new GuiButton(1, this.width / 2 + 2, this.height - 54, 60, 20, "Remove"));
        this.buttonList.add(this.login = new GuiButton(2, this.width / 2 + 2, this.height - 30, 60, 20, "Login"));
        this.buttonList.add(new GuiButton(3, this.width / 2 + 40 + 4 + 20, this.height - 30, 20, 20, "..."));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 60 - 2, this.height - 30, 60, 20, "Back"));

        this.login.enabled = this.guiListAlt.hasSelected();
        this.remove.enabled = this.guiListAlt.hasSelected();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.guiListAlt.drawScreen(mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.mc.fontRenderer.drawStringWithShadow(mc.getSession().getUsername(), 2.0F, 2.0F, 0xFFAAAAAA);
        this.mc.fontRenderer.drawStringWithShadow(this.status, (this.width - this.mc.fontRenderer.getStringWidth(this.status) - 2), 2.0F, 0xFFAAAAAA);
        final String accounts = Seppuku.INSTANCE.getAltManager().getAlts().size() + " " + ((Seppuku.INSTANCE.getAltManager().getAlts().size() == 1) ? "Account" : "Accounts");
        this.mc.fontRenderer.drawStringWithShadow(accounts, (this.width - this.mc.fontRenderer.getStringWidth(accounts) - 2), (2 + this.mc.fontRenderer.FONT_HEIGHT), 0xFFAAAAAA);

        this.login.enabled = this.guiListAlt.hasSelected();
        this.remove.enabled = this.guiListAlt.hasSelected();
    }

    @Override
    @ParametersAreNonnullByDefault
    public void actionPerformed(GuiButton button) throws IOException {
        GuiAddAlt guiAddAlt;
        super.actionPerformed(button);
        switch (button.id) {
            case 0:
                guiAddAlt = new GuiAddAlt(this);
                mc.displayGuiScreen(guiAddAlt);
                break;
            case 1:
                if (this.guiListAlt.hasSelected() && this.guiListAlt.getSelected() != null) {
                    mc.displayGuiScreen((GuiScreen) new GuiYesNo(this, "Remove account '" + this.guiListAlt.getSelected().getAlt().getUsername() + "'?", "", "Yes", "No", 0));
                }
                break;
            case 2:
                if (this.guiListAlt.hasSelected()) {
                    this.guiListAlt.login(this);
                }
                break;
            case 3:
                this.importAccounts();
                break;
            case 4:
                mc.displayGuiScreen(this.parent);
                break;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == 28 || keyCode == 205) { // ENTER
            this.guiListAlt.login(this);
        }

        if (keyCode == 211) { // DEL
            if (this.guiListAlt.getSelected() != null) {
                Seppuku.INSTANCE.getAltManager().getAlts().remove(this.guiListAlt.getSelected().getAlt());
            }
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        super.confirmClicked(result, id);
        if (result && id == 0) {
            if (this.guiListAlt.getSelected() != null) {
                Seppuku.INSTANCE.getAltManager().removeAlt(this.guiListAlt.getSelected().getAlt());
            }
            this.updateAlts();
        }
        this.mc.displayGuiScreen(this);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.guiListAlt.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.guiListAlt.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.guiListAlt.handleMouseInput();
    }

    private void updateAlts() {
        this.guiListAlt.setAlts(Seppuku.INSTANCE.getAltManager().getAlts());
    }

    private void importAccounts() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setVisible(true);
        chooser.setSize(500, 500);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("File", "txt"));
        final JFrame frame = new JFrame("Import");
        chooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("ApproveSelection") && chooser.getSelectedFile() != null) {
                    try {
                        Scanner scanner = new Scanner(new FileReader(chooser.getSelectedFile()));
                        scanner.useDelimiter("\n");
                        while (scanner.hasNext()) {
                            String[] split = scanner.next().trim().split(":");
                            Seppuku.INSTANCE.getAltManager().addAlt(new AltData(split[0], split[1]));
                            GuiAltManager.this.updateAlts();
                        }
                        scanner.close();
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    frame.setVisible(false);
                    frame.dispose();
                }
                if (e.getActionCommand().equals("CancelSelection")) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            }
        });
        frame.setAlwaysOnTop(true);
        frame.add(chooser);
        frame.setVisible(true);
        frame.setSize(800, 600);
    }

    public GuiScreen getParent() {
        return parent;
    }

    public GuiListAlt getGuiListAlt() {
        return guiListAlt;
    }

    public void setGuiListAlt(GuiListAlt guiListAlt) {
        this.guiListAlt = guiListAlt;
    }

    public GuiButton getLogin() {
        return login;
    }

    public void setLogin(GuiButton login) {
        this.login = login;
    }

    public GuiButton getRemove() {
        return remove;
    }

    public void setRemove(GuiButton remove) {
        this.remove = remove;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
