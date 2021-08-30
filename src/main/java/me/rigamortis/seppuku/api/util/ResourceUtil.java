package me.rigamortis.seppuku.api.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ResourceUtil {
    // thanks to the guy who made this helper method. source:
    // http://www.uofr.net/~greg/java/get-resource-listing.html
    // modified to keep the trailing slash in directories so you can
    // differentiate between directories and files, to support paths with a
    // leading slash, to always return absolute paths, to optionally recurse and
    // to return a set.
    // note that this is kinda expensive for jar files since it lists ALL the
    // resources and then filters them, so, if you need recursion, set recurse
    // to true instead of doing it yourself or you will slow everything down
    /**
    * List directory contents for a resource folder.
    * This is basically a brute-force implementation.
    * Works for regular files and also JARs.
    *
    * @author Greg Briggs
    * @param clazz Any java class that lives in the same place as the resources you want.
    * @param path Should end with "/".
    * @return The full path of each member item. Directories have a trailing slash.
    * @throws URISyntaxException
    * @throws IOException
    */
    public static Set<String> getResourceListing(Class clazz, String path, boolean recurse) throws URISyntaxException, IOException {
        String classPath = clazz.getName().replace(".", "/");

        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: ~~easy enough~~ not so easy for recursion */
            int lastSlash = classPath.lastIndexOf('/');
            if (lastSlash != -1) {
                path = "/" + classPath.substring(0, lastSlash + 1) + path;
            } else {
                path = "/" + path;
            }

            return listFiles(new HashSet<String>(), new File(dirURL.toURI()), path, recurse);
        }

        if (dirURL == null) {
            /*
            * In case of a jar file, we can't actually find a directory.
            * Have to assume the same jar as clazz.
            */
            String me = classPath + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
            // make absolute path from class' package if path doesnt have a leading slash, else, remove leading slash so patterns match
            if (path.startsWith("/")) {
                path = path.substring(1);
            } else {
                int lastSlash = classPath.lastIndexOf('/');
                if (lastSlash != -1) {
                    path = classPath.substring(0, lastSlash + 1) + path;
                }
            }
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path) && !name.equals(path)) { //filter according to the path and ignore entry if it's the input path
                    // ignore part after next slash so that only subdirectories are kept instead of children of subdirectories (if not recursing)
                    if (!recurse) {
                        int nextSlash = name.indexOf('/', path.length());
                        if (nextSlash != -1) {
                            name = name.substring(0, nextSlash + 1);
                        }
                    }
                    result.add("/" + name);
                }
            }

            return result;
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    private static HashSet<String> listFiles(HashSet<String> output, File dir, String prefix, boolean recurse) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String name = prefix + file.getName() + "/";
                output.add(name);

                if (recurse) {
                    listFiles(output, file, name, true);
                }
            } else {
                output.add(prefix + file.getName());
            }
        }

        return output;
    }
}