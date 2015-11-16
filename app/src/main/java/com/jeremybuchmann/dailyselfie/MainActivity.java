package com.jeremybuchmann.dailyselfie;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class MainActivity extends AppCompatActivity
	implements DeleteAllDialogFragment.DeleteAllDialogListener
{
	public static String TAG = "DailySelfie";
	public static final int GET_PHOTO_REQUEST_CODE = 1;
	public static final String SELFIE_KEY = "com.jeremybuchmann.DailySelfie.SELFIE_TO_SHOW";
	private static String _DELETE_ALL_PHOTOS = "deleteall";
	private GridView _gridView;
	private SelfieGridAdapter _gridAdapter;
	private Uri _photoLocation;
	private static String _datetimeFormat = "yyyyMMdd'T'HHmmss";
	private static String _filenamePrefix = "Selfie_";
	private static String _filenameRegex = "Selfie_(.*)\\.jpg";
	private CountDownLatch _uiLayoutSignal;

	/**
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the GridView's adapter
		_gridView = (GridView) findViewById(R.id.selfie_grid_view);
		_gridAdapter = new SelfieGridAdapter(this);
		_gridView.setAdapter(_gridAdapter);

		// Since I'm using the compatibility Toolbar for the Action Bar, it
		// must be set using setSupportActionBar
		Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(myToolbar);

		// Set up the click handler for the items in the gridview
		_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				// Create an intent with an attached Selfie URI
				Selfie selectedSelfie = (Selfie) _gridAdapter.getItem(position);
				Intent photoViewIntent = new Intent(MainActivity.this, PhotoViewActivity.class);
				photoViewIntent.putExtra(SELFIE_KEY, selectedSelfie.getURI());

				startActivity(photoViewIntent);
			}
		});

		// Create a CountDownLatch which enables us to signal when the
		// UI is done being laid out
		_uiLayoutSignal = new CountDownLatch(1);

		// Create a listener for when the UI is finished with the layout
		_gridView.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout()
				{
					// Signal the other thread (below) to load the saved
					// images into the grid
					Log.i(TAG, "gridview layout finished. columnwidth = " + _gridView.getColumnWidth());
					_uiLayoutSignal.countDown();
				}
			}
		);

		// Check whether the storage is mounted and available
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

			// Start a new thread to handle the loading of the images from
			// the filesystem and the bitmap generation
			new Thread( new Runnable() {

				@Override
				public void run()
				{
					// Check the filesystem to see whether there are any saved images
					File imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
					FileFilter selfieFilter = new FileFilter() {
						@Override
						public boolean accept(File file) {
							Log.i(TAG, "BG: checking file " + file.getAbsolutePath());
							return file.getName().matches(_filenameRegex);
						}
					};
					File[] imageFiles = imageDir.listFiles(selfieFilter);
					Log.i(TAG, "BG: loaded " + imageFiles.length + " images from storage");

					if (imageFiles.length > 0) {

						// Sort the images according to filename, which will also
						// be chronological due to the file naming convention
						Arrays.sort(imageFiles);

						try {
							Log.i(TAG, "BG: waiting for layout to finish");

							// Wait for the UI to finish its layout
							_uiLayoutSignal.await();

							// Create a parser for the date/time part of the filename
							SimpleDateFormat dateParser = new SimpleDateFormat(_datetimeFormat);

							// Add the images to the grid
							for (File imageFile : imageFiles) {
								final Uri imageFileURI = Uri.fromFile(imageFile);
								final Bitmap thumbnail = generateThumbnail(imageFileURI);
								final Date imageDate = dateParser.parse(imageFile.getName().replace(_filenamePrefix, "").replace(".jpg", ""));

								// Use View.post to add the image to the grid
								// on the UI thread
								_gridView.post(new Runnable() {
									@Override
									public void run()
									{
										Log.i(TAG, "BG: adding loaded image to gridview");
										_gridAdapter.add(new Selfie(thumbnail, imageDate, imageFileURI));
									}
								});
							}
						} catch (InterruptedException ie) {
							Log.e(TAG, "BG: Caught InterruptedException while waiting for countdown latch: " + ie);
							// TODO: what do we do here?
						} catch (ParseException pse) {
							Log.e(TAG, "BG: Caught ParseException while trying to generate a thumbnail for a stored image: " + pse);
							// TODO: what do we do here?
						} catch (FileNotFoundException fnfe) {
							Log.e(TAG, "BG: Caught FileNotFoundException while trying to generate a thumbnail for a stored image: " + fnfe);
							// TODO: what do we do here?
						}
					}
				}
			}).start();

		} else {
			Log.e(TAG, "cannot read photos; external storage is in state " + Environment.getExternalStorageState());
			Toast.makeText(this, R.string.storage_not_available, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 *
	 */
	@Override
	protected void onStart()
	{
		super.onStart();
		Log.i(TAG, "onStart()");
	}

	/**
	 *
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i(TAG, "onResume()");
	}

	/**
	 *
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		Log.i(TAG, "onPause()");
	}

	/**
	 *
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
		Log.i(TAG, "onStop()");
	}

	/**
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == GET_PHOTO_REQUEST_CODE) {

			if (resultCode == RESULT_OK) {

				Log.i(TAG, "got photo, adding it to grid");

				// Generate a thumbnail from the image file; we know the Uri is _photoLocation
				try {

					Bitmap thumbnail = generateThumbnail(_photoLocation);
					_gridAdapter.add( new Selfie(thumbnail, new Date(), _photoLocation) );

				} catch (FileNotFoundException fnfe) {
					Log.e(TAG, "Caught a FileNotFoundException while trying to generate a thumbnail from the snapped photo: " + fnfe);
					// TODO: what do we do here?
				}

			} else {

				Toast.makeText(this, R.string.bad_photo_result, Toast.LENGTH_LONG).show();

			}
		}
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
	 *
	 * @param fragment
	 */
	@Override
	public void onConfirmDeleteAllPhotos(android.app.DialogFragment fragment)
	{
		Log.i(TAG, "onConfirmDeleteAllPhotos()");

		int numToDelete = _gridAdapter.getCount();
		for (int i = numToDelete - 1; i >= 0; i--) {
			Selfie selfie = (Selfie) _gridAdapter.getItem(i);
			selfie.remove();
		}
		_gridAdapter.clear();
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

						SimpleDateFormat format = new SimpleDateFormat(_datetimeFormat);
						String imageFilename = _filenamePrefix + format.format(new Date()) + ".jpg";

						imageFile = new File(
							Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
							imageFilename
						);

						if (!imageFile.createNewFile()) {
							Log.e(TAG, "Could not create new file " + imageFilename + ". It probably already exists");
						}

					} catch (IOException ex) {
						Log.e(TAG, "Caught IOException while trying to create a file for saving the photo: " + ex);
						// TODO: what do we do here?
					}

					if (imageFile != null) {
						_photoLocation = Uri.fromFile(imageFile);
						getPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, _photoLocation);
						startActivityForResult(getPhotoIntent, GET_PHOTO_REQUEST_CODE);
					}
				}

			} else {

				// If the device doesn't have a camera, (meaning that this may
				// be an emulator), pick a random sample image to use instead
				Random rand = new Random();
				int picNum = rand.nextInt(8);

				Log.w(TAG, "device does not have a camera, so using sample image " + picNum);

				// Get the resource ID of the sample image and then load it
				// as a bitmap
				int sampleImageResourceId = getResources().getIdentifier("sample_" + picNum, "drawable", this.getPackageName());
				Bitmap sampleImage = BitmapFactory.decodeResource(getResources(), sampleImageResourceId);

				_photoLocation = Uri.parse("android.resource://" + this.getPackageName() + "/" + sampleImageResourceId);

				_gridAdapter.add( new Selfie(sampleImage, new Date(), _photoLocation) );
			}

			return true;

		} else if (item.getItemId() == R.id.action_delete) {

			Log.i(TAG, "asking whether to delete all photos");

			DialogFragment deleteAllDialog = new DeleteAllDialogFragment();
			deleteAllDialog.show(getFragmentManager(), _DELETE_ALL_PHOTOS);

			return true;

		} else {

			return super.onOptionsItemSelected(item);

		}
	}


	/**
	 *
	 * @param photoUri
	 * @return
	 */
	private Bitmap generateThumbnail(Uri photoUri) throws FileNotFoundException
	{
		// TODO: fix the thumbnail sizing
		int thumbnailSize = _gridView.getColumnWidth();
		Log.i(TAG, "_gridView.columnWidth = " + thumbnailSize);

		InputStream is = getContentResolver().openInputStream(photoUri);
		Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(is), thumbnailSize, thumbnailSize);
		return thumbnail;
	}
}