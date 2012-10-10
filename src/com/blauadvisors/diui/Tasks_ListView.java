package com.blauadvisors.diui;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.blauadvisors.diui.IntalioWS.IntalioWSBinder;

public class Tasks_ListView extends ListView implements OnItemClickListener	//OnClickListener
{	
	private IntalioWS intalio = null;
	private ArrayList<Hashtable<String, String>> tasks_list;
	private int num_tasks = 0;
	private TasksProcesses_ListAdapter list_adapter;
	private Notification notification;
	private NotificationManager notificationManager;
	private boolean updating_tasks_list = false;
	
	private static final int NOTIFICATION_ID = 1;
	
	//Thread and it's handler to refresh the list periodically
	public Handler tasksListUpdater = new Handler();
	public Runnable mUpdateTimeTask = new Runnable( )
	{
		public void run()
		{
			if( updating_tasks_list ) return;
			try
			{
				updating_tasks_list = true;
				new get_tasks_list().execute( );
				tasksListUpdater.postDelayed(this, 15000);	//Refresh the tasks list again after some time-interval
				updating_tasks_list = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public Tasks_ListView( Context context, ViewGroup header )
	{
		//this.setAdapter();
		
    	super(context);
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        addHeaderView( header );
        setOnItemClickListener( this );
        list_adapter = new TasksProcesses_ListAdapter( context );

        // Bind to LocalService
        context.getApplicationContext().bindService
        (
        	new Intent( context, IntalioWS.class),
        	mConnection,
        	Context.BIND_AUTO_CREATE
        );
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View item, int position, long id)
	{
		try {
			getContext().startActivity( create_task_intent
			(
				((Intd_TaskRow)item).id,
				((Intd_TaskRow)item).type,
				((Intd_TaskRow)item).url,
				((Intd_TaskRow)item).ns
			));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private Intent create_task_intent( String task_id, String task_type, String task_url, String task_ns)
    {
		//Intent intent = new Intent().setClass( getContext(), XSD_TasksForm_Activity.class);
		Intent intent = new Intent().setClass( getContext(), Canvas_TasksForm_Activity.class);
		//intent.setAction("start_task");
		intent.putExtra("task_id", task_id);
		intent.putExtra("task_type", task_type);
		intent.putExtra("task_url", task_url);
		intent.putExtra("task_ns", task_ns);
		return intent;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            // We've bound to IntalioWSService, cast the IBinder and get IntalioWSService instance
        	//IntalioWSBinder binder = (IntalioWSBinder) service;
            try{
            	intalio = ((IntalioWSBinder)service).getService();
            	tasksListUpdater.postDelayed(mUpdateTimeTask, 0);

            }catch( Exception e ){
            	e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            intalio = null;
        }
    };

    private class get_tasks_list extends AsyncTask< String, Void, ArrayList<ArrayList<Hashtable<String, String>>> >
    {
        protected ArrayList<ArrayList<Hashtable<String, String>>> doInBackground(String... urls)
        {
        	try{
        		
        		ArrayList<ArrayList<Hashtable<String, String>>> tasks_notifs = new ArrayList<ArrayList<Hashtable<String, String>>>();
        		tasks_notifs.add( intalio.get_readyTasks_list() );
        		tasks_notifs.add( intalio.get_readyNotifs_list() );
        		return tasks_notifs;
        	}catch( Exception e ){
        		e.printStackTrace();
        		return null;
        	}
        }

        protected void onPostExecute( ArrayList<ArrayList<Hashtable<String, String>>> new_tasks_list )
        {
        	reprint_tasks_list(new_tasks_list);
        }
    }

    private void reprint_tasks_list( ArrayList<ArrayList<Hashtable<String, String>>> tasks_notifs_list )
    {
    	try{
/*
    		//ArrayList<Hashtable<String, String>> old_tasks_list = tasks_list;
    		ArrayList<Hashtable<String, String>> new_tasks_list = tasks_notifs_list.get(0);	//Add tasks
    		new_tasks_list.addAll( tasks_notifs_list.get(1) );		//Add notifications
    		
    		//Exhaustive search. As the lists are ordered, could be a faster search, but there are notifications not ordened!
    		for( int nid = new_tasks_list.size() -1, oid; nid >= 0; nid-- )
    		{
    			for(
    				oid = tasks_list.size() -1;
    				oid >= 0 && tasks_list.get(oid).get("taskId").compareTo(new_tasks_list.get(nid).get("taskId")) != 0;
    				oid--
    			);
    			if( oid == -1 )	//New task
    			{
    				tasks_list.add( new_tasks_list.get(nid) );
       				Toast.makeText(
    					getContext(),//.getApplicationContext(),
    					getContext().getString
    					(
    						R.string.new_task)+": "
    						+Html.fromHtml(new_tasks_list.get(nid).get("description")
    					),
    	    			Toast.LENGTH_LONG
    	    		).show();
    			}
    		}
*/
    		/**
    			First, the UI is updated (long process).
    			After that, the old and the (copy of) new task list is compared
    			and the notifications are send.
    			This way the list and the notifications are closer in time. 
    		**/

    		ArrayList<Hashtable<String, String>> old_tasks_list = tasks_list;
    		ArrayList<Hashtable<String, String>> new_tasks_list = tasks_notifs_list.get(0);

    		//Better way to update the list? Use add() and remove() from ArrayAdapter
    		tasks_list = tasks_notifs_list.get(1);		//Notifications
    		tasks_list.addAll( new_tasks_list );
    		list_adapter.set_task_list( tasks_list );	//UI update. Long proces on huge list.
    		setAdapter( list_adapter );
    		
    		int num_new_tasks = new_tasks_list.size();
    		if( old_tasks_list != null )
    		{
	    		for( int i = num_new_tasks-1, j; i>=0; i--)
	    		{
	    			for(	//Search the new task into (old)tasks_list, it includes the notifications!
	    				j = old_tasks_list.size()-1;
	    				j>=0 && new_tasks_list.get(i).get("taskId").compareTo( old_tasks_list.get(j).get("taskId") ) != 0;
	    				j--
	    			);
	    			if( j==-1 )		//Not found, display a message
	    				Toast.makeText(
	    					getContext(),//.getApplicationContext(),
	    					getContext().getString
	    					(
	    						R.string.new_task)+": "
	    						+Html.fromHtml(new_tasks_list.get(i).get("description")
	    					),
	    	    			Toast.LENGTH_LONG
	    	    		).show();
	    		}
	    		if( num_tasks != num_new_tasks )
	    			notify_tasks( num_new_tasks );
    		}else
    			notify_tasks( num_new_tasks );

    		num_tasks = num_new_tasks;

    	}catch( Exception e )
    	{
    		//It's ok to remove all tasks from list?
    		if( tasks_list != null )
    		{
    			tasks_list.clear();
    			if( list_adapter != null )
    				list_adapter.set_task_list( tasks_list );
    		}
    		e.printStackTrace();
    	}
    }

    private void notify_tasks( int num_tasks)
    {
    	String msg = "Tasks: +"+String.valueOf( num_tasks );
    	
    	Intent notification_intent = new Intent( getContext(), TasksProcesses_View.class );
    	/*notification_intent.setFlags(
    		Intent.FLAG_ACTIVITY_CLEAR_TOP
    		| Intent.FLAG_ACTIVITY_SINGLE_TOP
    		//| Intent.FLAG_ACTIVITY_NEW_TASK
    	);*/
    	
    	notification = new Notification( R.drawable.icon, msg, System.currentTimeMillis() );
    	//notification.tickerText = msg;
    	notification.setLatestEventInfo
    	(
    		getContext(), msg, "",
    		PendingIntent.getActivity( getContext(), 0, notification_intent, 0 )
    	);
    	notificationManager.notify( NOTIFICATION_ID, notification );
    }
    
    public void remove_notification()
    {
    	notificationManager.cancel( NOTIFICATION_ID );
    }

    protected void  onDetachedFromWindow ()
    { clear(); }

    protected void  onAttachedToWindow()
    {
    	tasksListUpdater.postDelayed(mUpdateTimeTask, 0);
    }

    public void clear()
    {
    	updating_tasks_list = false;
    	tasksListUpdater.removeCallbacks(mUpdateTimeTask);
    	notificationManager.cancel( NOTIFICATION_ID );
    	//Better way to update the list? Use add() and remove() from ArrayAdapter
    	tasks_list = new ArrayList<Hashtable<String, String>>();
    	num_tasks = 0;
    	list_adapter.set_task_list( tasks_list );
    	setAdapter( list_adapter );
    }

}
