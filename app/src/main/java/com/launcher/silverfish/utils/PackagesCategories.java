package com.launcher.silverfish.utils;

import android.content.Context;
import android.content.pm.ResolveInfo;

import com.launcher.silverfish.R;
import com.launcher.silverfish.dbmodel.AppTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;


public final class PackagesCategories {

    //region Helper methods

    // Retrieve the default tab ID based on the English name
    private static long getCategoryId(String englishName) {
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

    public static HashMap<String, String[]> getKeywords()
    {
        HashMap<String, String[]> keywordsDict = new HashMap<>();

        keywordsDict.put("Phone", new String[]{"phone", "conv", "call", "sms", "mms", "contacts", "stk"});  // stk stands for "SIM Toolkit"
        keywordsDict.put("Games", new String[]{"game", "play"});
        keywordsDict.put("Internet", new String[]{"download", "mail", "vending", "browser", "maps", "twitter", "whatsapp", "outlook", "dropbox", "chrome", "drive"});
        keywordsDict.put("Media", new String[]{"pic", "gallery", "photo", "cam", "tube", "radio", "tv", "voice", "video", "music"});
        keywordsDict.put("Accessories", new String[]{"editor", "calc", "calendar", "organize", "clock", "time", "viewer", "file", "manager", "memo", "note"});
        keywordsDict.put("Settings", new String[]{"settings", "config", "keyboard", "launcher", "sync", "backup"});

        return keywordsDict;
    }

    //endregion

    //region Set each package category

    public static HashMap<AppTable, Long> setCategories(Context ctx,
                                                        List<ResolveInfo> activities)
    {
        return setCategories(activities, getPredefinedCategories(ctx), getKeywords());
    }

    public static HashMap<AppTable, Long> setCategories(List<ResolveInfo> activities,
                                                        HashMap<String, String> categories,
                                                        HashMap<String, String[]> keywords)
    {
        HashMap<AppTable, Long> app_categoryId = new HashMap<>();
        AppTable appTable = new AppTable();
        long categoryId;

        for (int i = 0; i < activities.size(); i++) {
            ResolveInfo ri = activities.get(i);
            String activityName = ri.activityInfo.name;
            String packageName = ri.activityInfo.packageName;
            appTable.setActivityName(activityName);
            appTable.setPackageName(packageName);


            if (categories.containsKey(packageName)) {
                categoryId = getCategoryId(categories.get(packageName));

                // Only add if not default
                if (categoryId > 1) {
                    app_categoryId.put(appTable, categoryId);
                }
            }
            // Intelligent fallback: Try to guess the category
            else {
                String _packageName = packageName.toLowerCase();
                for (String key : keywords.keySet()) {
                    if (containsKeyword(_packageName, keywords.get(key))) {
                        app_categoryId.put(appTable, getCategoryId(key));
                    }
                }
            }
        }

        return app_categoryId;
    }

    //endregion

}
