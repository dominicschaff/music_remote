Music Remote
============

Simple Music remote for use in cars.

Some Information First:
-----------------------

1. The block at the top is the current playing information. As well as the time and battery information, with a '+' if it is charging.
2. The next line is the current action
3. Your music player must be running already to use this app.
4. This is a work in progress. (There will be bugs)
5. The APK is compiled for 4.4 but might work on other version.
6. The red section is a dead zone, or cancel action zone.
7. This should work with any music player. I have not tested them.

Available Actions:
------------------

* 2 Fingers Controls Volume
* 3 Fingers Controls Skipping

After touching with the specified fingers, you can remove all but one finger
to view the screen more easily.

*Note:* that each action is split into two action, an up and a down.

Rotation is now allowed. if your screen it vertical the actions are up and down, but if your screen is horizontal then the actions are left and right.

Installation:
-------------

1. Download the Remote.apk (it is signed by me)
2. Copy to your phone.
3. Allow apps to be installed from untrusted applications.
4. Use file browser to install app.

Sources:
--------

I got the idea for this app from this great developer, (I think I have 
not done his project any justice though) : http://matthaeuskrenn.com/new-car-ui/


Known Bugs:
-----------

* There is a IntentReceiver that is being leaked
* Occasionally the screen does not clear
* I don't get all media information
* Sometimes the app misses some data. (Phone dependent)



Updates:
--------
**13 June 2014** : Allow for rotation
**12 June 2014** : Bug fixes for devices that do not report media information.
**11 June 2014** : Initial Version
