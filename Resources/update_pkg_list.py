#!/usr/bin/python3

import urllib.request
import zipfile
import os
import xml.etree.ElementTree as etree
import sys

# Define some required variables
url = 'http://f-droid.org/repo/index.jar'
output = '../app/src/main/java/com/launcher/silverfish/utils/PackagesCategories.java'

# Check if we should NOT merge the categories
merge = True
for arg in sys.argv:
    if arg == '--nomerge':
        merge = False
if merge:
    print('Categories will be merged.')
    print('If you want to disable this, pass --nomerge')

# Download index.jar
print('Downloading index from {}...'.format(url))
urllib.request.urlretrieve(url, 'index.jar')
print('Downloaded index.jar.')

# Extract index.xml
print('Extracting index.xml from index.jar...')

zfile = zipfile.ZipFile('index.jar', 'r')
with open('index.xml', 'wb') as f:
    f.write(zfile.read('index.xml'))

print('Extracted index.xml.')


# Generate Java .class
print('Generating Java .class code...')

# Dictionary used to merge the categories into Silverfish ones
merger = {
    'Phone & SMS':  'Phone',
    'Connectivity': 'Phone',

    'Internet':   'Internet',
    'Navigation': 'Internet',

    'Games': 'Games',

    'Multimedia': 'Media',
    'Graphics':   'Media',

    'Reading':             'Accessories',
    'Writing':             'Accessories',
    'Time':                'Accessories',
    'Science & Education': 'Accessories',
    'Sports & Health':     'Accessories',
    'Money':               'Accessories',
    'Development':         'Accessories',

    'System':   'Settings',
    'Security': 'Settings',
    'Theming':  'Settings'
}

with open(output, 'w', encoding='utf-8') as f:

    # Write the first part of the file
    f.write('''// PLEASE NOTE THAT THIS FILE HAS BEEN GENERATED AUTOMATICALLY
// ANY CHANGES MADE WILL BE **ERASED**
// IF YOU WISH TO MAKE ANY CHANGES, EDIT `Resources/update_pkg.list.py` AND RUN THE SCRIPT

package com.launcher.silverfish.utils;

import java.util.HashMap;
import java.util.Map;

public final class PackagesCategories {

    public static final String DEFAULT_CATEGORY = "Other";

    static final Map<String, String> pkgCategory = new HashMap<String, String>(){{''')

    # Read the XML and write the values for the Java Map
    tree = etree.parse('index.xml')
    for app in tree.getroot().findall('application'):
        # Child also has the attrib 'id', which contains the package name
        # It can be retrieved with `child.get('attribute name')`
        #
        # `child.find('tag')` on the other hands finds the first tag
        package = app.find('id').text
        category = app.find('category').text
        if merge:
            category = merger[category]

        # Write this app to the file
        f.write('\n        put("{}", "{}");'.format(package, category))

    # Write the final part of the file
    f.write('''
    }};

    public static String getCategory(String pkg) {
        if (pkgCategory.containsKey(pkg)) {
            return pkgCategory.get(pkg);
        }

        // Intelligent fallback: Try to guess the category
        pkg = pkg.toLowerCase();
        if (pkg.contains("conv") ||
                pkg.contains("phone") ||
                pkg.contains("call")) {
            return "Phone";
        }
        if (pkg.contains("game")) {
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
''')

print('Java code generated.')

# Clean up
print('Cleaning up...')

os.remove('index.jar')
os.remove('index.xml')

print('All done.')
