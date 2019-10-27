package me.rigamortis.seppuku.api.util;

import me.rigamortis.seppuku.Seppuku;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Author Seth
 * 4/7/2019 @ 9:13 PM.
 */
public final class ReflectionUtil {

    public static List<Class<?>> getClassesEx(String path) {
        final List<Class<?>> classes = new ArrayList<>();

        try {
            final File dir = new File(path);

            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
                    final ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()}, Seppuku.class.getClassLoader());

                    final ZipFile zip = new ZipFile(file);

                    for (Enumeration list = zip.entries(); list.hasMoreElements(); ) {
                        final ZipEntry entry = (ZipEntry) list.nextElement();

                        if (entry.getName().contains(".class")) {
                            classes.add(classLoader.loadClass(entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.')));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

}
