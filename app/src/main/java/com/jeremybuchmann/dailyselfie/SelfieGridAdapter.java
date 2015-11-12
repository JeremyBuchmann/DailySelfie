package com.jeremybuchmann.dailyselfie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for the selfie GridView
 */
public class SelfieGridAdapter extends BaseAdapter
{
	private Context _context;
	private List<Selfie> _selfies;

	/**
	 * Constructor. Initializes private members.
	 *
	 * @param inContext
	 */
	public SelfieGridAdapter(Context inContext)
	{
		_context = inContext;
		_selfies = new ArrayList<Selfie>();
	}

	/**
	 * Adds a new Selfie to the list
	 *
	 * @param newSelfie
	 */
	public void add(Selfie newSelfie)
	{
		_selfies.add(newSelfie);
		notifyDataSetChanged();
	}

	/**
	 * Deletes all Selfies from the list
	 */
	public void clear()
	{
		_selfies.clear();
		notifyDataSetChanged();
	}

	/**
	 * Returns the number of Selfies in the list
	 *
	 * @return
	 */
	public int getCount()
	{
		return _selfies.size();
	}

	/**
	 * Returns the Selfie at the specified position
	 *
	 * @param position
	 * @return
	 */
	public Object getItem(int position)
	{
		return _selfies.get(position);
	}

	/**
	 * Returns the ID of the Selfie at the specified position, which for
	 * our purposes is the time (in millis) that the selfie was taken.
	 *
	 * @param position
	 * @return
	 */
	public long getItemId(int position)
	{
		return _selfies.get(position).getDate().getTime();
	}

	/**
	 * Returns a View of the specified Selfie suitable for adding to a GridView
	 *
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LinearLayout selfieLayout = (LinearLayout) convertView;

		// If this view is not being recycled, inflate the layout
		if (selfieLayout == null) {
			LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			selfieLayout = (LinearLayout) inflater.inflate(R.layout.selfie_layout, parent, false);
		}

		// Set the image from the selfie list into the ImageView
		ImageView imageView = (ImageView) selfieLayout.findViewById(R.id.selfie_image);
		imageView.setImageBitmap(_selfies.get(position).getImage());

		// Format the date and set it into the TextView
		TextView dateView = (TextView) selfieLayout.findViewById(R.id.selfie_date);
		dateView.setText( new SimpleDateFormat("yyyy-MM-dd").format(_selfies.get(position).getDate()) );

		return selfieLayout;
	}
}
