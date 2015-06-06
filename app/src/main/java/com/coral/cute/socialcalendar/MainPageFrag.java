package com.coral.cute.socialcalendar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainPageFrag extends Fragment {

    private String userID;
    ListView listView;

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_page_layout, container, false);



        final com.prolificinteractive.materialcalendarview.MaterialCalendarView cal =
                (com.prolificinteractive.materialcalendarview.MaterialCalendarView) view.findViewById(R.id.calendarView);

        cal.setOnDateChangedListener(
                new com.prolificinteractive.materialcalendarview.OnDateChangedListener(){
                    @Override
                    public void onDateChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
                        String day = calendarDay.toString();
                        Intent intent = new Intent(getActivity(), DailyDetail.class).putExtra(Intent.EXTRA_TEXT, day);
                        startActivity(intent);
                    }
                }
        );


        // Create some dummy data for the ListView.
        String[] data = {
                "Study Android     May 12, 2015  3:02-9:05",
                "Return toy     May 13, 2015  10:00-12:00",
                "See Avengers     May 13, 2015  13:00-14:00",
                "Grand Budapest     May 13, 2015  13:00-19:00",
                "Visit London     May 14, 2015  20:00-20:47",
                "Sing music     May 14, 2015  13:00-19:00",
                "Lord of rings     May 14, 2015  13:20-19:34"
        };
        List<String> todayList = new ArrayList<String>(Arrays.asList(data));
        List<Integer> gggg = new ArrayList<Integer>(){{
            add(0);
            add(1);
            add(2);
            add(3);
            add(6);
            add(5);
            add(4);
        }};



        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        /*
        mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_todo, // The name of the layout ID.
                        R.id.list_item_todo_textview, // The ID of the textview to populate.
                        todayList);*/

        // Get a reference to the ListView, and attach this adapter to it.
        listView = (ListView) view.findViewById(R.id.listview_todo);
        List<String> placeholder = new ArrayList<String>();
        placeholder.add("Check Internet Connection");
        LazyAdapter adapter = new LazyAdapter(getActivity(),
                todayList, gggg, placeholder);
        listView.setAdapter(adapter);
        //listView.setAdapter(mForecastAdapter);
        setListViewHeightBasedOnChildren(listView);

        return view;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            userID = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        FetchDataTask fdt = new FetchDataTask();
        fdt.execute(userID);
    }

    /** Fetch data from server */
    public class FetchDataTask extends AsyncTask<String, Void, DailyList> {

        public String getWebServre(String Action, String Para) {


            String ServerUrl = "http://140.116.86.54/Public/AI_Account.aspx?";

            try {
                URL url = new URL(ServerUrl + "Action=" + Action + "&" + Para);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setConnectTimeout(6000);
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
                con.connect();


                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        con.getInputStream(), "UTF-8"));
                String jsonString = reader.readLine();
                reader.close();

                return jsonString;

            } catch (Exception e) {
                return e.toString();
            }
        }

        @Override
        protected DailyList doInBackground(String... params) {
            String dayString = "{\"day\": {\"year\": 2015, \"month\": 5, \"date\": 18}, \"todo\":";
            String todo = getWebServre("GetDay", "UserID=" + params[0] + "&Year=2015&Month=02&Date=20");
            String total = dayString + todo + "}";
            return new DailyList(total);
        }

        @Override
        protected void onPostExecute(DailyList result) {
            if (result.eventStringArray().size() == 0) {
                List<String> error = new ArrayList<String>();
                error.add("No data yet.");
                List<Integer> errorcode = new ArrayList<Integer>();
                errorcode.add(111);
                List<String> errorRemark = new ArrayList<String>();
                errorRemark.add("Check Internet Connection");
                LazyAdapter adapter = new LazyAdapter(getActivity(), error,
                        errorcode, errorRemark);
                listView.setAdapter(adapter);
            } else {
                LazyAdapter adapter = new LazyAdapter(getActivity(),
                        result.eventStringArray(),
                        result.eventTypeArray(),
                        result.eventRemarkArray());
                listView.setAdapter(adapter);
            }

        }
    }

}
