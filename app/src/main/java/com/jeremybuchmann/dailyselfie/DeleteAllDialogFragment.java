package com.jeremybuchmann.dailyselfie;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 *
 */
public class DeleteAllDialogFragment extends DialogFragment
{
	public static String TAG = "DailySelfie";

	/**
	 * Provides an interface that calling activities must implement
	 * in order to receive a callback from the confirmation button.
	 */
	public interface DeleteAllDialogListener
	{
		public void onConfirmDeleteAllPhotos(DialogFragment dialog);
	}

	// Create an instance of the DeleteAllDialogListener so we can use it
	// to call the activity's implementation of onConfirmDeleteAllPhotos
	DeleteAllDialogListener _listener;

	/**
	 * Saves a reference to the calling activity which must implement the
	 * DeleteAllDialogListener interface.
	 *
	 * @param activity
	 */
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		try {
			_listener = (DeleteAllDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DeleteAllDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Build a dialog asking the user to confirm they want to
		// delete all of the photos
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.delete_all_photos_confirm);
		builder.setCancelable(true);
		builder.setPositiveButton(R.string.delete_all_photos, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id)
			{
				Log.i(TAG, "deleting all photos...");
				_listener.onConfirmDeleteAllPhotos(DeleteAllDialogFragment.this);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id)
			{
				dismiss();
			}
		});

		// Create the AlertDialog object and return it
		return builder.create();
	}
}
