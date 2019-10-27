package me.rigamortis.seppuku.impl.config;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.config.Configurable;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.api.value.*;
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
                        if (v instanceof BooleanValue) {
                            final BooleanValue val = (BooleanValue) v;
                            if (StringUtil.isBoolean(split[1])) {
                                val.setBoolean(Boolean.parseBoolean(split[1]));
                            }
                        }
                        if (v instanceof NumberValue && !(v instanceof OptionalValue)) {
                            final NumberValue val = (NumberValue) v;
                            if (split[2].equals("Float") && val.getType() == Float.class) {
                                if (StringUtil.isFloat(split[1])) {
                                    val.setFloat(Float.parseFloat(split[1]));
                                }
                            }
                            if (split[2].equals("Integer") && val.getType() == Integer.class) {
                                if(StringUtil.isInt(split[1])) {
                                    val.setInt(Integer.parseInt(split[1]));
                                }
                            }
                            if (split[2].equals("Double") && val.getType() == Double.class) {
                                if(StringUtil.isDouble(split[1])) {
                                    val.setDouble(Double.parseDouble(split[1]));
                                }
                            }
                        }
                        if (v instanceof OptionalValue) {
                            final OptionalValue val = (OptionalValue) v;
                            if(StringUtil.isInt(split[1])) {
                                val.setInt(Integer.parseInt(split[1]));
                            }
                        }
                        if(v instanceof StringValue) {
                            final StringValue val = (StringValue) v;
                            if(split.length > 1) {
                                val.setString(split[1]);
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
                    if (val instanceof BooleanValue) {
                        final BooleanValue v = (BooleanValue) val;
                        writer.write(val.getDisplayName() + ":" + v.getBoolean());
                        writer.newLine();
                    }
                    if (val instanceof NumberValue && !(val instanceof OptionalValue)) {
                        final NumberValue v = (NumberValue) val;
                        if (v.getType() == Float.class) {
                            writer.write(val.getDisplayName() + ":" + v.getFloat() + ":Float");
                            writer.newLine();
                        } else if (v.getType() == Integer.class) {
                            writer.write(val.getDisplayName() + ":" + v.getInt() + ":Integer");
                            writer.newLine();
                        } else if (v.getType() == Double.class) {
                            writer.write(val.getDisplayName() + ":" + v.getDouble() + ":Double");
                            writer.newLine();
                        }
                    }
                    if (val instanceof OptionalValue) {
                        final OptionalValue v = (OptionalValue) val;
                        writer.write(val.getDisplayName() + ":" + v.getInt());
                        writer.newLine();
                    }
                    if(val instanceof StringValue) {
                        final StringValue v = (StringValue) val;
                        writer.write(val.getDisplayName() + ":" + v.getString());
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
