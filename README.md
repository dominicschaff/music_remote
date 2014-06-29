Music Remote
============

Simple Music remote for use in cars.

Some Information First:
-----------------------

1. The block at the top is the current playing information. As well as the time and battery information, with a '+' if it is charging.
2. The next line is the current action
3. Your music player must be running already to use this app. (This seems to not be the case in practise, it will start your default music app when the actions get applied)
4. This is a work in progress. (There will be bugs)
5. The APK is compiled for 4.4 but might work on other version.
6. The red section is a dead zone, or cancel action zone.
7. This should work with any music player. I have not tested many.

Available Actions:
------------------

* 1 Finger shows media information (or more fingers)
* 2 Fingers Controls Skipping
* 3 Fingers Controls Volume
* 4 Fingers Controls playback (stop or play/pause)

After touching with the specified fingers, you can remove all but one finger
to view the screen more easily.

*Note:* that each action is split into two actions, an up and a down. Or if you are in horizontal mode, then left and right.

Installation:
-------------

1. Download the Remote <version>>.apk (it is signed by me)
2. Copy to your phone.
3. Allow apps to be installed from untrusted applications.
4. Use file browser to install app.

Sources:
--------

I got the idea for this app from this great developer, (I think I have 
not done his project any justice though) : http://matthaeuskrenn.com/new-car-ui/

Known Bugs:
-----------

* There is a IntentReceiver that is being leaked (This might be fixed now)
* Occasionally the screen does not clear (just touch to fix)
* I don't get all media information (Some apps or devices I cannot get the required information)

Updates:
--------
* **29 June 2014** : V1.4.1 Updated location of receivers, and the actions. Also screen now remains blank(except for time and battery) when there are no fingers on the screen.
* **13 June 2014** : V1.3 Made Text Bigger in horizontal mode, added colours for battery values, customized text for the actions so it does not just say up and down
* **13 June 2014** : Allow for rotation
* **12 June 2014** : Bug fixes for devices that do not report media information.
* **11 June 2014** : Initial Version
