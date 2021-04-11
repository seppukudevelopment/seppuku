package me.rigamortis.seppuku.impl.module.hidden;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.event.minecraft.EventKeyPress;
import me.rigamortis.seppuku.api.event.player.EventChatKeyTyped;
import me.rigamortis.seppuku.api.event.player.EventSendChatMessage;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import me.rigamortis.seppuku.api.gui.hud.component.HudComponent;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.notification.Notification;
import me.rigamortis.seppuku.api.util.RenderUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Author Seth
 * 4/16/2019 @ 8:44 AM.
 */
public final class CommandsModule extends Module {

    public final Value<String> prefix = new Value("Prefix", new String[]{"prefx", "pfx"}, "The command prefix.", ".");
    public final Value<Boolean> predictions = new Value("Predictions", new String[]{"predict", "pre"}, "Renders command predictions on the screen (WIP).", false);

    private final Set<String> predictedCommands = new LinkedHashSet<>();

    public CommandsModule() {
        super("Commands", new String[]{"cmds", "cmd"}, "Allows you to execute client commands", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.toggle();
    }

    @Listener
    public void onRender2D(EventRender2D event) {
        if (!this.predictions.getValue())
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            if (mc.currentScreen instanceof GuiChat) {
                int height = 0;

                //final String input = ((GuiChat) mc.currentScreen).inputField.getText();

                for (String cmd : this.predictedCommands) {
                    final HudComponent predictionComponent = new HudComponent(2, event.getScaledResolution().getScaledHeight() - 24 - height, 4 + mc.fontRenderer.getStringWidth(cmd), mc.fontRenderer.FONT_HEIGHT);
                    RenderUtil.drawRect(predictionComponent.getX(), predictionComponent.getY(), predictionComponent.getX() + predictionComponent.getW(), predictionComponent.getY() + predictionComponent.getH(), 0xDD101010);
                    mc.fontRenderer.drawStringWithShadow(cmd, predictionComponent.getX() + 2, predictionComponent.getY(), 0xFF9900EE);
                    height += mc.fontRenderer.FONT_HEIGHT;
                }
            } else {
                this.predictedCommands.clear();
            }
        }
    }

    @Listener
    public void onChatKeyTyped(EventChatKeyTyped event) {
        if (!this.predictions.getValue())
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            if (mc.currentScreen instanceof GuiChat) {
                if (event.getKeyCode() == 15) { // tab
                    event.setCanceled(true);
                }

                final int prefixLength = this.prefix.getValue().length();
                String input = ((GuiChat) mc.currentScreen).inputField.getText();

                if (Character.isLetter(event.getTypedChar()) && !Character.isSpaceChar(event.getTypedChar())) {
                    input += event.getTypedChar();
                }

                if (input.startsWith(this.prefix.getValue())) {
                    if (input.length() > prefixLength) {
                        input = input.substring(prefixLength);
                    }

                    Command similarCommand = null;

                    this.populateCommands(input);
                    if (this.predictedCommands.size() > 0) {
                        for (String cmd : this.predictedCommands) {
                            similarCommand = Seppuku.INSTANCE.getCommandManager().findSimilar(cmd);
                            if (similarCommand != null) {
                                if (event.getKeyCode() == 15) {
                                    ((GuiChat) mc.currentScreen).inputField.setText(this.prefix.getValue() + similarCommand.getDisplayName());
                                }
                            }
                        }
                    }

                    if (similarCommand != null) {
                        if (input.length() > similarCommand.getDisplayName().length()) {
                            if (this.matches(input, similarCommand.getDisplayName())) {
                                this.populateArguments(similarCommand);
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void keyPress(EventKeyPress event) {
        if (this.prefix.getValue().length() == 1) {
            final char key = Keyboard.getEventCharacter();
            if (this.prefix.getValue().charAt(0) == key) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiChat());
            }
        }
    }

    @Listener
    public void sendChatMessage(EventSendChatMessage event) {
        if (event.getMessage().startsWith(this.prefix.getValue())) {
            final String input = event.getMessage().substring(this.prefix.getValue().length());
            final String[] split = input.split(" ");

            final Command command = Seppuku.INSTANCE.getCommandManager().find(split[0]);

            if (command != null) {
                try {
                    command.exec(input);
                } catch (Exception e) {
                    e.printStackTrace();
                    Seppuku.INSTANCE.errorChat("Error while running command");
                }
            } else {
                Seppuku.INSTANCE.errorChat("Unknown command " + "\247f\"" + event.getMessage() + "\"");
                final Command similar = Seppuku.INSTANCE.getCommandManager().findSimilar(split[0]);

                if (similar != null) {
                    Seppuku.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
                }
            }

            event.setCanceled(true);
        }
    }

    private void populateCommands(String input) {
        this.predictedCommands.clear();

        for (Command cmd : Seppuku.INSTANCE.getCommandManager().getCommandList()) {
            if (this.matches(input, cmd.getDisplayName())) {
                this.predictedCommands.add(cmd.getDisplayName());
            }
        }
    }

    private void populateArguments(Command command) {
        for (Command cmd : Seppuku.INSTANCE.getCommandManager().getCommandList()) {
            if (!cmd.getDisplayName().equalsIgnoreCase(command.getDisplayName()))
                continue;

            if (cmd.getArguments() == null) {
                Seppuku.INSTANCE.getNotificationManager().addNotification("Command Error", "Command has no arguments to tab complete.", Notification.Type.ERROR, 3000);
                continue;
            }

            //if (matches(split[1], arg)) {
            //}
            this.predictedCommands.addAll(Arrays.asList(cmd.getArguments()));
        }
    }

    private boolean matches(String input, String cmd) {
        return cmd.toLowerCase().startsWith(input.toLowerCase());
    }

    public Value<String> getPrefix() {
        return prefix;
    }
}
