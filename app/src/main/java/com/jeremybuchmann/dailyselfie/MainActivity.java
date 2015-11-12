package com.jeremybuchmann.dailyselfie;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

	/**
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar, menu);
		return true;
	}

	/**
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_photo) {
			Log.i(TAG, "Launching photo ");

			PackageManager packageManager = getPackageManager();
			if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
				// Send an Intent to open the photo app
				Intent getPhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (getPhotoIntent.resolveActivity(packageManager) != null) {
					startActivityForResult(getPhotoIntent, PHOTO_REQUEST_CODE);
				}
			} else {
				// If the device doesn't have a camera, pick a random sample
				// image to use instead
				Random rand = new Random();
				int picNum = rand.nextInt(8);
				Log.i(TAG, "device does not have a camera, so using sample image " + picNum);

				// Get the resource ID of the sample image and then load it
				// as a bitmap
				int sampleImageResourceId = getResources().getIdentifier("sample_"+picNum, "drawable", this.getPackageName());
				Bitmap sampleImage = BitmapFactory.decodeResource(getResources(), sampleImageResourceId);
				_gridAdapter.add(new Selfie(sampleImage, new Date()));
			}

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

				Bundle extras = data.getExtras();
				Bitmap photoBitmap = (Bitmap) extras.get("data");
				_gridAdapter.add(new Selfie(photoBitmap, new Date()));

			} else {

				Toast.makeText(this, R.string.bad_photo_result, Toast.LENGTH_LONG).show();

			}
		}
	}

}