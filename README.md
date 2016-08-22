#Silverfish Launcher v0.3
This is a simple and lightweight launcher for Android (... and heavily inspired by SmartLauncher)


# Usage
The launcher consist of two pages: The home screen and the appdrawer.


On the homescreen there is a widget section and a shortcut section.
To change the widget simply long click the widget section and choose your new widget (you might also have to give widget access to Silverfish launcher).


You can add shortcuts to the homescreen by going to the app drawer (swipe right) long click an app and then drag it into the homescreen. 
The homescreen will automatically position your shortcuts in such a way that they will form a square.


The app drawer is devided into multiple tabs for different application types, like SmartLauncher. Unlike SmartLauncher the apps are not automatically placed in their corresponding tabs, so you will have to place them there yourself :( 

You can add, remova or rename a tab by simply long clicking it.

#Notes
Keep in mind that this is just the second beta version so the functionality is very limited and bugs are expected. I mostly wrote it to scratch my own itch. As my itch gets worse more functionality will be implemented. If you have an itch that you want to scratch then your commits are welcome :)

#Todo 
* Make the launcher functional and stable - ✓
* Functionality to add, remove and rename tabs in app drawer.✓
* Icon support for tabs in app drawer.
* Ability to uninstall app from the app drawer. ✓
* Functionality to change wallpaper. 
* More shortcut layouts!
    - circular
    - square - ✓
    - triangular??
* Create a settings activity to modify stuff like
    - Colors
    - Position of homescreen and appdrawer
    - Hide or remove empty fragment
    - Change shortcut layout
    - Change the tab icons.

# Changes since last version (v0.2 Alpha)
* Fixed some bugs
* Added ability to remove and add tabs
* App icons now load async so they will not slow down main thread.
* Added french translation
