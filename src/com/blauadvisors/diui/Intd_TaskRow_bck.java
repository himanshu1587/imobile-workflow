package com.blauadvisors.diui;

import java.text.SimpleDateFormat;
import java.util.Hashtable;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Intd_TaskRow_bck extends LinearLayout
{
	public String id, type, url, ns;
	private TextView description, date;
	private ImageView icon;
	
	public Intd_TaskRow_bck(Context context, AttributeSet attrs, int defStyle){ super(context, attrs); }
	public Intd_TaskRow_bck(Context context, AttributeSet attrs){ super(context, attrs); }
    public Intd_TaskRow_bck(Context context){ super(context); }
    
	public Intd_TaskRow_bck(Context context, Hashtable<String, String> task_data )
	{
		super(context);
		
		create(
			context,
			task_data.get("taskId"),
			task_data.get("taskType"),
			task_data.get("url"),
			task_data.get("description"),
			task_data.get("creationDate"),
			task_data.get("initMessageNamespaceURI"),
			Boolean.parseBoolean( task_data.get("XSD_ok") )
		);
	}

	public Intd_TaskRow_bck(Context context, String task_id, String task_type, String task_url, String description, String creation_date, String task_ns, boolean XSD_ok )
	{
		super(context);
		create( context, task_id, task_type, task_url, description, creation_date, task_ns, XSD_ok );
	}


    public void create( Context context, String task_id, String task_type, String task_url, String desc, String creation_date, String task_ns, boolean XSD_ok )
    {
		id = task_id;
		type = task_type;
		url = task_url;
		ns = task_ns;
		
  		setClickable(false);
/*
    	LinearLayout item;
		if( task_type.compareTo("ACTIVITY") == 0 )
			item = (LinearLayout)findViewById(R.layout.process_item);
			
		else if( task_type.compareTo("NOTIFICATION") == 0 )
			item = (LinearLayout)findViewById(R.layout.notification_item);

		else //if( task_type.compareTo("INIT") == 0 )
			item = (LinearLayout)findViewById(R.layout.task_item);
		
		((TextView)item.findViewById(R.id.tasksProcesses_description)).setText(desc);
		//Creation date: 2011-07-21T12:21:59.800+02:00
		//date.setText(creation_date.subSequence(0, 19));	//Fast and dirty
		SimpleDateFormat orig_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		SimpleDateFormat dest_format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		try{
			((TextView)item.findViewById( R.id.tasksProcesses_date )).setText(
				dest_format.format( orig_format.parse(creation_date) )
			);
		}catch (Exception e){
			((TextView)item.findViewById( R.id.tasksProcesses_date )).setText("Invalid date!");
			e.printStackTrace();
		}
		//this.add
		addView(item);
		
// */
// /*
		//Icon:
		icon = new ImageView(context);
		description = new TextView(context);
		date = new TextView(context);
		
		if( XSD_ok )
		{
			if( task_type.compareTo("ACTIVITY") == 0 )
				icon.setImageResource(R.drawable.task_icon);
			
			else if( task_type.compareTo("NOTIFICATION") == 0 )
				icon.setImageResource(R.drawable.notification_icon);
			
			else //if( task_type.compareTo("INIT") == 0 )
				icon.setImageResource(R.drawable.process_icon);
		}else{
			icon.setImageResource(R.drawable.wrong_task);
			//description.setTextColor(getResources().getColor(R.color.error_task_color));
			//date.setTextColor(getResources().getColor(R.color.error_task_color));
		}
		
		//To convert DP to pixels
		//DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();

		//Task description:
		description.setText(Html.fromHtml(desc));
		//android.view.ViewGroup.LayoutParams lp = description.getLayoutParams();
		//lp.

		//description.setWidth((int)metrics.density*500);
		//description.setHeight((int)metrics.density*48);
		//description.setGravity(HORIZONTAL);		//Seems to not work

		//Creation date: 2011-07-21T12:21:59.800+02:00
		//date.setText(creation_date.subSequence(0, 19));	//Fast and dirty
		SimpleDateFormat orig_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		SimpleDateFormat dest_format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		try{
			date.setText( dest_format.format(orig_format.parse(creation_date)) );
			//date.setWidth((int)metrics.density*175);
			//date.setHeight((int)metrics.density*48);
			date.setGravity(VERTICAL);		//Center
		}catch (Exception e){
			date.setText("Invalid date!");
			e.printStackTrace();
		}

		addView(icon);
		addView(description);
		addView(date);
// */
    }

}
