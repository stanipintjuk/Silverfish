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

    public void getPackagesCategoryAsync(PackageCategoryListener listener, String... packages) {
        new CategoryFinderTask(listener).execute(packages);
    }

    //endregion

    //region Subclasses

    // This class downloads the Google play website for the given package
    // and then looks into the HTML to find the app category
    private class CategoryFinderTask extends AsyncTask<String, Void, Void> {

        // Save the listener so we can notify when we found a category
        PackageCategoryListener listener;

        // Constructor
        public CategoryFinderTask(PackageCategoryListener listener) {
            this.listener = listener;
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
        final static String categorySearchStart = "<a class=\"document-subtitle category\" href=\"/store/apps/category/";
        final static String categorySearchEnd = "\">";

        // Append the package to this string to get the right url
        final static String gplayPackageUrl = "https://play.google.com/store/apps/details?id=";

        // Finds the given package name category
        private String findCategory(String packageName) {

            URL url;
            InputStream is = null;
            BufferedReader br;
            String line;

            try {
                url = getPlayUrl(packageName);
                is = url.openStream();
                br = new BufferedReader(new InputStreamReader(is));

                // Read the HTML
                while ((line = br.readLine()) != null) {

                    int idx = line.indexOf(categorySearchStart);
                    if (idx != -1) { // If we found the category start

                        // Move the index by the needle length to omit it in the substring
                        idx += categorySearchStart.length();

                        // Determine where the category name ends
                        int endIdx = line.indexOf(categorySearchEnd, idx);

                        // Return category name
                        return line.substring(idx, endIdx);
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

        // Returns the Google play url for the given package
        private URL getPlayUrl(String packageName) throws IllegalArgumentException {
            try {
                return new URL(gplayPackageUrl + packageName);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("Invalid package name");
            }
        }
    }

    //endregion
}
