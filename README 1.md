#Axonix SDK for Android

###Contents
<!-- MarkdownTOC depth=2 autolink=true bracket=round -->

 - [Introduction](#introduction)
 - [Using the demo application](#using-the-demo-application)
 - [Setup](#setting-up-the-axonix-sdk-for-android)
  - [Adding the SDK library to your project](#adding-the-axonix-sdk-library-to-your-android-project)
  - [Referencing the Axonix SDK](#referencing-the-axonix-sdk)
  - [Adding Google Play services](#adding-google-play-services)
  - [Adding meta-data and permissions](#adding-axonix-meta-data-and-permissions-to-your-application)
 - [Integrating the Axonix SDK to display partial screen ads](#integrating-the-axonix-sdk-to-display-partial-screen-ads)
  - [Adding ads from Axonix into your app](#adding-ads-from-axonix-into-your-app)
  - [Using Java code to manipulate Axonix ads](#using-java-code-to-manipulate-axonix-ads)
  - [Adding listeners to ads (optional)](#adding-listeners-to-ads-optional)
 - [Integrating the Axonix SDK to display fullscreen ads](#integrating-the-axonix-sdk-to-display-fullscreen-ads)
  - [Adding fullscreen ads from Axonix into your app](#adding-fullscreen-ads-from-axonix-into-your-app)
  - [Adding listeners to fullscreen ads (optional)](#adding-listeners-to-fullscreen-ads-optional)
 - [Troubleshooting](#troubleshooting)

<!-- /MarkdownTOC -->

##Introduction

Welcome to the Axonix SDK for android

Before you can start setting up the Axonix SDK you must ensure you have done these 4 things

   1. You have an account on the [Axonix website](https://developer.axonix.com/register.html) if you don't have one already.

   2. You have created an application on the Axonix website, follow [these steps](https://developer.axonix.com/account/general/add_application) once logged in to create an application. This will provide you with an application ID that you will input into your app.

   3. You have registered the ad sizes that you want to display.

   4. You have downloaded the zip file or cloned the Github repository for the Axonix SDK available [here](https://github.com/AxonixRTB/axonix-android-sdk-releases/releases).


The SDK contains these items:

 - axonix-android-sdk-X.X.X.jar - An archive file that contains the Axonix SDK classes for Android, X.X.X represents the version.

 - AxonixDemo - A fully-functional Android application that integrates all features of the Axonix SDK.

This document will guide you through using the Axonix SDK and the demo application, the steps shown are performed in the Eclipse IDE but the process should be similar for other IDEs. If you have any issues setting up the SDK or with any part of this guide see the [troubleshooting section](#troubleshooting) at the end of this document.

##Using the demo application
**Note**: This section will show you how to setup the AxonixDemo application you have downloaded, if you want to skip this part and start setting up the SDK for **your** application you can go straight to the [setup section](#setting-up-the-axonix-sdk-for-android).

1. Start by importing the AxonixDemo project into Eclipse

2. Next you will need to install Google play services (if you haven't already) using the Android SDK manager by selecting Window > Android SDK Manager > tick Google Play services > Install packages...

3. Then import the Google Play services into your workspace from [YOUR_ANDROID_SDK_PATH]/extras/google/google_play_services/libproject

  Remember to select "Copy into workspace". If the demo can't find the library you may have to change the project's              properties using Project > Properties, Select Android on the left > Click Add... , Select the google_play_services_lib then OK.

4. You must also have a minimum of Android 1.6 (API 4), which can also be installed with the SDK manager

5. Connect an android device to your computer or create an android virtual device for the demo to run on and run the application

You should see a screen that looks like this...

<img src="https://s3.amazonaws.com/f.cl.ly/items/3f0y1Y180w032b3f3U3z/demo-final.png" width="40%" height="40%" />

The demo is a simple implementation of the sort of advertisements you can display with the Axonix SDK for android:

 - The two ads presented on the main screen are 320x50 and 300x250, you can click the 'Refresh ads' button to get new advertisements.

 - You can also display a fullscreen but clicking 'Get fullscreen ad' then 'Display fullscreen ad', alternatively you can click 'Get and display fullscreen ad' which does both actions simultaneously.

 - You can also replace "insert-your-application-key" with your own application key to check if ads are running properly for the application(s) you created on the developer dashboard.

 - The log messages displayed by the demo also indicate how the adview listeners can be used in applications.

You should now be ready to start setting up Axonix on your own app! Feel free to use code from the demo when you are setting up your own ads.

##Setting up the Axonix SDK for Android


###Adding the Axonix SDK library to your Android project

This step copies the Axonix classes to the project for your application. To perform this step, copy and paste the axonix-android-sdk-X.X.X.jar file you downloaded into your project’s libs directory:

<img src="https://s3.amazonaws.com/f.cl.ly/items/0n1z0a0a1o0C3Y042c2R/axonixjar.png" width="50%" height="50%" />

**Note**: If this directory does not exist, you must create it in your project’s root directory.


###Referencing the Axonix SDK

This step associates the Axonix SDK with your Android application, allowing the two entities to communicate.

1. In Eclipse, open your Android project.

2. In the menu bar, click Project > Properties.

3.  Select Java Build Path on the left and then select the Libraries tab.

  <img src="https://s3.amazonaws.com/f.cl.ly/items/002V2G221U1K1P0Y420L/properties-window.png" />

4.  Click Add JARs... and select the axonix-android-sdk-X.X.X.jar you copied in the previous step.

5.  Click OK.

  <img src="https://s3.amazonaws.com/f.cl.ly/items/05182K1u3c14233a3q2w/axonix-jar-referenced.png" width="50%" height="50%"/>


###Adding Google Play services


As of 1 August 2014, usage of Android-ID for advertising purposes goes
against the Google Play app store policy, favouring the Advertising ID to give
users more control and privacy capabilities. As a result in order to use versions 4.2.0 and above of the Axonix SDK
in your applications, you will need to add Google Play Services to your application if you haven't already, this process is summarised below and the full tutorial can be found
[here](https://developer.android.com/google/play-services/setup.html).

1. First install Google play services using the Android SDK manager by selecting Window > Android SDK Manager > tick Google Play services > Install packages...

2. Import google play services into your workspace. The folder should be found at [YOUR_ANDROID_SDK_PATH]/extras/google/google_play_services/libproject

   Remember to select "Copy into workspace".

3. In your application's AndroidManifest.xml, add this:
  ```
    <!-- Google Play Services -->
    <meta-data android:name="com.google.android.gms.version"
        android:value="@integer/google_play_services_version" />
  ```
4. In your application's project settings, add a reference to the project using Project > Properties, Select Android on the left > Click Add... , Select google_play_services_lib then OK.

###Adding Axonix meta-data and permissions to your application

In this step, you are declaring the components and permissions that your Android app uses.

In order to implement the Axonix SDK there are a few things that must be added to your application's AndroidManifest.xml file.

**For an example of what your manifest should look like see the code below.**

1. Before the `<application>` tag, add or make sure your application has the INTERNET permission

2. (Optional) Before the `<application>` tag, add or make sure your application has the ACCESS_NETWORK_STATE permission

3. Before the end of the `<application>` tag add Google play services

4. After Google Play Services, add the APPLICATION_ID

  **IMPORTANT:** Remember to replace "insert-your-application-key" with your application ID from the developer dashboard. Click [here](https://developer.axonix.com/account/general/add_application) if you have not yet set up your application yet.

5. After the Application ID, add the AxonixBrowserActivity

6. For applications that support Android tablets (Android SDK Version 3.0+), we also recommend adding this line of code as an `<application>` attribute: `android:hardwareAccelerated="true"`

  If this line of code causes issues with your application, then at least make sure you have `android:hardwareAccelerated="true"` to the AxonixBrowserActivity. This flag is used to ensure that HTML5 Video ads play on Android tablet devices.

Your manifest should look something like this (with the commented lines added by you):

```
<?xml version="1.0" encoding="utf-8"?>

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
  package="com.axonix.demo"
  android:versionName="1.0.0 "
  android:versionCode="1">

  <uses-sdk
    android:minSdkVersion="11"
    android:targetSdkVersion="19" />

  <!--Required-->
  <uses-permission android:name="android.permission.INTERNET" />
  <!--Optional-->
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />  

  <application android:icon="@drawable/icon"
    android:label="@string/app_name"
    android:hardwareAccelerated="true" > <!-- Add to application tags -->

    <activity android:name=".AxonixDemo"
      android:label="@string/app_name">  
    </activity>

  <!-- Google Play Services -->
  <meta-data android:name="com.google.android.gms.version"
    android:value="@integer/google_play_services_version" />

  <!-- Add the application key -->
  <meta-data android:name="com.axonix.APPLICATION_ID"
    android:value="insert-your-application-key"/>

  <!-- Axonix browser activity -->
  <activity
    android:name="com.axonix.android.sdk.AxonixBrowserActivity"  
    android:theme="@android:style/Theme.Translucent.NoTitleBar"
    android:hardwareAccelerated="true" />

  </application>

</manifest>

```

##Integrating the Axonix SDK to display partial screen ads

This section will show you how to integrate the two types of small ads: 320x50 and 300x250.

###Adding ads from Axonix into your app

1.  Open up Eclipse and navigate to the directory where the XML file for your page layout exists (the page you want to advertise on).

  <img src="https://s3.amazonaws.com/f.cl.ly/items/1F223o3z3R163F3J3C47/layoutxml.png" width="50%" height="50%">

2.  In the page layout file, add the following code to instantiate the ad view object

  ```
  <com.axonix.android.sdk.AxonixMMABannerXLAdView
      android:id="@+id/advertising_banner_view"
      android:layout_width="320dip"
      android:layout_height="50dip"
      android:layout_gravity="center"
      android:layout_alignParentBottom='true' />
  ```
  **OR** for a 300x250 ad:

  ```
    <com.axonix.android.sdk.AxonixIABRectangleMAdView
      android:id="@+id/advertising_rectangle_view"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_gravity="center"
      android:layout_alignParentBottom="true" />
  ```

  You should now be able to see the ad space in your layout editor:

  <img src="https://s3.amazonaws.com/f.cl.ly/items/161h0K2W051d442I1k41/layout-editor-banner-ad.png" width="50%" height="50%">

3.  You can control the position ad in the XML file or the layout editor.

4.  Save your page layout's XML file. Your application is now all set to receive ads!

###Using Java code to manipulate Axonix ads

You can control how and when your ads are displayed by using Java

1. Add import lines for the desired ad sizes

  ```
  import com.axonix.android.sdk.AxonixMMABannerXLAdView;
  ```
  for 320x50 ads

  or
  ```
  import com.axonix.android.sdk.AxonixIABRectangleMAdView;
  ```
  for 320x250 ads

2. Declare the ad view (**outside** your method) then get the ad view object using it's `android:id` value from the layout file, (**inside** your method).

 The code shown is for 320x50 ads. The process is similar for 300x250 ads, simply replace `AxonixMMABannerXLAdView` with `AxonixIABRectangleMAdView`.

  ```
  //Declare the ad view
  AxonixMMABannerXLAdView adview_banner;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    ...

    //Get the ad view from the layout file
    adview_banner = (AxonixMMABannerXLAdView) findViewById(R.id.advertising_banner_view);

    //Get an ad
    adview_banner.getAd();

    //Pause ad refresh
    adview_banner.pause();

    //Resume ad refresh
    adview_banner.resume();

  }
  ```

###Adding listeners to ads (optional)

You can also add listeners to ads individually so you can program your app to act differently depending on the behaviour of Axonix ads, for example if an ad fails to load you might want to display something different or log the error code outputted by the SDK.

1. First you will need to add the following import lines:

  ```
  import com.axonix.android.sdk.AxonixAdView;
  import com.axonix.android.sdk.AxonixAdViewListener;
  ```
2. Then add the following to your class declaration so it implements the AxonixAdViewListener:

  ```
  public class MainActivity ... implements AxonixAdViewListener
  ```
3. Then add the listener to your ad view object

  ```
  adview_banner.addAxonixAdViewListener(this);
  ```
4. AxonixAdViewListener implements several methods, all of which you will need to add to your application:

  ```
  @Override
  public void onSuccessfulLoad(AxonixAdView adView) {
      //Called when an ad is loaded
      Log.v(Axonix, "The Axonix ad request was successful!");
  }

  @Override
  public void onFailedLoad(AxonixAdView adView, int errorCode) {
      //Called when an ad fails to load
      Log.v("Axonix", "The ad failed to load with error code: " + errorCode);
  }

  @Override
  public void onAdClick(AxonixAdView adView) {
      //Called when an ad is clicked
      Log.v("Axonix", "Ad clicked!");
  }

  @Override
  public String keywords() {
      //Optional - simply return null if you do not wish to use
      //Can be called to return comma separated strings providing contextual information to about what's currently on the screen to better target ads to the user
      return null;
  }

  @Override
  public String query() {
      //Optional - simply return null if you do not wish to use
      //Can be called to return a string providing search data, if the user is searching for something, to better target ads to the user
      return null;
  }

  @Override
  public boolean onOpenAllocationLoad(AxonixAdView adView, int openAllocationCode) {
      //Optional - return false and leave blank if you do not wish to use
      //Called when the Axonix SDK receives a message from your application to show an advertisement from an ad network you selected for Open Allocation
      return false;
  }

  @Override
  public void onCustomAdTouchThrough(AxonixAdView adView, String string) {
      //Called when the user clicks a custom advertisement
  }
  ```

The full API is available on the Developer Dashboard [here](https://developer.axonix.com/help/advertising/sdk_api/android).

##Integrating the Axonix SDK to display fullscreen ads

###Adding fullscreen ads from Axonix into your app

The AxonixFullScreenAdView object is only available through code and cannot be implemented using layout files. Follow the steps below to implement a simple fullscreen ad from Axonix in your application:

1. Import the AxonixAdView classes to your Activity

  ```
  import com.axonix.android.sdk.AxonixFullScreenAdView;
  ```

2. Declare the view (**outside** your method), then create and display the ad (**inside** your method)

  ```
  //Declare the ad view
  AxonixFullScreenAdView fsadview;

  @Override
  public void onCreate(Bundle savedInstanceState) {

      ...

      //Create the ad view object
      fsadview = new AxonixFullScreenAdView(this);

      //Request and display a fullscreen ad
      fsadview.requestAndDisplayAd();

  }
  ```

The key methods for controlling the preloading and timing of the display of fullscreen ads are as follows:

`fsadview.requestAd();`


 - Requests an advertisement which will download and render the ad but not display it. Any additional calls will cancel any pending requests to the Axonix ad server.

`fsadview.displayRequestedAd();`

 - Displays an advertisement loaded by the AxonixFullScreenAdView object if the ad exists. We recommend you use the hasAd method to verify that an ad has loaded prior to calling this method.

`fsadview.hasAd();`

 - Returns true if the AxonixFullScreenAdView has an ad loaded via the requestAd method.

`fsadview.requestAndDisplayAd();`

 - Gets and displays an ad simultaneously.

###Adding listeners to fullscreen ads (optional)

Similar to normal ads, you can also add listeners to individual fullscreen ads.

1. First you will need to add the following import lines:

  ```
  import com.axonix.android.sdk.AxonixFullScreenAdViewListener;
  ```
2. Then add the following to your class declaration so it implements the AxonixFullScreenAdViewListener:

  ```
  public class MainActivity ... implements AxonixFullScreenAdViewListener
  ```
3. Then add the listener to your ad view object

  ```
  fsadview.addAxonixAdViewListener(this);
  ```
4. AxonixAdViewListener implements several methods, all of which you will need to add to your application:

  ```
	@Override
	public void onDismissAd(AxonixFullScreenAdView fsadview) {
      //Called when a fullscreen ad is dismissed
      Log.v("AxonixFullScreenAdView", "Fullscreen ad dismissed");
	}

	@Override
	public void onFailedLoad(AxonixFullScreenAdView fsadview, int errorCode) {
      //Called when a fullscreen ad fails to load
      Log.v("AxonixFullScreenAdView", "Fullscreen ad failed to load with error code: " + errorcode);
	}

	@Override
	public void onFinishLoad(AxonixFullScreenAdView fsadview) {
      //Called when a fullscreen ad has loaded
      Log.v("AxonixFullScreenAdView", "Fullscreen ad finished loading.");

	}

	@Override
	public void onPresentAd(AxonixFullScreenAdView fsadview) {
      //Called when a fullscreen ad is displayed
      Log.v("AxonixFullScreenAdView", "Fullscreen ad presented");
	}

  @Override
  public String keywords() {
    //Optional - simply return null if you do not wish to use
    //Can be called to return comma separated strings providing contextual information to about what's currently on the screen to better target ads to the user
    return null;
  }

	@Override
	public String query() {
    //Optional - simply return null if you do not wish to use
    //Can be called to return a string providing search data, if the user is searching for something, to better target ads to the user
		return null;
	}
  ```

The full API is available on the Developer Dashboard [here](https://developer.axonix.com/help/advertising/sdk_api/android).

##Troubleshooting

If adverts are not being displayed:

 - First, check that the following items have been added correctly to your AndroidManifest.xml file:

    -  The meta-data for the com.axonix.APPLICATION_ID parameter is inside the <application> tags.

    -  The permissions android.permission.INTERNET has been added to the AndroidManifest.xml file and is outside the <application> tags.

 - If you are testing on a mobile phone, ensure the phone has an internet connection.

 - If you enabled ads on the developer dashboard recently, it is possible they are not yet active, this can sometimes take up to an hour. In the meantime you can check if your app is set up correctly by entering the phrase "insert-your-application-key" into the application key in your manifest, this should supply your app with test ads.

 - You can also review how the AxonixDemo application for an example of how the SDK should be implemented.

**If you still experience problems or you have any further queries you can raise an issue on Github or email publisher.support@axonix.com**
