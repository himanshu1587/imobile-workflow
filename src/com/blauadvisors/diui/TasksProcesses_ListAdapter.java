package com.blauadvisors.diui;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class TasksProcesses_ListAdapter extends BaseAdapter //implements OnClickListener
{
	private Context context;
	private List<Intd_TaskRow> task_list = new ArrayList<Intd_TaskRow>();
	
	public TasksProcesses_ListAdapter(Context c)
	{
		//super(arg0, arg1)
		context = c;
	}	
	
	public TasksProcesses_ListAdapter(Context c, OnItemClickListener click )
	{
		context = c;
	}

	public TasksProcesses_ListAdapter(Context c, ArrayList<Hashtable<String, String>> tasks_data, OnItemClickListener click )
	{
		context = c;
		set_task_list(tasks_data);
	}
	
	@Override
	public int getCount(){ return task_list.size(); }

	@Override
	public Object getItem(int i){ return task_list.get(i); }

	@Override
	public long getItemId(int position){ return position; }

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		//if( position < task_list.size() )
			//return null;
		//new Intd_TaskRow(context);
		return (View) task_list.get(position);
	}

	public void set_task_list( ArrayList<Hashtable<String, String>> tasks_data )
	{
		task_list.clear();
		Intd_TaskRow itv;
		for( int i = tasks_data.size()-1; i >= 0; i-- )
		{
			itv = new Intd_TaskRow( context, tasks_data.get(i) );
			task_list.add( itv );
		}
	}

}
