package me.rigamortis.seppuku.api.gui.menu;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.impl.gui.menu.GuiAltManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

/**
 * @author noil
 */
public final class GuiAddAlt extends GuiScreen {

    private final GuiAltManager parent;
    private GuiTextField usernameField;
    private GuiTextField emailField;
    private GuiPasswordField passwordField;

    public GuiAddAlt(GuiAltManager parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.emailField = new GuiTextField(2, this.fontRenderer, this.width / 2 - 100, 56, 200, 20);
        this.usernameField = new GuiTextField(3, this.fontRenderer, this.width / 2 - 100, 96, 200, 20);
        this.passwordField = new GuiPasswordField(4, this.fontRenderer, this.width / 2 - 100, 136, 200, 20);
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, "Add"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96 + 36, "Back"));
        this.usernameField.setMaxStringLength(64);
        this.passwordField.setMaxStringLength(64);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        mc.fontRenderer.drawStringWithShadow("Email", (this.width / 2.0f - 100), 56 - mc.fontRenderer.FONT_HEIGHT - 1, 0xFFAAAAAA);
        mc.fontRenderer.drawStringWithShadow(ChatFormatting.RESET + "Username " + ChatFormatting.DARK_GRAY + "(caches player head)", (this.width / 2.0f - 100), 96.0F - mc.fontRenderer.FONT_HEIGHT - 1, 0xFFAAAAAA);
        mc.fontRenderer.drawStringWithShadow("Password", (this.width / 2.0f - 100), 136.0F - mc.fontRenderer.FONT_HEIGHT - 1, 0xFFAAAAAA);
        this.emailField.drawTextBox();
        this.usernameField.drawTextBox();
        this.passwordField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        final boolean emailFieldEmpty = this.emailField.getText().trim().isEmpty();
        final boolean usernameFieldEmpty = this.usernameField.getText().trim().isEmpty();
        final boolean passwordFieldEmpty = this.passwordField.getText().trim().isEmpty();

        switch (button.id) {
            case 0:
                if (!emailFieldEmpty && !usernameFieldEmpty && !passwordFieldEmpty) {
                    final AltData premiumAlt = new AltData(this.emailField.getText().trim(), this.usernameField.getText().trim(), this.passwordField.getText().trim());
                    Seppuku.INSTANCE.getAltManager().addAlt(premiumAlt);
                }
                if (emailFieldEmpty && !usernameFieldEmpty && passwordFieldEmpty) {
                    final AltData offlineAlt = new AltData(this.usernameField.getText().trim());
                    Seppuku.INSTANCE.getAltManager().addAlt(offlineAlt);
                }
                if (emailFieldEmpty && !usernameFieldEmpty && !passwordFieldEmpty) {
                    final AltData semiPremiumAlt = new AltData(this.usernameField.getText().trim(), this.passwordField.getText().trim());
                    Seppuku.INSTANCE.getAltManager().addAlt(semiPremiumAlt);
                }
                if (!emailFieldEmpty && usernameFieldEmpty && !passwordFieldEmpty) {
                    final AltData emailPremiumAlt = new AltData(this.emailField.getText().trim(), this.passwordField.getText().trim());
                    Seppuku.INSTANCE.getAltManager().addAlt(emailPremiumAlt);
                }
                mc.displayGuiScreen(this.parent);
                break;
            case 1:
                mc.displayGuiScreen(this.parent);
                break;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.emailField.textboxKeyTyped(typedChar, keyCode);
        this.usernameField.textboxKeyTyped(typedChar, keyCode);
        this.passwordField.textboxKeyTyped(typedChar, keyCode);

        if (typedChar == '\r') {
            this.actionPerformed(this.buttonList.get(0));
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.emailField.mouseClicked(mouseX, mouseY, mouseButton);
        this.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        this.passwordField.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
