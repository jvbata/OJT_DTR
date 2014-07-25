package com.example.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gztrackz.DB_User_Time_Log;
import com.example.gztrackz.R;
import com.example.gztrackz.ResultListAdapter;
import com.example.gztrackz.StandUpsDialog;
import com.example.gztrackz.TimeLog;
import com.example.gztrackz.TimeStampQueryDialog;

public class TimestampsFragment extends Fragment {
	
	private String PREFERENCE_NAME = "com.example.gztrackz",FNAME = "com.example.gztrackz.firstname",LNAME = "com.example.gztrackz.lastname",EMAIL="com.example.gztrackz.email";
	private SharedPreferences prefs ;
	private String email;
	private boolean firstCreate=true;
	private DB_User_Time_Log timeLogDB;
	private List<TimeLog> timelogs;
	private Button queryBTN;
	private ListView resultListView;
	private List<TimeLog> resultList;
	private ResultListAdapter resultListAdapter;
	private TextView noRecords,dateTXT;	
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_timestamp, container, false);
		prefs = getActivity().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);		
		email = prefs.getString(EMAIL, null);
		
		noRecords = (TextView) rootView.findViewById(R.id.norecordfound);
		dateTXT = (TextView) rootView.findViewById(R.id.dateTextView);
		resultList = new ArrayList();
		
		resultListAdapter = new ResultListAdapter(getActivity(),resultList);
		resultListView = (ListView) rootView.findViewById(R.id.timeintimeoutlist);		
		queryBTN = (Button) rootView.findViewById(R.id.historyquerybutton);		
		
		noRecords.setVisibility(View.INVISIBLE);
		if(firstCreate){
			timeLogDB = new DB_User_Time_Log(getActivity());
			timeLogDB.open();
			timeLogDB.removeAll();
			new RetrieveTimeLogHistory(getActivity(),email).execute();		
			firstCreate = false;
		}
		
		
		
		queryBTN.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getActivity(),TimeStampQueryDialog.class);
				startActivityForResult(i,1);
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {	
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==1){
			if(resultCode==getActivity().RESULT_OK){
				Log.d("year",Integer.toString(data.getIntExtra("year",0)));
				Log.d("month",Integer.toString(data.getIntExtra("month",0)));
				Log.d("day",Integer.toString(data.getIntExtra("day",0)));
				int day = data.getIntExtra("day",0);
				int month = data.getIntExtra("month",0);
				int year = data.getIntExtra("year",0);
				String date2,date;
				if(year!=0){
					date = Integer.toString(year) + "-" ;
					
					if(month!=0){
						if(month<10){
							date = date +"0" + Integer.toString(month) + "-";			
						}else{
							date = date + Integer.toString(month) + "-";
						}
						if(day!=0){
							if(day<10){
								date = date + "0" + Integer.toString(day) + " 00:00:00";
							}else{
								date = date + Integer.toString(day) + " 00:00:00";
							}
							dateTXT.setText(date.substring(0,11));
							date2 = nextDay(year,month,day);
							Log.d("Today",date);
							Log.d("Tomorrow", date2);
						}else{
							dateTXT.setText(date.substring(0,date.length()-1));
							date = date + "01 00:00:00";
							date2 = nextMonth(year,month);							
							Log.d("Today",date);
							Log.d("Next Month", date2);							
						}
					}else{
						dateTXT.setText(date.substring(0,date.length()-1));
						date = date + "01-01 00:00:00";
						date2 = Integer.toString(year+1) +"-01-01 00:00:00"; 
						Log.d("Today",date);
						Log.d("Next Year", date2);						
					}					
					resultList= timeLogDB.getAllDay(email, date, date2);					
					if(resultList.size()>0){						
						for(int init=0;init<resultList.size();init++){
							Log.d("RESULT", resultList.get(init).getTimeIn() + "     " + resultList.get(init).getTimeOut());							
						}												
						resultListAdapter = new ResultListAdapter(getActivity(),resultList);
						resultListView.setAdapter(resultListAdapter);
						noRecords.setVisibility(View.INVISIBLE);
					}else{											
						noRecords.setVisibility(View.VISIBLE);
					}
					resultListAdapter = new ResultListAdapter(getActivity(),resultList);
					resultListView.setAdapter(resultListAdapter);
					//Toast.makeText(getActivity(), Integer.toString(resultList.size()),Toast.LENGTH_SHORT).show();
				}else{
					dateTXT.setText("---- -- --");
				}
			}			
		}
		
	}
	
	
	private String nextDay(int year,int month, int day){
		String flag=null;
		day++;
		if((month==1||month==3||month==5||month==7||month==8||month==10||month==12 )&& day==32){
			day = 1;
			month++;
		}else if((month==4||month==6||month==9||month==11) && day==31){
			day = 1;
			month++;
		}else if (month==2){			
			if(year%4==0 && day==30){
				day = 1;
				month++;
			}else if (day==29){
				day=1;
				month++;
			}
		}
		
		if(month==13){
			month=1;
			year++;
		}
		flag = Integer.toString(year) + "-" ;
		
		if(month<10){
			flag = flag +"0" + Integer.toString(month) + "-";			
		}else{
			flag = flag + Integer.toString(month) + "-";
		}
		
		if(day<10){
			flag = flag + "0" + Integer.toString(day) + " 00:00:00";
		}else{
			flag = flag + Integer.toString(day) + " 00:00:00";
		}
		
		return flag;
	}
	
	private String nextMonth(int year,int month){
		String flag = null;
		month++;
		if(month==13){
			year++;
			month=1;
		}
		flag = Integer.toString(year);
		
		if(month<10){
			flag = flag + "-0" + Integer.toString(month) + "-01 00:00:00";
		}else{
			flag = flag + "-" + Integer.toString(month) + "-01 00:00:00";			
		}
		
		return flag;
	}

	private class RetrieveTimeLogHistory extends AsyncTask<String, Void,Boolean> {	        
		String email;
		Context context;
		
		public RetrieveTimeLogHistory(Context context,String email){
			this.context = context;
			this.email = email;			
		}
		
		@Override
	    protected void onPreExecute() {
			Log.d("STARTED RETRIEVING TIMELOG HISTORY","GOGOGOGOGO");
			queryBTN.setText("Retrieving history....");
			queryBTN.setEnabled(false);
	    }
		
		@Override
	    protected void onPostExecute(Boolean result) {        	
			queryBTN.setText("Search");
			queryBTN.setEnabled(true);
	    }
		
		@Override
	    protected Boolean doInBackground(String... params) {
	        boolean flag = true;	            
	        try {
	        	
	        	String urlTopTracks;
				TimeLog latest =timeLogDB.getLatestRowOf(email);
				Log.d("LATEST","");
				if(latest.getEmail()!=null){
					String timein = latest.getTimeIn();
					timein = timein.replace(" ", "%20");
					urlTopTracks = "http://gz123.site90.net/get_timein_greater/?email=" + email +"&timein=" + timein;
					Log.d("CALLED", latest.getEmail() + "'");
				}else{
					urlTopTracks = "http://gz123.site90.net/get_userlog/?email=" + email ;
					Log.d("CALLED NONE", latest.getEmail() + "");
					
				}
				
				HttpClient client = new DefaultHttpClient();
				ResponseHandler<String> handler = new BasicResponseHandler();
				
				HttpPost request = new HttpPost(urlTopTracks);
				
				String httpResponseTopTracks = client.execute(request, handler);				
				
				StringTokenizer token = new StringTokenizer(httpResponseTopTracks,"<");
				String retrieveResult = token.nextToken();
				JSONArray timeLogResult = new JSONArray(retrieveResult);
				JSONObject temp;
				for(int init = 0;init<timeLogResult.length();init++){					
					temp = timeLogResult.getJSONObject(init);
					if(latest.getEmail()!=null&&init==0){
						timeLogDB.updateRow(email,temp.getString("timein") , temp.getString("timeout"));
					}else
						timeLogDB.insertRow(email,temp.getString("timein") , temp.getString("timeout"));
				}
				Log.d("TIMELOG HISTORY     " + Integer.toString(timeLogResult.length()),retrieveResult);				
			} catch (Exception e) {			
				flag = false;
				e.printStackTrace();
			}
	        
	        return flag;
	    }	          
	}
}
