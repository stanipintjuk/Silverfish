package com.launcher.silverfish.utils;

import android.content.Context;
import android.content.res.Resources;

import com.launcher.silverfish.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class PackagesCategories {

    public static final String DEFAULT_CATEGORY = "Other";

    public static String getCategory(Context ctx, String pkg) {

        // Read the file containing a list in the form of package=category
        try {
            Resources res = ctx.getResources();
            InputStream inputStream = res.openRawResource(R.raw.package_category);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Read each line of the file until we find the right category
            String line;
            while ((line = reader.readLine()) != null) {
                // The left side ([0]) of the string contains the package
                if (pkg.equals(line.split("=")[0])) {

                    // Return the category, contained in the right side ([1])
                    return line.split("=")[1];
                }
            }
        } catch (Exception e) { e.printStackTrace(); }


        // Intelligent fallback: Try to guess the category
        pkg = pkg.toLowerCase();
        if (pkg.contains("conv") ||
                pkg.contains("phone") ||
                pkg.contains("call")) {
            return "Phone";
        }
        if (pkg.contains("game") ||
                pkg.contains("play")) {
            return "Games";
        }
        if (pkg.contains("download") ||
                pkg.contains("mail") ||
                pkg.contains("vending")) {
            return "Internet";
        }
        if (pkg.contains("pic") ||
                pkg.contains("photo") ||
                pkg.contains("cam") ||
                pkg.contains("tube") ||
                pkg.contains("radio") ||
                pkg.contains("tv")) {
            return "Media";
        }
        if (pkg.contains("calc") ||
                pkg.contains("calendar") ||
                pkg.contains("organize") ||
                pkg.contains("clock") ||
                pkg.contains("time")) {
            return "Accessories";
        }
        if (pkg.contains("settings") ||
                pkg.contains("config") ||
                pkg.contains("keyboard") ||
                pkg.contains("sync") ||
                pkg.contains("backup")) {
            return "Settings";
        }

        // If we could not guess the category, return default
        return DEFAULT_CATEGORY;
    }
}
