package com.blauadvisors.diui;

import java.util.ArrayList;
import java.util.Hashtable;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.blauadvisors.diui.IntalioWS.IntalioWSBinder;

public class Processes_ListView extends ListView implements OnItemClickListener
{
	private IntalioWS intalio = null;
	//private ArrayList<Hashtable<String, String>> processes_list;
	private TasksProcesses_ListAdapter list_adapter;
/*
	//Does it need to refresh the _processes_ list? it's not updated so often.
	public Handler processesListUpdater = new Handler();
	public Runnable mUpdateTimeTask = new Runnable( )
	{
		public void run()
		{
			try {
				refresh_processes_list();
				processesListUpdater.postDelayed(this, 15000);	//Refresh the tasks list again after some time-interval
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
*/
	public Processes_ListView(Context context, ViewGroup header)
	{
		super(context);

        addHeaderView( header );
        setOnItemClickListener( this );
		list_adapter = new TasksProcesses_ListAdapter( context );
        // Bind to IntalioWS
        context.bindService
        (
        	new Intent(context, IntalioWS.class),
        	mConnection,
        	Context.BIND_AUTO_CREATE
        );
	}

	public void onItemClick(AdapterView<?> adapter, View item, int position, long id)
	{
		try {			
			//Intent intent = new Intent().setClass(getContext(), XSD_ProcessesForm_Activity.class);
			Intent intent = new Intent().setClass(getContext(), Canvas_ProcessesForm_Activity.class);
			intent.setAction("start_process");
			intent.putExtra("task_id", ((Intd_TaskRow)item).id);
			intent.putExtra("task_url", ((Intd_TaskRow)item).url);
			intent.putExtra("task_ns", ((Intd_TaskRow)item).ns);
			intent.putExtra("task_type", "INIT");
			//intent.putExtra("process_id", ((Intd_TaskRow)item).id);
			//intent.putExtra("process_url", ((Intd_TaskRow)item).url);
			getContext().startActivity(intent);

		} catch (Exception e) {
			//processesListUpdater.postDelayed(mUpdateTimeTask, 15000);
			e.printStackTrace();
		}
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
            	refresh_processes_list();
            	//processesListUpdater.postDelayed(mUpdateTimeTask, 0);

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

    private void refresh_processes_list()
    {
    	try{
    		//Better way to update the list? Use add() and remove() from ArrayAdapter
			list_adapter.set_task_list( intalio.get_processes_list() );
			setAdapter(list_adapter);
    	}catch( Exception e ){
    		//It's ok to remove all tasks from list?
    		list_adapter.set_task_list( new ArrayList<Hashtable<String, String>>() );
    		setAdapter(list_adapter);
    		e.printStackTrace();
    	}
    }
    
    public void clear()
    {
    	//processesListUpdater.removeCallbacks(mUpdateTimeTask);
    	//Better way to update the list? Use add() and remove() from ArrayAdapter
    	list_adapter.set_task_list(new ArrayList<Hashtable<String, String>>());
    	setAdapter(list_adapter);
    }
}
