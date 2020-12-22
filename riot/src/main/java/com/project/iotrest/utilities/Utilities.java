package com.project.iotrest.utilities;

import com.project.iotrest.exceptions.ApplicationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * A simple Utility class that reads from properties file and provides a resource bundle.
 *
 * @author Roshan
 */
public class Utilities {

    private static final String CONFIG_FOLDER;

    static {
        /* directory to read the source file from */
        CONFIG_FOLDER = "conf";
    }

    /* Private Constructor. */
    private Utilities() {
        throw new UnsupportedOperationException();
    }

    /**
     * Prepares and returns a ResourceBundle for the file passed in method body.
     *
     * @param propFile String
     * @return ResourceBundle
     * @throws MalformedURLException, in case of failure to form URLs.
     */
    public static ResourceBundle getResourceBundleFromFile(String propFile) throws MalformedURLException {
        File file = FileUtils.getFile(CONFIG_FOLDER);
        URL[] urls = {file.toURI().toURL()};
        ClassLoader loader = new URLClassLoader(urls);
        return ResourceBundle.getBundle(propFile, Locale.getDefault(), loader);
    }

    /**
     * Return the value for the specified key from the given property file
     *
     * @param propFile String
     * @param key      String
     * @return String
     */
    public static String getPropertyData(String propFile,
                                         String key) throws MalformedURLException,
            ApplicationException {
        ResourceBundle rb = getResourceBundleFromFile(propFile);
        if (rb.containsKey(key)) {
            return rb.getString(key);
        } else {
            throw new ApplicationException(500,
                    "Attempted to read a missing key " + key + " from properties file " + propFile);
        }
    }

    public static Map<String, String> getResourceBundleMap(String propFile) throws MalformedURLException {
        /* Get Resource bundle from particle properties file. */
        ResourceBundle rb = getResourceBundleFromFile(propFile);
        Map<String, String> map = new HashMap<>();
        Enumeration<String> keys = rb.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key, rb.getString(key));
        }
        return map;
    }

}
