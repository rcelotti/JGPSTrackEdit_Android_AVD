# JGPSTrackEdit_Android_AVD
This is a clone of JGPSTrackEdit:

https://sourceforge.net/projects/jgpstrackedit/ 

Modifications have been introduced to send GPS locations to an Android AVD instance to simulate device movements.

## Warning: early early early stage prototype
Right now everything is hard-coded, quick-n-dirty, not user-friendly.

This project is just usable for Android app debugging purposes.

Android AVD instance must be listening on <em>localhost:5554</em> and You must put Your AVD auth code inside main java file <em>JGPSTrackEdit.java</em> (search for AVD_AUTH_TOKEN).

The auth token can be found in Your home directory, look for a file called

    .emulator_console_auth_token

(check https://developer.android.com/studio/run/emulator-console for details)

## Rationale
This project is born for these main reasons:
<ol>
  <li>Android AVD can import only GPX/KML files while JGPSTrackEdit handles various file types
  </li>
  <li>Within JGPSTrackEdit data is editable
  </li>
  <li>There is no pause/resume inside Android AVD 
  </li>
  <li>Android AVD imported location precision is really bad, coordinates are rounded when imported and if Your app uses locations for - say - heading calculations it will be measleaded
  </li>
</ol>

## The future
<ol>
  <li>Make UI user friendly
  </li>
  <li>Synchronize play/pause/stop commands with map and datasheet
  </li>
  <li>Allow click on datasheet rows
  </li>
  <li>Remove hardcoded parameters
  </li>
</ol>
 
