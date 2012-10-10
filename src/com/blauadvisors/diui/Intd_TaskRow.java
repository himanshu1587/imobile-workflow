package com.blauadvisors.diui;

import java.text.SimpleDateFormat;
import java.util.Hashtable;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Intd_TaskRow extends LinearLayout
{
	public String id, type, url, ns;
	
	public Intd_TaskRow(Context context, AttributeSet attrs, int defStyle){ super(context, attrs); }
	public Intd_TaskRow(Context context, AttributeSet attrs){ super(context, attrs); }
    public Intd_TaskRow(Context context){ super(context); }
    
	public Intd_TaskRow(Context context, Hashtable<String, String> task_data )
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

	public Intd_TaskRow(Context context, String task_id, String task_type, String task_url, String description, String creation_date, String task_ns, boolean XSD_ok )
	{
		super(context);
		create( context, task_id, task_type, task_url, description, creation_date, task_ns, XSD_ok );
	}
	
    public void create( Context context, String task_id, String task_type, String task_url, String desc, String creation_date, String task_ns, boolean XSD_ok )
    {
		id = task_id;
		type = task_type;
		ns = task_ns;
		url = task_url;
		
  		setClickable(false);

  		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	LinearLayout item;// = new LinearLayout( getContext() );
    	if( !XSD_ok )
    		item = (LinearLayout) inflater.inflate(R.layout.wrong_task_item, this);
    		//item = (LinearLayout)findViewById(R.layout.wrong_task_item );
    	else{
			if( task_type.compareTo("ACTIVITY") == 0 )
				item = (LinearLayout) inflater.inflate(R.layout.process_item, this);
				
			else if( task_type.compareTo("NOTIFICATION") == 0 )
				item = (LinearLayout) inflater.inflate(R.layout.notification_item, this);
	
			else //if( task_type.compareTo("INIT") == 0 )
				item = (LinearLayout) inflater.inflate(R.layout.task_item, this);
    	}

		((TextView)item.getChildAt(1)).setText(Html.fromHtml(desc));
		
		//Creation date: 2011-07-21T12:21:59.800+02:00
		//date.setText(creation_date.subSequence(0, 19));	//Fast and dirty
		SimpleDateFormat orig_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		SimpleDateFormat dest_format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		try{
			((TextView)item.getChildAt(2)).setText( dest_format.format( orig_format.parse(creation_date) ) );
		}catch (Exception e){
			((TextView)item.getChildAt(2)).setText( getResources().getString( R.string.invalid_date ) );
			//e.printStackTrace();
		}
		addView(item);
    }
}
