VPN Server Helper
================

VPN Server Helper is a mix of [ics-openvpn's](https://github.com/schwabe/ics-openvpn) *removeExample* and *vpndialogxposed* modules
It provides VpnHelper class which works with [openvpn](https://play.google.com/store/apps/details?id=de.blinkt.openvpn)
app aidl-based api.
Library also has a patched VpnDialogPatcher class that automatically confirms VPN dialog.
To use this option you will need to configure [Xposed Framework](http://xposed.info).

Configuring Xposed Framework
--------
* Have a rooted Android phone.
* Install the Xposed Framework Installer.
* Use the installer to install the Xposed Framework.
* Install this app.
* Enable this module in the Xposed Installer.
* Reboot phone.
* Profit.

Building
--------
Put add-ons/XposedBridge-android-15 to your ANDROID_SDK/add-ons folder
* Running `android list targets` should then show "rovo89:Xposed Bridge:15"
* Open the project in Android Studio and click build

If you don't want to use custom sdk you can also add XposedBridgeApi-2.1.jar as a dependency (USE ONLY "PROVIDED" SCOPE!) to build.gradle

If you don't want to use Android Studio, you're on your own.
