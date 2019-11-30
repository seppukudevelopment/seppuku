package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.management.ConfigManager;

import java.io.*;

/**
 * Author Seth
 * 4/19/2019 @ 12:08 AM.
 */
public final class ValueConfig extends Configurable {

    public ValueConfig() {
        super(ConfigManager.CONFIG_PATH + "Values/");
    }

    @Override
    public void load() {
        try {
            for(Module mod : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
                final File file = new File(this.getPath() + mod.getDisplayName() + ".cfg");

                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }

                final BufferedReader reader = new BufferedReader(new FileReader(this.getPath() + mod.getDisplayName() + ".cfg"));

                String line;
                while ((line = reader.readLine()) != null) {
                    final String[] split = line.split(":");
                    final Value v = mod.find(split[0]);
                    if (v != null) {
                        if (v.getValue() instanceof Boolean) {
                            if (StringUtil.isBoolean(split[1])) {
                                v.setValue(Boolean.parseBoolean(split[1]));
                            }
                        }

                        if (v.getValue() instanceof Number && !(v.getValue() instanceof Enum)) {
                            if (split[2].equals("Float") && v.getValue().getClass() == Float.class) {
                                if (StringUtil.isFloat(split[1])) {
                                    v.setValue(Float.parseFloat(split[1]));
                                }
                            }

                            if (split[2].equals("Integer") && v.getValue().getClass() == Integer.class) {
                                if (StringUtil.isInt(split[1])) {
                                    v.setValue(Integer.parseInt(split[1]));
                                }
                            }

                            if (split[2].equals("Double") && v.getValue().getClass() == Double.class) {
                                if (StringUtil.isDouble(split[1])) {
                                    v.setValue(Double.parseDouble(split[1]));
                                }
                            }
                        }

                        if (v.getValue() instanceof Enum) {
                            if (StringUtil.isInt(split[1])) {
                                v.setValue(Integer.parseInt(split[1]));
                            }
                        }

                        if (v.getValue() instanceof String) {
                            if (split.length > 1) {
                                v.setValue(split[1]);
                            }
                        }
                    }
                }

                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try {
            for(Module mod : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
                final File file = new File(this.getPath() + mod.getDisplayName() + ".cfg");

                if(!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }

                final BufferedWriter writer = new BufferedWriter(new FileWriter(this.getPath() + mod.getDisplayName() + ".cfg"));

                for (Value val : mod.getValueList()) {
                    if (val.getValue() instanceof Boolean) {
                        writer.write(val.getName() + ":" + val.getValue());
                        writer.newLine();
                    }

                    if (val.getValue() instanceof Number && !(val.getValue() instanceof Enum)) {
                        if (val.getValue().getClass() == Float.class) {
                            writer.write(val.getName() + ":" + val.getValue() + ":Float");
                            writer.newLine();
                        } else if (val.getValue().getClass() == Integer.class) {
                            writer.write(val.getName() + ":" + val.getValue() + ":Integer");
                            writer.newLine();
                        } else if (val.getValue().getClass() == Double.class) {
                            writer.write(val.getName() + ":" + val.getValue() + ":Double");
                            writer.newLine();
                        }
                    }

                    if (val.getValue() instanceof Enum || val.getValue() instanceof String) {
                        writer.write(val.getName() + ":" + val.getValue());
                        writer.newLine();
                    }
                }

                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
