package com.jeremybuchmann.dailyselfie;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
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
 * The main activity for this app
 */
public class MainActivity extends AppCompatActivity
	implements DeleteAllDialogFragment.DeleteAllDialogListener
{
	public static String TAG = "DailySelfie";
	public static final int GET_PHOTO_REQUEST_CODE = 1;
	public static final String SELFIE_KEY = "com.jeremybuchmann.DailySelfie.SELFIE_TO_SHOW";
	private static String _DELETE_ALL_PHOTOS = "deleteall";
	private static String _DATETIME_FORMAT = "yyyyMMdd'T'HHmmss";
	private static String _FILENAME_PREFIX = "Selfie_";
	private static String _FILENAME_REGEX = "Selfie_(.*)\\.jpg";
	private static long _ALARM_INTERVAL = 1000 * 60 * 2;
	private GridView _gridView;
	private SelfieGridAdapter _gridAdapter;
	private Uri _photoLocation;
	private AlarmManager _alarmManager;
	private PendingIntent _alarmIntent;
	private CountDownLatch _uiLayoutSignal;

	/**
	 * Sets up the user interface, creates callbacks for handling important
	 * events, and creates an alarm to notify the user of when to take
	 * a selfie
	 *
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

				// Start the activity for viewing a single photo
				startActivity(photoViewIntent);
			}
		});

		// Create a CountDownLatch which enables us to signal when the
		// UI is done being laid out
		_uiLayoutSignal = new CountDownLatch(1);

		// Create a listener for when the UI is finished with the layout. The
		// thread that adds the thumbnails must wait until the gridview is
		// finished with its layout before it can add the thumbnails
		_gridView.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout()
				{
					// Signal the other thread (below) to load the saved
					// images into the grid
					_uiLayoutSignal.countDown();
				}
			}
		);

		// Check whether the storage is mounted and available
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

			// Start a new thread to handle the loading of the images from
			// the filesystem and the thumbnail generation
			new Thread( new Runnable() {

				@Override
				public void run()
				{
					// Check the filesystem to see whether there are any saved images
					File imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
					FileFilter selfieFilter = new FileFilter() {
						@Override
						public boolean accept(File file) {
							return file.getName().matches(_FILENAME_REGEX);
						}
					};
					File[] imageFiles = imageDir.listFiles(selfieFilter);

					if (imageFiles.length > 0) {

						// Sort the images according to filename, which will also
						// be chronological due to the file naming convention
						Arrays.sort(imageFiles);

						try {
							// Wait for the UI to finish its layout
							_uiLayoutSignal.await();

							// Create a parser for the date/time part of the filename
							SimpleDateFormat dateParser = new SimpleDateFormat(_DATETIME_FORMAT);

							// Add the images to the grid
							for (File imageFile : imageFiles) {
								final Uri imageFileURI = Uri.fromFile(imageFile);
								final Bitmap thumbnail = generateThumbnail(imageFileURI);
								final Date imageDate = dateParser.parse(imageFile.getName().replace(_FILENAME_PREFIX, "").replace(".jpg", ""));

								// Use View.post to add the image to the grid
								// on the UI thread
								_gridView.post(new Runnable() {
									@Override
									public void run()
									{
										_gridAdapter.add(new Selfie(thumbnail, imageDate, imageFileURI));
									}
								});
							}
						} catch (InterruptedException ie) {
							Log.e(TAG, "BG: Caught InterruptedException while waiting for countdown latch: " + ie);
						} catch (ParseException pse) {
							Log.e(TAG, "BG: Caught ParseException while trying to generate a thumbnail for a stored image: " + pse);
						} catch (FileNotFoundException fnfe) {
							Log.e(TAG, "BG: Caught FileNotFoundException while trying to generate a thumbnail for a stored image: " + fnfe);
						}
					}
				}
			}).start();

		} else {
			Log.e(TAG, "cannot read photos; external storage is in state " + Environment.getExternalStorageState());
			Toast.makeText(this, R.string.storage_not_available, Toast.LENGTH_LONG).show();
		}

		// Create an alarm to remind the user to take a selfie
		_alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		_alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), 0);
		_alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + _ALARM_INTERVAL, _ALARM_INTERVAL, _alarmIntent);
	}

	/**
	 * Handle the result from the camera
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == GET_PHOTO_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Generate a thumbnail from the image file and add it to
				// the grid; we know the Uri is _photoLocation
				try {
					Bitmap thumbnail = generateThumbnail(_photoLocation);
					_gridAdapter.add( new Selfie(thumbnail, new Date(), _photoLocation) );
				} catch (FileNotFoundException fnfe) {
					Log.e(TAG, "Caught a FileNotFoundException while trying to generate a thumbnail from the snapped photo: " + fnfe);
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
	 * Callback for the "Delete all Photos" confirmation button. Deletes
	 * all of the photos.
	 *
	 * @param fragment
	 */
	@Override
	public void onConfirmDeleteAllPhotos(android.app.DialogFragment fragment)
	{
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

			// Cancel any notifications
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();

			// Check whether the device has a camera
			PackageManager packageManager = getPackageManager();
			if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

				// If the device has a camera, send an Intent to open the photo app
				Intent getPhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (getPhotoIntent.resolveActivity(packageManager) != null) {

					// Create a file where we want the full size image to be saved
					File imageFile = null;
					try {

						// Create the filename
						SimpleDateFormat format = new SimpleDateFormat(_DATETIME_FORMAT);
						String imageFilename = _FILENAME_PREFIX + format.format(new Date()) + ".jpg";

						// Create the File object
						imageFile = new File(
							Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
							imageFilename
						);

						// Create the actual file on the filesystem and
						// start the camera activity
						if (imageFile.createNewFile()) {
							_photoLocation = Uri.fromFile(imageFile);
							getPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, _photoLocation);
							startActivityForResult(getPhotoIntent, GET_PHOTO_REQUEST_CODE);
						} else {
							Log.e(TAG, "Could not create new file " + imageFilename + ". It probably already exists");
						}

					} catch (IOException ex) {
						Log.e(TAG, "Caught IOException while trying to create a file for saving the photo: " + ex);
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

			// Show the confirmation dialog asking whether to really delete
			// all photos
			DialogFragment deleteAllDialog = new DeleteAllDialogFragment();
			deleteAllDialog.show(getFragmentManager(), _DELETE_ALL_PHOTOS);

			return true;

		} else {

			return super.onOptionsItemSelected(item);

		}
	}


	/**
	 * Generates a thumbnail from a larger image's Uri
	 *
	 * @param photoUri
	 * @return
	 */
	private Bitmap generateThumbnail(Uri photoUri) throws FileNotFoundException
	{
		int thumbnailSize = _gridView.getColumnWidth();
		InputStream is = getContentResolver().openInputStream(photoUri);
		Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(is), thumbnailSize, thumbnailSize);
		return thumbnail;
	}
}