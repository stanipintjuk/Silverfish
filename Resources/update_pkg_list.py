#!/usr/bin/python3

import urllib.request
import zipfile
import os
import xml.etree.ElementTree as etree
import sys

# Define some required variables
url = 'http://f-droid.org/repo/index.jar'
output = '../app/src/main/res/raw/package_category.txt'

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


# Generate the output text file in the form of package=category
print('Generating output .txt...')

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
        f.write('{}={}\n'.format(package, category))

print('.txt file generated.')

# Clean up
print('Cleaning up...')

os.remove('index.jar')
os.remove('index.xml')

print('All done.')
