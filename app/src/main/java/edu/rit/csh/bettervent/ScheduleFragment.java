package edu.rit.csh.bettervent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.api.services.calendar.model.Event;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment implements MonthLoader.MonthChangeListener<Event>{

    WeekView weekView;
    List<Event> events;

    public static ScheduleFragment newInstance(List<Event> events){
        ScheduleFragment f = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putSerializable("events", (Serializable) events);
        f.setArguments(args);
        return f;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        infoPrint("Loaded Schedule Fragment.");
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        Bundle args = getArguments();
        if (args != null) {
            infoPrint("Found events data");
            events = (List<Event>) args.getSerializable("events");
            if (events != null && events.size() > 0)
                 infoPrint("First event title: " + events.get(0).getSummary());
        }else{
            infoPrint("ERROR! NO DATA FOUND!");
        }

        MainActivity.centralClock.setTextColor(0xff000000);

        weekView = view.findViewById(R.id.week_view);
        weekView.setMonthChangeListener(this);
        weekView.setNumberOfVisibleDays(7);

        // Lets change some dimensions to best fit the view.
        weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        weekView.setTimeColumnTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
        weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     */
    private void setupDateTimeInterpreter() {
        weekView.setDateTimeInterpreter(new DateTimeInterpreter() {

            SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

            @Override
            public String interpretDate(Calendar date) {
                String weekday = weekdayNameFormat.format(date.getTime());
                if (weekView.getNumberOfVisibleDays() == 7) {
                    weekday = String.valueOf(weekday.charAt(0));
                }
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    protected String getEventTitle(Calendar time) {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);
        int month = time.get(Calendar.MONTH) + 1;
        int dayOfMonth = time.get(Calendar.DAY_OF_MONTH);
        return String.format(Locale.getDefault(), "Event of %02d:%02d %s/%d", hour, minute, month, dayOfMonth);
    }

    private void infoPrint(String info){
        System.out.println("SCHE_: " + info);
    }

    @Override
    public List<WeekViewDisplayable<Event>> onMonthChange(Calendar startDate, Calendar endDate) {

        List<WeekViewDisplayable<Event>> weekViewEvents = new ArrayList<>();

        final int color1 = getResources().getColor(R.color.colorPrimaryDark);

        if (events != null){
            infoPrint("event size : " + events.size());
            for (int i = 0; i < events.size(); i++){
                Event event = events.get(i);
                WeekViewEvent wve = new WeekViewEvent();

                // Set ID (not the Google Calendar ID).
                wve.setId(i);

                // Set Title
                wve.setTitle(event.getSummary());
                
                final int newYear = startDate.get(Calendar.YEAR);
                final int newMonth = startDate.get(Calendar.MONTH);

                //TODO: NullPointerException still happens somewhere in WeekViewEvent.
                try{
                    // Start Time
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTimeInMillis(event.getStart().getDateTime().getValue());
                    startCal.set(Calendar.MONTH, newMonth);
                    startCal.set(Calendar.YEAR, newYear);
                    wve.setStartTime(startCal);

                    // End Time
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTimeInMillis(event.getEnd().getDateTime().getValue());
                    endCal.set(Calendar.MONTH, newMonth);
                    endCal.set(Calendar.YEAR, newYear);
                    wve.setEndTime(endCal);
                }catch (NullPointerException error){
                    error.printStackTrace();
                    wve.setIsAllDay(true);
                }

                wve.setColor(color1);

                wve.setIsAllDay(false);

                weekViewEvents.add(wve);
            }
        }
        return weekViewEvents;
    }
}
