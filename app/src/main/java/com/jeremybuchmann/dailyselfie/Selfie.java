package com.jeremybuchmann.dailyselfie;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * This class packages an image and date into one class which makes it
 * easier to pass around and use for the subject of the adapter.
 */
public class Selfie
{
	private Bitmap _image;
	private Date _date;

	public Selfie(Bitmap inImage, Date inDate)
	{
		_image = inImage;
		_date = inDate;
	}

	public Date getDate()
	{
		return _date;
	}

	public Bitmap getImage()
	{
		return _image;
	}

	public void setDate(Date inDate)
	{
		this._date = inDate;
	}

	public void setImage(Bitmap inImage)
	{
		this._image = inImage;
	}
}
