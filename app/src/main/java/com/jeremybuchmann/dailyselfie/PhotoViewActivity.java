package com.jeremybuchmann.dailyselfie;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Activity for displaying the full size image
 */
public class PhotoViewActivity extends Activity
{
	public static String TAG = "DailySelfie";

	/**
	 * Loads the layout and places the image into the ImageView
	 *
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selfie_photo_view);
		ImageView selfieView = (ImageView) findViewById(R.id.selfie_full_size_photo_view);

		// Get the Uri that was passed in via the intent
		Intent starterIntent = getIntent();
		Bundle extras = starterIntent.getExtras();
		Uri photoUri = (Uri) extras.get(MainActivity.SELFIE_KEY);

		// Load the image and set it into the view
		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
			selfieView.setImageBitmap(bitmap);
		} catch (IOException ioe) {
			Log.e(TAG, "Caught IOException while trying to load the full size image: " + ioe.toString());
		}
	}
}
