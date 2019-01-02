package edu.rit.csh.bettervent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatusFragment extends Fragment {

    private ConstraintLayout mStatusLayout;
    private TextView mReserved;
    private TextView mFree;
    private TextView mNext;

    private TextView mEventTitle;
    private TextView mEventTime;
    private TextView mNextTitle;
    private TextView mNextTime;

    public List<Event> events;

    public String textMessage;
    public String currentTitle;
    public String currentTime;
    public String nextTitle;
    public String nextTime;

    public static StatusFragment newInstance(List<Event> events) {
        StatusFragment f = new StatusFragment();
        Bundle args = new Bundle();
        // I guess you can serialize events. Huh.
        System.out.println("STAT_: " + events);
        args.putSerializable("events", (Serializable) events);
        f.setArguments(args);
        return f;
    }

    /**
     * Extract information from the bundle that may have been provided with the StatusFragment,
     * inflate status_layout and set it as the currently active view, then make references to all of
     * the various pieces of the UI so that the class can update the UI with the API data.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        infoPrint("Loaded Status Fragment.");

        Bundle args = getArguments();
        if (args != null) {
            infoPrint("Found status data: " + args.getSerializable("events"));
            events = (List<Event>) args.getSerializable("events");
            getCurrentAndNextEvents();
//            textMessage = args.getString("textMessage");
//            currentTitle = args.getString("currentTitle");
//            currentTime = args.getString("currentTime");
//            nextTitle = args.getString("nextTitle");
//            nextTime = args.getString("nextTime");

        } else {
            infoPrint("ERROR! NO DATA FOUND!");
        }

        View view = inflater.inflate(R.layout.fragment_status, container, false);

        mStatusLayout = view.findViewById(R.id.status_layout);

        mReserved = view.findViewById(R.id.reserved_label);
        mFree = view.findViewById(R.id.free_label);
        mNext = view.findViewById(R.id.next_label);

        mEventTitle = view.findViewById(R.id.event_title);
        mEventTime = view.findViewById(R.id.event_time);

        mNextTitle = view.findViewById(R.id.next_event_title);
        mNextTime = view.findViewById(R.id.next_event_time);

        if (currentTitle == null) {
            textMessage = currentTitle = currentTime = nextTitle = nextTime = "";
        }

        if (nextTitle == null) nextTitle = "";

        return view;
    }

    /**
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        infoPrint("Fragment Event Title: " + currentTitle); // TODO: The fragment is STILL not getting updates. Fuck.
        setRoomStatus();
    }

    /**
     *
     */
    private void setRoomFree() {
        mReserved.setVisibility(View.INVISIBLE);
        mFree.setVisibility(View.VISIBLE);
        mEventTitle.setText("");
        mEventTime.setText("");
        mStatusLayout.setBackgroundColor(getResources().getColor(R.color.CSHGreen));
    }

    /**
     *
     */
    private void setRoomStatus() {
        if (!currentTitle.equals("")) {
            mFree.setVisibility(View.INVISIBLE);
            mReserved.setVisibility(View.VISIBLE);
            mEventTitle.setText(currentTitle); // mEventTitle is null when this method is caused, thus invoking a NullPointerException.
            mEventTime.setText(currentTime);
            mStatusLayout.setBackgroundColor(getResources().getColor(R.color.CSHRed));
        } else {
            setRoomFree();
        }
        setRoomFuture();
    }

    /**
     *
     */
    private void setRoomFuture() {
        if (nextTitle != "") {
            mNext.setVisibility(View.VISIBLE);
            mNextTitle.setText(nextTitle);
            mNextTime.setText(nextTime);
        } else {
            mNext.setVisibility(View.INVISIBLE);
            mNextTitle.setText("There are no upcoming events.");
            mNextTime.setText("");
        }
    }


    /**
     * Looks at the APIOutList (the List of Events generated by the API),
     * and based on how many there are and when they are, sets the string
     * values for currentEventTitle, currentEventTime, nextEventTitle, and
     * nextEventTime.
     */
    private void getCurrentAndNextEvents() {
        if (events == null)
            infoPrint("There may have been an issue getting the data.");

        if (events == null || events.size() == 0) {
            currentTitle = "";
            currentTime = "";
            nextTitle = "";
            nextTime = "";
        } else {
            //Here's all the data we'll need.
            String summary = events.get(0).getSummary();
            DateTime start = events.get(0).getStart().getDateTime();
            DateTime end = events.get(0).getEnd().getDateTime();

            if (start == null) { // If the event will last all day...
                // (All-day events don't have start times, so just use the start date)
                start = events.get(0).getStart().getDate();
                currentTitle = summary;
                currentTime = formatDateTime(start);
            } else { // If the event has a set start and end time...
                DateTime now = new DateTime(System.currentTimeMillis());
                if (start.getValue() > now.getValue()) { // If the first event will happen in the future...
                    // Then we don't have a current event.
                    currentTitle = "";
                    currentTime = "";
                    nextTitle = summary;
                    nextTime = formatDateTime(start) + " — " + formatDateTime(end);
                } else { // If the first event is happening right now...
                    currentTitle = summary;
                    currentTime = formatDateTime(start) + " — " + formatDateTime(end);
                    if (events.size() > 1) // If there's an event after this one...
                        getNextEvent();
                }
            }
        }
    }

    /**
     * Takes the second index of APIOutList (the List of Events generated by the API)
     * and sets nextEventTitle and nextEventTime.
     */
    //TODO: This looks like you should put it in the StatusFragment.
    private void getNextEvent() {
        try {
            String nextEventSummary = events.get(1).getSummary();
            DateTime nextEventStart = events.get(1).getStart().getDateTime();
            DateTime nextEventEnd = events.get(1).getEnd().getDateTime();
            if (nextEventStart == null) {
                // All-day events don't have start times, so just use
                // the start date.
                nextEventStart = events.get(1).getStart().getDate();
            }
            nextTitle = nextEventSummary;
            nextTime = formatDateTime(nextEventStart) + " — " + formatDateTime(nextEventEnd);
        } catch (Exception e) {
            nextTitle = "";
            nextTime = "";
        }
    }

    /**
     * Method to format DateTimes into human-readable strings
     *
     * @param dateTime: DateTime to make readable
     * @return: HH:MM on YYYY/MM/DD
     */
    private String formatDateTime(DateTime dateTime) {
        String[] t = dateTime.toString().split("T");
        String time = t[1].substring(0, 5);
        String[] date = t[0].toString().split("-");
        String dateString = date[0] + "/" + date[1] + "/" + date[2];

        return time + " on " + dateString;
    }

    private void infoPrint(String info) {
        System.out.println("STAT_: " + info);
    }
}