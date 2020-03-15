package com.project.iotrest.utilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

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
}
