package com.launcher.silverfish.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Lonami on 11/08/16.
 */
public class PackageInfo {

    //region Fields

    public static final String DEFAULT_CATEGORY = "UNKNOWN";

    public static final String ENGINE_PLAY = "play";
    public static final String ENGINE_FDROID = "f-droid";

    //endregion

    //region Interfaces

    public interface PackageCategoryListener
    {
        // Fired when a category is found
        void onPackageCategory(String pkg, String category);
    }

    //endregion

    //region Public methods

    /** Returns a list of all the installed packages that can be launched */
    public String[] getInstalledPackages(Context context) {

        // Used to find only "launchable" packages
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        // Get the package manager and the available activities
        PackageManager pkgMan = context.getPackageManager();
        List<ResolveInfo> availableActivities = pkgMan.queryIntentActivities(i, 0);

        // Store here our result
        String[] packages = new String[availableActivities.size()];

        // Load all the package names
        for (int j = 0; j < availableActivities.size(); j++) {
            ResolveInfo ri = availableActivities.get(j);
            packages[j] = ri.activityInfo.packageName;
        }

        return packages;
    }

    // Note that multiple categories will be listed as "CATEGORY1,CATEGORY2"
    public void getPackagesCategoryAsync(PackageCategoryListener listener, String engine, String... packages) {
        new CategoryFinderTask(listener, engine).execute(packages);
    }

    //endregion

    //region Subclasses

    // This class downloads the Google play website for the given package
    // and then looks into the HTML to find the app category
    private class CategoryFinderTask extends AsyncTask<String, Void, Void> {

        // Save the listener so we can notify when we found a category
        PackageCategoryListener listener;
        String engine; // What engine are we using for finding out the category?

        // Constructor
        public CategoryFinderTask(PackageCategoryListener listener, String engine) {
            this.listener = listener;
            this.engine = engine;
        }

        @Override
        protected Void doInBackground(String... packages) {
            for (int i = 0; i < packages.length; i++) {

                // Notify the listener with the found category
                listener.onPackageCategory(packages[i], findCategory(packages[i]));

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            return null;
        }

        // Actual work

        // These strings are used to sniff the HTML for finding the app category
        // cs stands for Category Search
        final static String csPlayStart = "<a class=\"document-subtitle category\" href=\"/store/apps/category/";
        final static String csPlayEnd = "\">";

        final static String csFDroidStart = "Categories:";

        // Format the package to this string to get the right url
        final static String playPackageUrl = "https://play.google.com/store/apps/details?id=%s";
        final static String fdroidPackageUrl = "https://gitlab.com/fdroid/fdroiddata/raw/master/metadata/%s.txt";

        // Finds the given package name category
        private String findCategory(String packageName) {

            URL url;
            InputStream is = null;
            BufferedReader br;
            String line;

            try {
                url = getPackageUrl(packageName);
                is = url.openStream();
                br = new BufferedReader(new InputStreamReader(is));

                switch (engine) {
                    case ENGINE_PLAY:
                        // Read the HTML
                        while ((line = br.readLine()) != null) {

                            int idx = line.indexOf(csPlayStart);
                            if (idx != -1) { // If we found the category start

                                // Move the index by the needle length to omit it in the substring
                                idx += csPlayStart.length();

                                // Determine where the category name ends
                                int endIdx = line.indexOf(csPlayEnd, idx);

                                // Return category name
                                return line.substring(idx, endIdx);
                            }
                        }
                        break;

                    case ENGINE_FDROID:
                        // F-Droid has the category at the start of the line, as "Category:App category"
                        while ((line = br.readLine()) != null) {
                            if (line.startsWith(csFDroidStart)) {
                                return line.substring(csFDroidStart.length()).toUpperCase();
                            }
                        }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException ioe) { /* do nothing */ }
            }

            return DEFAULT_CATEGORY;
        }

        // Returns the package url for the given package, based on the selected engine
        private URL getPackageUrl(String packageName) throws Exception {
            try {

                switch (engine) {
                    case ENGINE_PLAY:
                        return new URL(String.format(playPackageUrl, packageName));

                    case ENGINE_FDROID:
                        return new URL(String.format(fdroidPackageUrl, packageName));

                    default:
                        throw new Exception("Unknown engine");
                }
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("Invalid package name");
            }
        }
    }

    //endregion
}
