## 0.0.1

*  Initial release with android only api.

## 0.0.2

###  Bug fixes for

*  Removing label error in manifest
*  Removing theme requirement on the id card capture screen
*  Allow multi threads for job submission

## 0.0.3

###  Bug fixes for

*  Added ios implementation
*  Added rethrows to allow calling methods to handle exceptions

## 0.0.4

###  Bug fixes for

*  id capture return the correct error code for -1

## 0.0.5

###  Bug fixes for

*  clashes with other libraries returning activity results
*  removed extra storage permissions

## 0.0.6

###  Bug fixes for

*  get one result from enrolled error or on success which fixes the crashes when the reply has already been sent

## 0.0.7

###  Bug fixes for

*  Fix for missing additional partner params

## 0.0.8

###  Feature

*  Add ability to add call back url to the submitJob method

## 0.0.9

###  Bug fix for

*  iOS dependency not being found

## 0.0.10

###  Bug fix for

*  iOS dependency not being found
*  Android release mode invalid data fix

## 0.0.11

###  Bug fix for
*  Android release mode invalid data fix

## 0.0.12

###  Feature
*  Added consent screen on android

## 0.0.13

###  Bug fix for
*  Consent screen dialog listener not found error

## 0.0.14

###  Bug fix for
*  Job id issue where job id wasn't returning the correct value passed into partner params

## 0.0.15

###  Bug fix for
*  Obfuscation issues fix

## 0.0.16

###  Bug fix for
*  Fix for callback issues on iOS and Android

## 0.0.17

###  Bug fix for
*  Get file paths for captured images offline

## 0.0.18
###  Bug fix for
*  Added document verification

## 0.0.19
###  Feature
*  Permissions can now be auto handled by the sdk by default or can be handled by implementating app
*  Clear data methos added to clear images from previous capturess
###  Bug Fix
*  Removed unnecessary permissions when capturing selfies
*  Android Fix for custom tags 

## 0.0.20

###  Bug fix for
*  Fixes for file paths
*  Introduced smile ui customisation

## 0.0.21

###  Feature
*  Flutter upgrades to latest version 2.10.4

## 0.0.22

###  Feature
*  Fix for selfie capture screens getting stuck after capture

## 0.0.23

###  Feature
*  Fix document verification use enrolled image or not

## 0.0.24

###  Feature
*  Fix document verification better document captures
*  Fix permissions crashes


## 0.0.25-beta.1

###  Feature
*  Fix typo on Portrait mode

## 0.0.25

###  Improvements
* Blurred photos fix from auto focus
* Increased resolution on photos, picture size now greater than 2430
* Correct cropping on images (much more apparent on the S8)
* Camera preview increased resolution
* Camera display aspect ratio now matches the preview size from the camera

## 0.0.26

###  Improvements
* Fixed issue with passing tag

## 0.0.27

###  Improvements
* Increased resolution on photos, picture size now 2800
* Landscape mode when capturing documentation (Android only)
* Improved pre and post capture blur detection (Android only)
* Improved pre and post capture light checks (Android only)

###  Features
* Minimum android SDK is now 19
* Updated gradle wrapper to version 7.5 (Android only)
* Updated gradle plugin to version 7.2.1 (Android only)
* Minimum supported gradle wrapper is now 6.7.1

### Bugfixes
* IOS partner params fix
* IOS changing color and width of the SmartSelfie capture progress


## 0.0.28

###  Improvements
* Blur checks now happen less frequent allowing for better focus Android only)

### Bugfixes
* IOS use callback url fix for when it's set in the portal and there is no 
  callback url set in the SDK

## 0.0.29

###  Improvements
* Blur checks now happen less frequent allowing for better focus Android only)

## 0.0.30

###  Improvements
* Blur checks now happen less frequent allowing for better focus Android only)
* Fix for consent dialog issue

## 0.0.31

###  Improvements (Android Only)
* Blur checks improvements 
* Fix crashes when getting selfies
* Fix crashes when capturing documents

## 0.0.32

###  Improvements (Android Only)
* Blur checks improvements
* Fix crashes when getting selfies
* Fix crashes when capturing documents
* Updated minimum gradle wrapper to version 7
* Updated gradle plugin from version 4.2 to 7.2.0

## 0.0.33-beta.1

###  Improvements (Android Only)
* Support for devices without auto focus

## 0.0.34

###  Feature (Android & iOS)
* New support for the BVN consent and confirmation flow

## 0.0.35
###  Features
* Downgraded iOS min os version to 12
* BVN consent fixes

## 0.0.36
### Improvements
* Fix for crash on Android

## 0.0.37
### Improvements
* Bump Android and iOS native SDK versions (containing bug fixes)

## 0.0.38
###  Feature
* Introduce whitelabelling by using is_white_labelled setting to true/false
  will not show and show the smile id logos respectively for the selfie and
  document capture screens

## 0.0.39
###  Android Fixes
* SmartSelfie screen reinstantiation on some devices
* Document capture image crop issues
* IOS BVN screen navigation handling issues

## 0.0.40
###  Android Fixes
* Updated android dependancies

## 0.0.41
###  IOS Fixes
* Updated native iOS SDK to version 2.1.32 which fixes issues with 
* Fix for android removing unnesessary permissions for capture screens

## 0.0.42
###  Android Fixes
* Remove limits on specifying kotlin version in the SDK and use application ext kotlin_version

## 0.0.43
###  IOS Fixes
* Updated native iOS SDK to version 2.1.32 which fixes issues with 
* Fix for android removing unnesessary permissions for capture screens

## 0.0.44
###  IOS Fixes
* Update iOS SDK to handle network error connections

## 0.0.45
###  IOS & Android Fixes
* Update SDKs to allow reuse of user ids using the allow_re_enroll flag
* Update SDKs to allow updatng of the selfie using the use_enrolled_image flag

## 0.0.46 
###  IOS & Android Fixes
* iOs bvn consent not showing images
* minor bug fixes and improvements on android
