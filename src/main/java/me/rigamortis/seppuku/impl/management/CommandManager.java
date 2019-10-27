package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.api.event.command.EventCommandLoad;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.ReflectionUtil;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.api.value.*;
import me.rigamortis.seppuku.impl.command.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 4/16/2019 @ 8:36 AM.
 */
public final class CommandManager {

    private List<Command> commandList = new ArrayList<>();

    public CommandManager() {
        this.commandList.add(new HelpCommand());
        this.commandList.add(new ToggleCommand());
        this.commandList.add(new VClipCommand());
        this.commandList.add(new HClipCommand());
        this.commandList.add(new HideCommand());
        this.commandList.add(new ColorCommand());
        this.commandList.add(new BindCommand());
        this.commandList.add(new XrayCommand());
        this.commandList.add(new FriendCommand());
        this.commandList.add(new PeekCommand());
        this.commandList.add(new SpectateCommand());
        this.commandList.add(new ModuleCommand());
        this.commandList.add(new YawCommand());
        this.commandList.add(new PitchCommand());
        this.commandList.add(new NameCommand());
        this.commandList.add(new MacroCommand());
        this.commandList.add(new WaypointsCommand());
        this.commandList.add(new ReloadCommand());
        this.commandList.add(new UnloadCommand());
        this.commandList.add(new DupeCommand());
        this.commandList.add(new InvSeeCommand());
        this.commandList.add(new SayCommand());
        this.commandList.add(new IPCommand());
        this.commandList.add(new CoordsCommand());
        this.commandList.add(new ConnectCommand());
        this.commandList.add(new DisconnectCommand());
        this.commandList.add(new SeedCommand());
        this.commandList.add(new TeleportCommand());
        this.commandList.add(new IgnoreCommand());
        this.commandList.add(new AutoIgnoreCommand());
        this.commandList.add(new JavaScriptCommand());
        this.commandList.add(new FakeChatCommand());
        this.commandList.add(new JoinDateCommand());
        this.commandList.add(new EnchantCommand());
        this.commandList.add(new RenameCommand());
        this.commandList.add(new SpawnEggCommand());
        this.commandList.add(new StackSizeCommand());
        this.commandList.add(new CrashSlimeCommand());
        this.commandList.add(new SignBookCommand());
        this.commandList.add(new SkullCommand());
        this.commandList.add(new GiveCommand());

        //create commands for every value within every module
        loadValueCommands();

        //load our external commands
        loadExternalCommands();
    }

    /**
     * This is where we load custom external commands from disk
     * This allows users to create their own commands and load
     * them during runtime
     */
    public void loadExternalCommands() {
        try {
            //create a directory at "Seppuku 1.12.2/Commands"
            final File dir = new File("Seppuku 1.12.2/Commands");

            //if it doesnt exist create it
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //all jars/zip files in the dir
            //loop though all classes within the jar/zip
            for (Class clazz : ReflectionUtil.getClassesEx(dir.getPath())) {
                if (clazz != null) {
                    //if we have found a class and the class inherits "Module"
                    if (Command.class.isAssignableFrom(clazz)) {
                        //create a new instance of the class
                        final Command command = (Command) clazz.newInstance();

                        if (command != null) {
                            //add the class to our list of modules
                            this.commandList.add(command);
                            Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventCommandLoad(command));
                            System.out.println("[Seppuku] Found external command " + command.getDisplayName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadValueCommands() {
        for (final Module module : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
            if (module.getValueList().size() > 0) {
                this.commandList.add(new Command(module.getDisplayName(), module.getAlias(), module.getDesc() != null ? module.getDesc() : "There is no description for this command", module.toUsageString()) {

                    @Override
                    public void exec(String input) {
                        if (!this.clamp(input, 2, 3)) {
                            this.printUsage();
                            return;
                        }

                        final String[] split = input.split(" ");

                        final Value v = module.find(split[1]);

                        if (v != null) {
                            if (v instanceof BooleanValue) {
                                final BooleanValue val = (BooleanValue) v;

                                if (split.length == 3) {
                                    if (split[2].equalsIgnoreCase("true") || split[2].equalsIgnoreCase("false") || split[2].equalsIgnoreCase("1") || split[2].equalsIgnoreCase("0")) {
                                        if (split[2].equalsIgnoreCase("1")) {
                                            val.setValue(true);
                                            Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to true");
                                            Seppuku.INSTANCE.getConfigManager().saveAll();
                                        } else if (split[2].equalsIgnoreCase("0")) {
                                            val.setValue(false);
                                            Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to false");
                                            Seppuku.INSTANCE.getConfigManager().saveAll();
                                        } else {
                                            val.setValue(Boolean.parseBoolean(split[2]));
                                            Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + Boolean.parseBoolean(split[2]));
                                            Seppuku.INSTANCE.getConfigManager().saveAll();

                                        }
                                    } else {
                                        Seppuku.INSTANCE.errorChat("Invalid input " + "\"" + split[2] + "\" expected true/false");
                                    }
                                }else{
                                    val.setBoolean(!val.getBoolean());
                                    Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + val.getBoolean());
                                    Seppuku.INSTANCE.getConfigManager().saveAll();
                                }
                            }
                            if(v instanceof StringValue) {
                                if (!this.clamp(input, 3, 3)) {
                                    this.printUsage();
                                    return;
                                }
                                final StringValue val = (StringValue) v;
                                val.setValue(split[2]);
                                Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + split[2]);
                                Seppuku.INSTANCE.getConfigManager().saveAll();
                            }
                            if (v instanceof NumberValue && !(v instanceof OptionalValue)) {
                                if (!this.clamp(input, 3, 3)) {
                                    this.printUsage();
                                    return;
                                }
                                final NumberValue val = (NumberValue) v;
                                if (val.getType() == Float.class) {
                                    if (StringUtil.isFloat(split[2])) {
                                        val.setValue(Float.parseFloat(split[2]));
                                        Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + Float.parseFloat(split[2]));
                                        Seppuku.INSTANCE.getConfigManager().saveAll();
                                    } else {
                                        Seppuku.INSTANCE.errorChat("Invalid input " + "\"" + split[2] + "\" expected a number");
                                    }
                                }
                                if (val.getType() == Double.class) {
                                    if (StringUtil.isDouble(split[2])) {
                                        val.setValue(Double.parseDouble(split[2]));
                                        Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + Double.parseDouble(split[2]));
                                        Seppuku.INSTANCE.getConfigManager().saveAll();
                                    } else {
                                        Seppuku.INSTANCE.errorChat("Invalid input " + "\"" + split[2] + "\" expected a number");
                                    }
                                }
                                if (val.getType() == Integer.class) {
                                    if (StringUtil.isInt(split[2])) {
                                        val.setValue(Integer.parseInt(split[2]));
                                        Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + Integer.parseInt(split[2]));
                                        Seppuku.INSTANCE.getConfigManager().saveAll();
                                    } else {
                                        Seppuku.INSTANCE.errorChat("Invalid input " + "\"" + split[2] + "\" expected a number");
                                    }
                                }
                            }
                            if (v instanceof OptionalValue) {
                                if (!this.clamp(input, 3, 3)) {
                                    this.printUsage();
                                    return;
                                }

                                final OptionalValue val = (OptionalValue) v;

                                final int op = val.getOption(split[2]);

                                if (op != -1) {
                                    val.setValue(op);
                                    Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + val.getOptions()[op]);
                                    Seppuku.INSTANCE.getConfigManager().saveAll();
                                } else if (StringUtil.isInt(split[2])) {
                                    final int mode = Integer.parseInt(split[2]);

                                    if (mode > val.getOptions().length - 1) {
                                        Seppuku.INSTANCE.errorChat("Invalid mode " + "\"" + split[2] + "\"");
                                        StringBuilder sb = new StringBuilder();

                                        final int size = val.getOptions().length;
                                        for (int i = 0; i < val.getOptions().length; i++) {
                                            String option = val.getOptions()[i];
                                            sb.append(option + " (" + i + ")" + ((i == size - 1) ? "" : ", "));
                                        }

                                        Seppuku.INSTANCE.logChat("Valid Options:");
                                        Seppuku.INSTANCE.logChat(sb.toString());
                                        return;
                                    } else if (mode < 0) {
                                        Seppuku.INSTANCE.errorChat("Invalid mode " + "\"" + split[2] + "\"");
                                        StringBuilder sb = new StringBuilder();

                                        final int size = val.getOptions().length;
                                        for (int i = 0; i < val.getOptions().length; i++) {
                                            String option = val.getOptions()[i];
                                            sb.append(option + " (" + i + ")" + ((i == size - 1) ? "" : ", "));
                                        }

                                        Seppuku.INSTANCE.logChat("Valid Options: " + sb.toString());
                                        return;
                                    } else {
                                        val.setValue(mode);
                                        Seppuku.INSTANCE.logChat(module.getDisplayName() + " \247c" + val.getDisplayName() + "\247f set to " + val.getOptions()[mode]);
                                        Seppuku.INSTANCE.getConfigManager().saveAll();
                                    }
                                } else {
                                    Seppuku.INSTANCE.errorChat("Invalid input " + "\"" + split[2] + "\" expected a number");
                                }
                            }
                        } else {
                            Seppuku.INSTANCE.errorChat("Invalid input " + "\"" + split[1] + "\"");
                            this.printUsage();
                        }
                    }
                });
            }
        }
    }

    /**
     * Returns a given command based on display name or alias
     *
     * @param alias
     * @return
     */
    public Command find(String alias) {
        for (Command cmd : this.getCommandList()) {
            for (String s : cmd.getAlias()) {
                if (alias.equalsIgnoreCase(s) || alias.equalsIgnoreCase(cmd.getDisplayName())) {
                    return cmd;
                }
            }
        }
        return null;
    }

    /**
     * Returns the most similar command based on display name or alias
     *
     * @param input
     * @return
     */
    public Command findSimilar(String input) {
        Command cmd = null;
        double similarity = 0.0f;

        for (Command command : this.getCommandList()) {
            final double currentSimilarity = StringUtil.levenshteinDistance(input, command.getDisplayName());

            if (currentSimilarity >= similarity) {
                similarity = currentSimilarity;
                cmd = command;
            }
        }

        return cmd;
    }

    public void unload() {
        for(Command cmd : this.commandList) {
            Seppuku.INSTANCE.getEventManager().removeEventListener(cmd);
        }
        this.commandList.clear();
    }

    public List<Command> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<Command> commandList) {
        this.commandList = commandList;
    }
}
