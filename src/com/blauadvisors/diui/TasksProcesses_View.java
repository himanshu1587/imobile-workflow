package com.blauadvisors.diui;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.blauadvisors.diui.IntalioWS.IntalioWSBinder;

public class TasksProcesses_View extends TabActivity implements OnClickListener
{
	private IntalioWS intalio = null;
	private Dialog login;
	private Tasks_ListView tasks_view;
	private Processes_ListView processes_view;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//"StrictMode" for Andrdoid 3.0, only!
		//if( android.os.Build.VERSION.SDK_INT >= 11 )	//http://stackoverflow.com/questions/4821845/honycomb-and-defaulthttpclient
		//{
			//if( DEBUG_MODE )
				//StrictMode.setThreadPolicy( ThreadPolicy.LAX );
			//else
				StrictMode.enableDefaults();
		//}
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawableResource( R.drawable.background_bw_1280x768_jpg );
		//setContentView(R.layout.tasksprocesses_tabs);

		if( intalio == null || intalio.isTokenNull() )		//Avoid showing if already connected, happens on screen turn.
			bindService
			(
				new Intent( this, IntalioWS.class ),
				mConnection,
				Context.BIND_AUTO_CREATE
			);

	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		//super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		//super.onOptionsItemSelected(item);
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.logout:
	        logout();
	        return true;
	    case R.id.exit:
	        finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private List<Server_info> read_connection_settings()
	{
		String line;
		String fields[];
		Server_info server;
		List<Server_info> settings = new ArrayList<Server_info>();
		try{
			//deleteFile("connection_settings.csv");
			BufferedReader file_reader = new BufferedReader(
				new InputStreamReader( openFileInput( "connection_settings.csv" ) )
			);
			//Server_info empty_server = new Server_info();
			//empty_server.setServer( getResources().getString( R.string.select_server ) );
			//Log.d("RCS", ""+getString( R.string.select_server ));
			settings.add( new Server_info() );
			while( (line = file_reader.readLine()) != null )
			{
				fields = line.split("\t");
				if( fields.length == 3 )	//Simple check to avoid some exceptions
				{
					server = new Server_info();
					server.setServer(fields[0]);
					server.setUser(fields[1]);
					server.setPwd(fields[2]);
					settings.add( server );
				}
			}
			file_reader.close();
			return settings;

		}catch( Exception e ){
			//e.printStackTrace();
			return settings;
		}
	}
	
	protected Dialog onCreateDialog(int id, Bundle b )
	{
		login = new Dialog( this );
		login.setContentView(R.layout.login);
		login.setTitle(R.string.server_n_login);
		
		final List<Server_info> servers_info = read_connection_settings();
		ArrayAdapter<Object> aa = new ArrayAdapter<Object> (
			this,
			android.R.layout.simple_spinner_item,
			servers_info.toArray()
		);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner servers = (Spinner)login.findViewById(R.id.know_servers);
		servers.setAdapter(aa);
		final class OnSelectServer implements OnItemSelectedListener
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
			{
				Server_info si = (Server_info)parent.getItemAtPosition( pos );
				((EditText)login.findViewById(R.id.server)).setText( si.getServer() );
				((EditText)login.findViewById(R.id.user)).setText( si.getUser() );
				((EditText)login.findViewById(R.id.password)).setText( si.getPwd() );
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0){ }
		}
		servers.setOnItemSelectedListener(new OnSelectServer());
		Button connect = (Button)login.findViewById(R.id.connect);
		connect.setOnClickListener(new OnClickListener(){
		    public void onClick(View v)
		    {
		    	connect();
		    }
		});
		Button save = (Button)login.findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		    	try{
		    		Server_info si = new Server_info();
		    		si.setServer( ((EditText)login.findViewById(R.id.server)).getText().toString() );
		    		si.setUser( ((EditText)login.findViewById(R.id.user)).getText().toString() );
		    		si.setPwd( ((EditText)login.findViewById(R.id.password)).getText().toString() );
		    		int i;
		    		for(	//Search if the server already exists
		    			i = servers_info.size()-1;
		    			i >= 0 && servers_info.get(i).toString().compareTo(si.toString()) != 0;
		    			i--
		    		);
		    		if( i != -1 )	//Found: delete (and replace)
		    			servers_info.remove(i);

	    			//Write the connection settings from scratch (any better code?)
	    			FileOutputStream fos = openFileOutput("connection_settings.csv", Context.MODE_PRIVATE);
	    			for( i = servers_info.size()-1; i>=0; i--)
	    				if( servers_info.get(i).getServer() != null )
	    					fos.write(servers_info.get(i).toFile());
	    			fos.write(si.toFile());
	    			fos.close();
/*
	    			ArrayAdapter<Object> aa = new ArrayAdapter<Object> (
	    				v.getContext(),
	    				android.R.layout.simple_spinner_item,
	    				servers_info.toArray()
	    			);
	    			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    			Spinner servers = (Spinner)login.findViewById(R.id.know_servers);
	    			servers.setAdapter(aa);
*/
	    			Toast.makeText( getApplicationContext(), R.string.server_saved, Toast.LENGTH_LONG ).show();

		    	}catch (Exception e){ e.printStackTrace(); }
		    	
		    }
		});
		Button remove = (Button)login.findViewById(R.id.remove);
		remove.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		    	try{
		    		Server_info si = new Server_info();
		    		si.setServer( ((EditText)login.findViewById(R.id.server)).getText().toString() );
		    		si.setUser( ((EditText)login.findViewById(R.id.user)).getText().toString() );
		    		int i;
		    		for(	//Search if the server already exists
		    			i = servers_info.size()-1;
		    			i >= 0 && servers_info.get(i).toString().compareTo(si.toString()) != 0;
		    			i--
		    		);
		    		if( i >= 0 )	//Found
		    		{
		    			servers_info.remove(i);		//Remove it
		    			//Write the connection settings from scratch (any better code?)
		    			FileOutputStream fos = openFileOutput("connection_settings.csv", Context.MODE_PRIVATE);
		    			for( i = servers_info.size()-1; i>=0; i--)
		    				if( servers_info.get(i).getServer() != null )
		    					fos.write(servers_info.get(i).toFile());
		    			fos.close();
		    			Toast.makeText(getApplicationContext(), R.string.server_removed, Toast.LENGTH_LONG).show();
		    		}else
		    			Toast.makeText(getApplicationContext(), R.string.server_not_found, Toast.LENGTH_LONG).show();

		    	}catch (Exception e){ e.printStackTrace(); }
		    	
		    }
		});
		return login;
	}
	
	public void print_ui()
	{ try{
		
		//setContentView(R.layout.tasksprocesses_tabs);
		Resources res = getResources();	// Resource object to get Drawables
		TabHost tabHost = getTabHost();
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.tasksprocesses_header, processes_view, false);

	    // Initialize a TabSpec for each tab and add it to the TabHost
		tasks_view = new Tasks_ListView(this, header);
        TabSpec tasks = tabHost.newTabSpec("tasks");
		tasks.setIndicator( res.getString(R.string.tasks), res.getDrawable(R.drawable.tab_tasks) );
		tasks.setContent( new TabContentFactory()
		{
			public View createTabContent(String tag){ return tasks_view; }
		});
		tabHost.addTab( tasks );
		
		processes_view = new Processes_ListView(this, header);
        TabSpec processes = tabHost.newTabSpec("processes");
	    processes.setIndicator(res.getString(R.string.processes), res.getDrawable(R.drawable.tab_processes) );
	    processes.setContent( new TabContentFactory()
		{
			public View createTabContent(String tag){ return processes_view; }
		});

	    tabHost.addTab(processes);

	    //tabHost.setCurrentTab(1);
		tabHost.setCurrentTab(0);

	 }catch (Exception e) { e.printStackTrace(); }
	}

	public void onClick(View v){ }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
        	setContentView(R.layout.tasksprocesses_tabs);
            // We've bound to IntalioWSService, cast the IBinder and get IntalioWSService instance
            try{
            	intalio = ((IntalioWSBinder)service).getService();
            	showDialog(R.layout.login);

            }catch( Exception e ){ e.printStackTrace(); }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0){ intalio = null; }
    };

    private void connect()
    {
    	if( intalio == null )
    		Toast.makeText( getApplicationContext(), R.string.couldnt_connect, Toast.LENGTH_LONG).show();

    	intalio.setServer(
       		((EditText)login.findViewById(R.id.server)).getText().toString()	
   		);
       	intalio.setUser(
       		((EditText)login.findViewById(R.id.user)).getText().toString()
       	);
       	intalio.setPort(8080);
       	intalio.xmlPost_login(
       		((EditText)login.findViewById(R.id.password)).getText().toString()		
       	);
       	if( !intalio.isTokenNull() )
       	{
       		//unbindService(mConnection);
       		print_ui();
       		login.hide();
       	}else{
       		Toast.makeText( getApplicationContext(), R.string.couldnt_connect, Toast.LENGTH_LONG).show();
       		//showDialog(R.layout.login);
       	}
    }
	
    private void clear()
    {
    	if( tasks_view != null )
    		tasks_view.clear();
    	if( processes_view != null )
    		processes_view.clear();

   		getTabHost().clearAllTabs();
   		//unbindService(mConnection);
   		//intalio = null;
    }
    
    private void logout()
    {
    	clear();
   		showDialog(R.layout.login);
    }
}