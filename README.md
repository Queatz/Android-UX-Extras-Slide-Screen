# Android UX Extras - Slide Screen
A sliding screen that zooms out

![Sample Usage Image](http://pasteall.org/pic/show.php?id=215a5de4057eaa7073fe375fad80f36d)

# Usage

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Add the dependency:

	dependencies {
	        implementation 'com.github.Queatz:Android-UX-Extras-Slide-Screen:0.1'
	}

In your XML layout:

	<com.github.queatz.slidescreen.SlideScreen
	    xmlns:android="http://schemas.android.com/apk/res/android"
	    android:id="@+id/slideScreen"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent" />

In your activity:

	SlideScreen slideScreen = findViewById(R.id.slideScreen);
	slideScreen.setAdapter(new SlideScreenAdapter() {
	    @Override
	    public int getCount() {
	        return 2; // Total number of slides
	    }

	    @Override
	    public Fragment getSlide(int position) {
	        return new Fragment(); // Provide a fragment for each slide
	    }

	    @Override
	    public FragmentManager getFragmentManager() {
	        return MyActivity.this.getFragmentManager(); // Provide your acitivity's fragment manager
	    }
	});
