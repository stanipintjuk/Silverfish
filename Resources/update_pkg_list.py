#!/usr/bin/python3

import urllib.request
import zipfile
import os
import xml.etree.ElementTree as etree

# Define some required variables
url = 'http://f-droid.org/repo/index.jar'
output = '../app/src/main/java/com/launcher/silverfish/utils/PackagesCategories.java'

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

with open(output, 'w', encoding='utf-8') as f:

    # Write the first part of the file
    f.write('''package com.launcher.silverfish.utils;

import java.util.HashMap;
import java.util.Map;

public final class PackagesCategories {

    public static final String DEFAULT_CATEGORY = "Unknown";

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

        # Write this app to the file
        f.write('\n        put("{}", "{}");'.format(package, category))

    # Write the final part of the file
    f.write('''
    }};

    public static String getCategory(String pkg) {
        if (pkgCategory.containsKey(pkg)) {
            return pkgCategory.get(pkg);
        }
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
