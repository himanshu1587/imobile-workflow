package com.blauadvisors.diui;

import java.util.Hashtable;

import org.jdom.Element;
import org.jdom.Namespace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Canvas_ProcessesForm_Activity extends Canvas_Form_Activity
{
	public Canvas_ProcessesForm_Activity(){ super();}
	public void onCreate( Bundle savedInstanceState ){super.onCreate(savedInstanceState);}

	public void print_form() throws Exception
	{
		layout.removeAllViews();
		task_output = new Element("FormModel");
		task_output.setNamespace( Namespace.getNamespace("diui", task_ns) );

		try{
			parse_canvas( intalio.get_canvas_form( task_url) );
		}catch( Exception e ){
			e.printStackTrace();
			Toast.makeText(layout.getContext(), getString(R.string.cannot_open_form)+"\n"+task_url, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		Button complete = new Button( layout.getContext() );
		complete.setText( R.string.start );
		complete.setOnClickListener( new OnClickListener() {
		    public void onClick(View v)
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
		    	try{

	    			Hashtable<String, String> response = 
	    				check_start_process_response( intalio.start_process(task_id, task_output), v.getContext() );
	    			
	    			if( response != null )
	    				chained_execution(response, v.getContext() );

		    	}catch (Exception e){
		    		Toast.makeText( v.getContext(), "Excecption: "+e.getClass().toString(), Toast.LENGTH_LONG ).show();
		    		//Log.d("Exc", "Hola?");
		    		//Log.d("EXC", Arrays.toString( e.getStackTrace() ) );
		    		//e.getStackTrace().toString() );
		    		e.printStackTrace();
		    	}
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
	
	private void chained_execution( Hashtable<String, String> response, Context context ) throws Exception
	{
		String next_task_id = response.get("next_task_ID");
		if( next_task_id.compareTo("") != 0 )
		{
			//(Re)print new task in the same activity, as starting new one seems to not work:
			Intent intent = new Intent().setClass( context, Canvas_TasksForm_Activity.class );
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//intent.setAction("start_task");
			intent.putExtra("task_id", next_task_id);
			intent.putExtra("task_type", "ACTIVITY");
			intent.putExtra("task_url", response.get("next_task_URL"));
			context.startActivity(intent);
		}
		finish();
	}
	
	private Hashtable<String, String> check_start_process_response( Element e_response, Context c )
	{
		if( e_response == null )
		{
			//dialog_msg = getString( R.string.cannot_start_process );
			//showDialog( CANNOT_COMPLETE_DIALOG );
			Toast.makeText( c, getString( R.string.cannot_start_process ), Toast.LENGTH_LONG ).show();
			return null;
		}
		Hashtable<String, String> response = intalio.parse_response_on_init_complete( e_response );
		//What's the "KO" status?
		if( response.get("status").compareTo("OK") != 0 )
		{
			//dialog_msg = "Error "+response.get("error_code")+": "+response.get("error_reason");
			//showDialog( CANNOT_COMPLETE_DIALOG );
			Toast.makeText
				( c, "Error "+response.get("error_code")+": "+response.get("error_reason"), Toast.LENGTH_LONG ).show();

			return null;
		}
		return response;
	}

}
