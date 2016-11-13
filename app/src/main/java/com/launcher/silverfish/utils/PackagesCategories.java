package com.launcher.silverfish.utils;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.launcher.silverfish.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;


public final class PackagesCategories {

    //region Helper methods

    // Retrieve the default tab ID based on the English name
    private static int getCategoryId(String englishName) {
        switch (englishName)
        {
            default: case "Other":       return 1;
                     case "Phone":       return 2;
                     case "Games":       return 3;
                     case "Internet":    return 4;
                     case "Media":       return 5;
                     case "Accessories": return 6;
                     case "Settings":    return 7;
        }
    }

    private static boolean containsKeyword(String str, String[] keywords) {
        for (String keyword : keywords) {
            if(str.contains(keyword)) return true;
        }
        return false;
    }

    //endregion

    //region Get Categories

    public static HashMap<String, String> getPredefinedCategories(Context ctx)
    {
        HashMap<String, String> predefCategories = new HashMap<>();

        InputStream inputStream = ctx.getResources().openRawResource(R.raw.package_category);
        String line;
        String[] lineSplit;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()){
                    lineSplit = line.split("=");
                    predefCategories.put(lineSplit[0], lineSplit[1]);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if(inputStream != null) inputStream.close();
            } catch (IOException e) { e.printStackTrace(); }
        }

        return predefCategories;
    }

    //endregion

    //region Get Keywords

    public static HashMap<String, String[]> getKeywords(Context ctx)
    {
        HashMap<String, String[]> keywordsDict = new HashMap<>();

        keywordsDict.put("Phone", new String[]{"phone", "conv", "call", "sms", "mms", "contacts", "stk"});  // stk stands for "SIM Toolkit"
        keywordsDict.put("Games", new String[]{"game", "play", "puzz"});
        keywordsDict.put("Internet", new String[]{"download", "mail", "vending", "browser", "maps", "twitter", "whatsapp", "outlook", "dropbox", "chrome", "drive"});
        keywordsDict.put("Media", new String[]{"pic", "gallery", "photo", "cam", "tube", "radio", "tv", "voice", "video", "music", "mp3"});
        keywordsDict.put("Accessories", new String[]{"editor", "calc", "calendar", "organize", "clock", "time", "viewer", "file", "manager", "memo", "note"});
        keywordsDict.put("Settings", new String[]{"settings", "config", "keyboard", "launcher", "sync", "backup"});


        return keywordsDict;
    }

    //endregion

    //region Set each package category

    public static HashMap<String, Integer> setCategories(Context ctx, List<ResolveInfo> activities)
    {
        return setCategories(ctx, activities, getPredefinedCategories(ctx), getKeywords(ctx));
    }

    public static HashMap<String, Integer> setCategories(Context ctx, List<ResolveInfo> activities,
                                                         HashMap<String, String> categories,
                                                         HashMap<String, String[]> keywords)
    {
        HashMap<String, Integer> pkg_categoryId = new HashMap<>();
        String pkg, category = "";
        int categoryId;

        for (int i = 0; i < activities.size(); i++) {
            ResolveInfo ri = activities.get(i);
            pkg = ri.activityInfo.packageName;
            boolean hit = false;

            if (categories.containsKey(pkg)) {
                category = categories.get(pkg);
                categoryId = getCategoryId(category);

                // Only add if not default
                if (categoryId > 1) {
                    pkg_categoryId.put(pkg, categoryId);
                    hit = true;
                }
            }

            // Intelligent fallback: Try to guess the category
            if (!hit) {
                pkg = pkg.toLowerCase();
                for (String key : keywords.keySet())
                {
                    if (containsKeyword(pkg, keywords.get(key)))
                    {
                        if(pkg.contains("contacts")) Log.d("PACKAGES", "==== CONTACTS APP ====");
                        category = key;
                        pkg_categoryId.put(pkg, getCategoryId(key));
                        System.out.println(pkg + " -> " + category);
                        hit = true;
                    }
                }
            }

            if (!hit) System.out.println(pkg);
        }

        return pkg_categoryId;
    }

    //endregion

}
