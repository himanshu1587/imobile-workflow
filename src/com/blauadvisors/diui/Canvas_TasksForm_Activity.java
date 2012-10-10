package com.blauadvisors.diui;

import java.util.Hashtable;

import org.jdom.Element;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Canvas_TasksForm_Activity extends Canvas_Form_Activity
{
	public Canvas_TasksForm_Activity(){ super(); }
	public void onCreate( Bundle savedInstanceState ){super.onCreate(savedInstanceState);}

	public void print_form() throws Exception
	{
		layout.removeAllViews();
		try{
			task_output = intalio.get_task_input_element(task_id);		//Initial output is the input
			parse_canvas( intalio.get_canvas_form(task_url) );
		}catch( Exception e ){
			e.printStackTrace();
			Toast.makeText(layout.getContext(), R.string.cannot_open_form, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		Button complete = new Button( layout.getContext() );
		complete.setText( R.string.complete );
		complete.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    { try{
	    		if( task_type.compareTo("ACTIVITY") == 0 )
	    		{
			    	boolean valid_input;
			    	String error_msg = "";
			    	Intd_Form_Widget input;
			    	for( int id = 0; id < input_count; id++ )
			    	{
			    		input = ((Intd_Form_Widget)findViewById(id));
			    		valid_input = input.hasValidInput();
			    		if( !valid_input )
			    			error_msg += "\n"+input.getName();
			    		else
			    			input.setElementText();
			    	}
			    	if( error_msg.length() > 0 )
			    	{
		    			Toast.makeText(v.getContext(), getString(R.string.invalid_input)+error_msg, Toast.LENGTH_LONG).show();
			    		return;		//Zas, en toa la boca!
			    	}		    			
	    			
	    			Hashtable<String, String> response = 
	    				check_complete_task_response( intalio.complete_task(task_id, task_output) );
	    			if( response != null )
	    				chained_execution( response );
	    		}else{
	    			intalio.complete_notify( task_id );
	    			finish();
	    		}
	    	}catch (Exception e){ e.printStackTrace(); }
		   }
		});

		Button cancel = new Button( layout.getContext() );
		cancel.setText( R.string.cancel );
		cancel.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
	    		//Restart to update and reprint the task list
	    		//TasksNotifs tasksNotifs = (TasksNotifs)( getContext() );
	    		//tasksNotifs.tasksListUpdater.postDelayed(parent.mUpdateTimeTask, 0);
	    		finish();
		    }
		});

		LinearLayout buttons_layout = new LinearLayout( getApplicationContext() );
		
		buttons_layout.addView( complete );
		buttons_layout.addView( cancel );
		layout.addView( buttons_layout );
		setContentView( scroll );
	}
	
	private void chained_execution( Hashtable<String, String> response ) throws Exception
	{
		String next_task_id = response.get("next_task_ID");
		if( next_task_id.compareTo("") != 0 )
		{
			//(Re)print new task in the same activity, as starting new one seems to not work:
			setTask_id( next_task_id );
			setTask_type( "ACTIVITY" );
			setTask_url( response.get("next_task_URL") );
			input_count = 0;		//There's a form input counter that must be reset before reprint.
			print_form();

		}else
			finish();

	}

	private Hashtable<String, String> check_complete_task_response( Element e_response )
	{
		if( e_response == null )
		{
			dialog_msg = getString( R.string.cannot_complete_task );
			showDialog( CANNOT_COMPLETE_DIALOG );
			return null;
		}
		Hashtable<String, String> response = intalio.parse_response_on_task_complete( e_response );
		//What's the "KO" status?
		if( response.get("status").compareTo("OK") != 0 )
		{
			dialog_msg = "Error "+response.get("error_code")+": "+response.get("error_reason");
			showDialog( CANNOT_COMPLETE_DIALOG );
			return null;
		}
		return response;
	}

}
