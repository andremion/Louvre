<img alt="Icon" src="sample/src/main/res/mipmap-xxhdpi/ic_launcher.png?raw=true" align="left" hspace="1" vspace="1">

<a alt='Buy Me a Coffee at ko-fi.com' href='https://ko-fi.com/T6T05M4O' target='_blank' align='right'><img align='right' height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi4.png?v=0' border='0' /></a>
<a alt='Get it on Google Play' href='https://play.google.com/store/apps/details?id=com.andremion.louvre.sample' target='_blank' align='right'><img align='right' height='36' style='border:0px;height:36px;' src='https://developer.android.com/images/brand/en_generic_rgb_wo_60.png' border='0' /></a>
# Louvre

A small customizable image picker. Useful to handle an gallery image pick action built-in your app.

</br>

[![License Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=true)](http://www.apache.org/licenses/LICENSE-2.0)
![minSdkVersion 19](https://img.shields.io/badge/minSdkVersion-19-red.svg?style=true)
![compileSdkVersion 25](https://img.shields.io/badge/compileSdkVersion-25-yellow.svg?style=true)
[![maven-central](https://img.shields.io/maven-central/v/com.github.andremion/louvre.svg)](https://search.maven.org/#artifactdetails%7Ccom.github.andremion%7Clouvre%7C1.3.0%7Caar)

[![Android Arsenal Louvre](https://img.shields.io/badge/Android%20Arsenal-Louvre-green.svg?style=true)](https://android-arsenal.com/details/1/5188)
[![MaterialUp Louvre](https://img.shields.io/badge/MaterialUp-Louvre-blue.svg?style=true)](https://www.uplabs.com/posts/louvre)

<p align="center">
  <img alt='Sample' src="https://raw.githubusercontent.com/andremion/Louvre/master/art/sample.gif"></br>
  <i>*Images from Google Image Search</i>
</p>

## Installation

Add this in your root `build.gradle` file (**not** your app module `build.gradle` file):

```gradle
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

Then, add the library in your app module `build.gradle`

```groovy
dependencies{
    compile 'com.github.andremion:louvre:[LATEST VERSION]'
}
```

## Usage

Choose one of the **Louvre** themes to use in `GalleryActivity` and override it to define your app color palette.

```xml
<style name="AppTheme.Louvre.Light.DarkActionBar" parent="Louvre.Theme.Light.DarkActionBar">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>
</style>
```
```xml
<style name="AppTheme.Louvre.Dark" parent="Louvre.Theme.Dark">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>
</style>
```
```xml
<style name="AppTheme.Louvre.Light" parent="Louvre.Theme.Light">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>
</style>
```

For `PreviewActivity` you just need to define the accent color.

```xml
<style name="AppTheme.Louvre.Preview" parent="Louvre.Theme.Preview">
    <item name="colorAccent">@color/colorAccent</item>
</style>
```

Declare the **Louvre** activities in `AndroidManifest.xml` file using your new app themes.

```xml
<activity
    android:name="com.andremion.louvre.home.GalleryActivity"
    android:theme="@style/AppTheme.Louvre.Light.DarkActionBar" />
<activity
    android:name="com.andremion.louvre.preview.PreviewActivity"
    android:theme="@style/AppTheme.Louvre.Preview" />
```

Add `READ_EXTERNAL_STORAGE` permission in your `AndroidManifest.xml` file.

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

In your `Activity` you just need the below lines of code to open the **Louvre**.

```java
Louvre.init(myActivity)
        .setRequestCode(LOUVRE_REQUEST_CODE)
        .open();
```

You can also use a `Fragment` to open the **Louvre**. In this case, the `Fragment` will get the `onActivityResult` callback.

```java
Louvre.init(myFragment)
        .setRequestCode(LOUVRE_REQUEST_CODE)
        .open();
```

But you can customize the picker:

######Setting the max images allowed to pick
```java
louvre.setMaxSelection(10)
```

######Setting the current selected items
```java
List<Uri> selection;
...
louvre.setSelection(selection)
```

######Setting the media type to filter the query with a combination of one of these types: `Louvre.IMAGE_TYPE_BMP`, `Louvre.IMAGE_TYPE_JPEG`, `Louvre.IMAGE_TYPE_PNG`
```java
louvre.setMediaTypeFilter(Louvre.IMAGE_TYPE_JPEG, Louvre.IMAGE_TYPE_PNG)
```

See more at the [sample](https://github.com/andremion/Louvre/tree/master/sample)

## Libraries and tools used in the project

* [Design Support Library](http://developer.android.com/intl/pt-br/tools/support-library/features.html#design)
The Design package provides APIs to support adding material design components and patterns to your apps.
* [CounterFab](https://github.com/andremion/CounterFab)
A FloatingActionButton subclass that shows a counter badge on right top corner.
* [Glide](https://github.com/bumptech/glide)
An image loading and caching library for Android focused on smooth scrolling
* [PhotoView](https://github.com/chrisbanes/PhotoView)
Implementation of ImageView for Android that supports zooming, by various touch gestures.

## Contributing

Contributions are always welcome!

**Issues:**
Fell free to open a new issue. Follow the [ISSUE_TEMPLATE.MD](https://github.com/andremion/Louvre/tree/master/ISSUE_TEMPLATE.md)

Follow the "fork-and-pull" Git workflow.

 1. **Fork** the repo on GitHub
 2. **Clone** the project to your own machine
 3. **Commit** changes to your own branch
 4. **Merge** with current *development* branch
 5. **Push** your work back up to your fork
 7. Submit a **Pull request** your changes can be reviewed (please refere the issue if reported)

**Prevent** code-style related changes. Format the code before commiting.

## License

    Copyright 2017 André Mion

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
