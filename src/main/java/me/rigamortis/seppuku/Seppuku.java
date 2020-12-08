package me.rigamortis.seppuku;

import me.rigamortis.seppuku.api.event.client.EventLoad;
import me.rigamortis.seppuku.api.event.client.EventReload;
import me.rigamortis.seppuku.api.event.client.EventUnload;
import me.rigamortis.seppuku.api.logging.SeppukuFormatter;
import me.rigamortis.seppuku.impl.gui.menu.GuiSeppukuMainMenu;
import me.rigamortis.seppuku.impl.management.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import team.stiff.pomelo.EventManager;
import team.stiff.pomelo.impl.annotated.AnnotatedEventManager;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 * Author Seth
 * 4/4/2019 @ 10:21 PM.
 */
public final class Seppuku {

    public static final Seppuku INSTANCE = new Seppuku();

    private Logger logger;

    //private String prevTitle;

    private EventManager eventManager;

    private APIManager apiManager;

    private ModuleManager moduleManager;

    private CommandManager commandManager;

    private FriendManager friendManager;

    private ConfigManager configManager;

    private RotationManager rotationManager;

    private MacroManager macroManager;

    private WaypointManager waypointManager;

    private TickRateManager tickRateManager;

    private ChatManager chatManager;

    private WorldManager worldManager;

    private IgnoredManager ignoredManager;

    private CapeManager capeManager;

    private PositionManager positionManager;

    private JoinLeaveManager joinLeaveManager;

    private HudManager hudManager;

    private AnimationManager animationManager;

    private NotificationManager notificationManager;

    private GuiSeppukuMainMenu seppukuMainMenu;

    private CameraManager cameraManager;

    /**
     * The initialization point of the client
     * this is called post launch
     */
    public void init() {
        this.eventManager = new AnnotatedEventManager();
        this.apiManager = new APIManager();
        this.configManager = new ConfigManager();
        this.ignoredManager = new IgnoredManager();
        this.friendManager = new FriendManager();
        this.rotationManager = new RotationManager();
        this.macroManager = new MacroManager();
        this.waypointManager = new WaypointManager();
        this.tickRateManager = new TickRateManager();
        this.chatManager = new ChatManager();
        this.worldManager = new WorldManager();
        this.capeManager = new CapeManager();
        this.positionManager = new PositionManager();
        this.joinLeaveManager = new JoinLeaveManager();
        this.animationManager = new AnimationManager();
        this.notificationManager = new NotificationManager();
        this.moduleManager = new ModuleManager();
        this.commandManager = new CommandManager();
        this.cameraManager = new CameraManager();
        this.hudManager = new HudManager();
        this.seppukuMainMenu = new GuiSeppukuMainMenu();

        this.configManager.init(); // Keep last, so we load configs after everything else inits

        //this.prevTitle = Display.getTitle();
        //Display.setTitle("Seppuku 1.12.2");

        this.getEventManager().dispatchEvent(new EventLoad());

        // Add runtime hook to listen for shutdown to save configs
        Runtime.getRuntime().addShutdownHook(new Thread("Seppuku Shutdown Hook") {
            @Override
            public void run() {
                getConfigManager().saveAll();
            }
        });
    }

    public void errorChat(String message) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("\2477[Seppuku]\247c " + message));
    }

    public void errorfChat(String format, Object... objects) {
        errorChat(String.format(format, objects));
    }

    public void logChat(String message) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("\2477[Seppuku]\247f " + message));
    }

    public void logcChat(ITextComponent textComponent) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("\2477[Seppuku]\247f ").appendSibling(textComponent));
    }

    public void logfChat(String format, Object... objects) {
        logChat(String.format(format, objects));
    }

    public void unload() {
        this.moduleManager.unload();
        this.apiManager.unload();
        this.commandManager.unload();
        this.friendManager.unload();
        this.waypointManager.unload();
        this.macroManager.unload();
        this.tickRateManager.unload();
        this.chatManager.unload();
        this.ignoredManager.unload();
        this.capeManager.unload();
        this.joinLeaveManager.unload();
        this.hudManager.unload();
        this.animationManager.unload();
        this.notificationManager.unload();
        this.seppukuMainMenu.unload();
        this.cameraManager.unload();

        this.getEventManager().dispatchEvent(new EventUnload());

        ModContainer seppukuModContainer = null;

        for (ModContainer modContainer : Loader.instance().getActiveModList()) {
            if (modContainer.getModId().equals("seppukumod")) {
                seppukuModContainer = modContainer;
            }
        }

        if (seppukuModContainer != null) {
            Loader.instance().getActiveModList().remove(seppukuModContainer);
        }

        //Display.setTitle(this.prevTitle);
        Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages(true);
        System.gc();
    }

    //TODO fix multi event firing when reloading modules
    public void reload() {
        this.waypointManager.getWaypointDataList().clear();
        this.friendManager.getFriendList().clear();
        this.macroManager.getMacroList().clear();
        this.worldManager.getWorldDataList().clear();
        this.ignoredManager.getIgnoredList().clear();

        this.capeManager.getCapeUserList().clear();
        this.capeManager.getCapesMap().clear();
        this.capeManager = new CapeManager();

        this.configManager.getConfigurableList().clear();
        this.configManager = new ConfigManager();

        this.getEventManager().dispatchEvent(new EventReload());
    }

    /**
     * Setup a logger and set the format
     */
    private void initLogger() {
        this.logger = Logger.getLogger(Seppuku.class.getName());
        logger.setUseParentHandlers(false);
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SeppukuFormatter());
        logger.addHandler(handler);
    }

    public Logger getLogger() {
        if (this.logger == null) {
            this.initLogger();
        }

        return this.logger;
    }

    public EventManager getEventManager() {
        if (this.eventManager == null) {
            this.eventManager = new AnnotatedEventManager();
        }

        return this.eventManager;
    }

    public APIManager getApiManager() {
        if (this.apiManager == null) {
            this.apiManager = new APIManager();
        }
        return this.apiManager;
    }

    public ModuleManager getModuleManager() {
        if (this.moduleManager == null) {
            this.moduleManager = new ModuleManager();
        }
        return this.moduleManager;
    }

    public CommandManager getCommandManager() {
        if (this.commandManager == null) {
            this.commandManager = new CommandManager();
        }
        return this.commandManager;
    }

    public FriendManager getFriendManager() {
        if (this.friendManager == null) {
            this.friendManager = new FriendManager();
        }
        return this.friendManager;
    }

    public ConfigManager getConfigManager() {
        if (this.configManager == null) {
            this.configManager = new ConfigManager();
        }
        return this.configManager;
    }

    public RotationManager getRotationManager() {
        if (this.rotationManager == null) {
            this.rotationManager = new RotationManager();
        }
        return this.rotationManager;
    }

    public MacroManager getMacroManager() {
        if (this.macroManager == null) {
            this.macroManager = new MacroManager();
        }
        return this.macroManager;
    }

    public WaypointManager getWaypointManager() {
        if (this.waypointManager == null) {
            this.waypointManager = new WaypointManager();
        }
        return this.waypointManager;
    }

    public TickRateManager getTickRateManager() {
        if (this.tickRateManager == null) {
            this.tickRateManager = new TickRateManager();
        }
        return this.tickRateManager;
    }

    public ChatManager getChatManager() {
        if (this.chatManager == null) {
            this.chatManager = new ChatManager();
        }
        return this.chatManager;
    }

    public WorldManager getWorldManager() {
        if (this.worldManager == null) {
            this.worldManager = new WorldManager();
        }
        return this.worldManager;
    }

    public IgnoredManager getIgnoredManager() {
        if (this.ignoredManager == null) {
            this.ignoredManager = new IgnoredManager();
        }
        return this.ignoredManager;
    }

    public CapeManager getCapeManager() {
        if (this.capeManager == null) {
            this.capeManager = new CapeManager();
        }
        return this.capeManager;
    }

    public PositionManager getPositionManager() {
        if (this.positionManager == null) {
            this.positionManager = new PositionManager();
        }
        return this.positionManager;
    }

    public JoinLeaveManager getJoinLeaveManager() {
        if (this.joinLeaveManager == null) {
            this.joinLeaveManager = new JoinLeaveManager();
        }
        return this.joinLeaveManager;
    }

    public HudManager getHudManager() {
        if (this.hudManager == null) {
            this.hudManager = new HudManager();
        }
        return this.hudManager;
    }

    public AnimationManager getAnimationManager() {
        if (this.animationManager == null) {
            this.animationManager = new AnimationManager();
        }
        return this.animationManager;
    }

    public NotificationManager getNotificationManager() {
        if (this.notificationManager == null) {
            this.notificationManager = new NotificationManager();
        }
        return this.notificationManager;
    }

    public GuiSeppukuMainMenu getSeppukuMainMenu() {
        if (this.seppukuMainMenu == null) {
            this.seppukuMainMenu = new GuiSeppukuMainMenu();
        }
        return this.seppukuMainMenu;
    }

    public CameraManager getCameraManager() {
        if (this.cameraManager == null) {
            this.cameraManager = new CameraManager();
        }
        return this.cameraManager;
    }

}
