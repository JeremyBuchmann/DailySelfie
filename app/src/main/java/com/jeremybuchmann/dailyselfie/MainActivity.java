package com.jeremybuchmann.dailyselfie;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 *
 */
public class MainActivity extends AppCompatActivity
{
	private String TAG = "DailySelfie";
	private final int PHOTO_REQUEST_CODE = 1;
	private GridView _gridView;
	private SelfieGridAdapter _gridAdapter;
	private Uri _photoLocation;

	/**
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the GridView's adapter
		_gridView = (GridView) findViewById(R.id.selfiegridview);
		_gridAdapter = new SelfieGridAdapter(this);
		_gridView.setAdapter(_gridAdapter);


		// Since I'm using the compatibility Toolbar for the Action Bar, it
		// must be set using setSupportActionBar
		Toolbar myToolbar = (Toolbar) findViewById(R.id.maintoolbar);
		setSupportActionBar(myToolbar);

	}

	/**
	 *
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
	}

	/**
	 *
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	/**
	 * Creates the action bar items
	 *
	 * @param menu object into which the action bar menu is inflated
	 * @return true
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar, menu);
		return true;
	}

	/**
	 * Handles selections of the action bar buttons
	 *
	 * @param item the item that was selected
	 * @return true if the menu item is recognized, otherwise the parent's
	 * onOptionsItemSelected() is returned
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_photo) {
			Log.i(TAG, "launching photo");

			// Check whether the device has a camera
			PackageManager packageManager = getPackageManager();
			if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

				// If the device has a camera, send an Intent to open the photo app
				Intent getPhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (getPhotoIntent.resolveActivity(packageManager) != null) {

					// Create a file where we want the full size image to be saved
					File imageFile = null;
					try {

						imageFile = File.createTempFile(
							"Selfie_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
							".jpg",
							Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
						);

					} catch (IOException ex) {
						// TODO: what do we do here?
					}

					if (imageFile != null) {
						_photoLocation = Uri.fromFile(imageFile);
						getPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, _photoLocation);
						startActivityForResult(getPhotoIntent, PHOTO_REQUEST_CODE);
					}
				}

			} else {

				// If the device doesn't have a camera, (meaning that this may
				// be an emulator), pick a random sample image to use instead
				Random rand = new Random();
				int picNum = rand.nextInt(8);

				Log.i(TAG, "device does not have a camera, so using sample image " + picNum);

				// Get the resource ID of the sample image and then load it
				// as a bitmap
				int sampleImageResourceId = getResources().getIdentifier("sample_" + picNum, "drawable", this.getPackageName());
				Bitmap sampleImage = BitmapFactory.decodeResource(getResources(), sampleImageResourceId);

				_photoLocation = Uri.parse("android.resource://" + this.getPackageName() + "/" + sampleImageResourceId);

				_gridAdapter.add( new Selfie(sampleImage, new Date(), _photoLocation) );
			}

			return true;

		} else if (item.getItemId() == R.id.action_delete) {

			Log.i(TAG, "deleting all photos");

			// TODO: Display a dialog asking whether to actually delete all the photos

			return true;

		} else {

			return super.onOptionsItemSelected(item);

		}
	}

	/**
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		if (requestCode == PHOTO_REQUEST_CODE) {

			if (resultCode == RESULT_OK) {

				Log.i(TAG, "got photo, adding it to grid");

				// This code only works when you don't save the full size image to a file.
				// When you do, the data passed into this method is null
				//Bundle extras = data.getExtras();
				//Bitmap photoBitmap = (Bitmap) extras.get("data");

				// Generate a thumbnail from the image file; we know the Uri is _photoLocation
				try {

					// TODO: fix the thumbnail sizing
					int thumbnailSize = _gridView.getColumnWidth();

					InputStream is = getContentResolver().openInputStream(_photoLocation);
					Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(is), thumbnailSize, thumbnailSize);
					_gridAdapter.add( new Selfie(thumbnail, new Date(), _photoLocation) );

				} catch (FileNotFoundException fnfe) {
					// TODO: what do we do here?
				}



			} else {

				Toast.makeText(this, R.string.bad_photo_result, Toast.LENGTH_LONG).show();

			}
		}
	}


}