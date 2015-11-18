package com.jeremybuchmann.dailyselfie;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.util.Date;

/**
 * This class packages an image, date, and file location into one class
 * which makes it easier to pass around and use for the subject of the
 * adapter.
 */
public class Selfie
{
	private Bitmap _image;
	private Date _date;
	private Uri _imageLocation;

	/**
	 * Constructor. Creates a new Selfie.
	 *
	 * @param inImage
	 * @param inDate
	 * @param inImageURI
	 */
	public Selfie(Bitmap inImage, Date inDate, Uri inImageURI)
	{
		_image = inImage;
		_date = inDate;
		_imageLocation = inImageURI;
	}

	/**
	 * Deletes the image file from the filesystem
	 *
	 * @return
	 */
	public boolean remove()
	{
		File imageFile = new File(_imageLocation.getPath());

		// This check ensures we aren't trying to remove a resource file
		return (imageFile.exists() && imageFile.delete());
	}

	/**
	 * Returns whether the given Selfie is the same as this one
	 *
	 * @param test
	 * @return
	 */
	public boolean equals(Selfie test)
	{
		return _imageLocation.equals(test._imageLocation);
	}

	/**
	 * Getter for the date
	 *
	 * @return
	 */
	public Date getDate()
	{
		return _date;
	}

	/**
	 * Getter for the image
	 *
	 * @return
	 */
	public Bitmap getImage()
	{
		return _image;
	}

	/**
	 * Getter for the Uri
	 *
	 * @return
	 */
	public Uri getURI()
	{
		return _imageLocation;
	}
}
