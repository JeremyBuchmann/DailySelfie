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
 *
 */
public class PhotoViewActivity extends Activity
{
	public static String TAG = "DailySelfie";

	/**
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selfie_photo_view);
		ImageView selfieView = (ImageView) findViewById(R.id.selfie_full_size_photo_view);

		Intent starterIntent = getIntent();
		Bundle extras = starterIntent.getExtras();
		Uri photoUri = (Uri) extras.get(MainActivity.SELFIE_KEY);

		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
			selfieView.setImageBitmap(bitmap);
		} catch (IOException ioe) {
			// TODO: what do we do here?
		}
	}

	/**
	 *
	 */
	@Override
	protected void onStart()
	{
		super.onStart();
		Log.i(TAG, "PhotoViewActivity:onStart()");
	}

	/**
	 *
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i(TAG, "PhotoViewActivity:onResume()");
	}

	/**
	 *
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		Log.i(TAG, "PhotoViewActivity:onPause()");
	}

	/**
	 *
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
		Log.i(TAG, "PhotoViewActivity:onStop()");
	}
}
