package com.jeremybuchmann.dailyselfie;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Date;

/**
 * This class packages an image and date into one class which makes it
 * easier to pass around and use for the subject of the adapter.
 */
public class Selfie
{
	private Bitmap _image;
	private Date _date;
	private Uri _imageLocation;

	public Selfie(Bitmap inImage, Date inDate, Uri inImageURI)
	{
		_image = inImage;
		_date = inDate;
		_imageLocation = inImageURI;
	}

	public Date getDate()
	{
		return _date;
	}

	public Bitmap getImage()
	{
		return _image;
	}

	public Uri getURI()
	{
		return _imageLocation;
	}
}
