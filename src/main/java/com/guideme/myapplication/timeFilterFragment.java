package com.guideme.myapplication;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import java.util.Calendar;

public class timeFilterFragment extends Fragment
        implements View.OnClickListener
{
    public static final Pair<String, String> defaultTimeRange = Pair.create("12:00:00 AM", "11:59:00 PM");

    public Pair<String, String> timeRange = new Pair<>("", "");

    private EditText startTimeEdit;
    private EditText endTimeEdit;
    private boolean timeStartFilled = false;
    private boolean timeEndFilled = false;

    private OnTimeFilterSelected mListener;

    public timeFilterFragment() {}

    public static timeFilterFragment newInstance(String param1, String param2)
    {
        timeFilterFragment fragment = new timeFilterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
        setRetainInstance(true);
    }

    @Override public View onCreateView(LayoutInflater inflater,
                                       ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_time_filter, container, false);
    }

    private String formatTime(int hourOfDay, int minute)
    {
        String ap;
        String h;

        if (hourOfDay >= 12)
        {
            if (hourOfDay == 12)
            {
                h = "12";
            }
            else
            {
                hourOfDay -=12;
                h = (hourOfDay < 10) ? "0" + hourOfDay: String.valueOf(hourOfDay);
            }
            ap = "PM";
        }
        else
        {
            if (hourOfDay == 0)
            {
                h = "12";
            }
            else
            {
                h = (hourOfDay < 10) ? "0" + hourOfDay: String.valueOf(hourOfDay);
            }
            ap = "AM";
        }
        final String m = (minute < 10) ? "0" + minute : String.valueOf(minute);
        return h + ":" + m + ":00 " + ap;
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        final View root = view;

        final RadioGroup group = root.findViewById(R.id.timeRadioGroup);
        this.startTimeEdit = root.findViewById(R.id.time_from);
        this.endTimeEdit = root.findViewById(R.id.time_to);
        final Button applyBtn = root.findViewById(R.id.applyTime);

        this.startTimeEdit.setShowSoftInputOnFocus(false);
        this.endTimeEdit.setShowSoftInputOnFocus(false);

        startTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Calendar time = Calendar.getInstance();
                TimePickerDialog pickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                    {
                        updateTime(view, formatTime(hourOfDay, minute), timeRange.second);
                        timeStartFilled = true;
                    }
                }, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), false);
                pickerDialog.show();
            }});
        endTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Calendar time = Calendar.getInstance();
                TimePickerDialog pickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                    {
                        updateTime(view, timeRange.first, formatTime(hourOfDay, minute));
                        timeEndFilled = true;
                    }
                }, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), false);
                pickerDialog.show();
            }});

        for (int i=0; i < group.getChildCount(); ++i) {
            group.getChildAt(i).setOnClickListener(this);
        }

        final RadioButton button = root.findViewById(group.getCheckedRadioButtonId());
        onClick(button);

        applyBtn.setOnClickListener(this);
    }

    private void updateTime(View v, String timeStart, String timeEnd)
    {
        this.startTimeEdit.setText(timeStart);
        this.endTimeEdit.setText(timeEnd);
        this.timeRange = Pair.create(timeStart, timeEnd);
        this.mListener.onTimeChanged(timeStart, timeEnd);
    }

    @Override public void onClick(View v)
    {
        //TODO: FORMAT TEXT BEFORE PASS IN
        final View root = this.getView();
        final RadioGroup group = root.findViewById(R.id.timeRadioGroup);
        final RadioButton button = root.findViewById(group.getCheckedRadioButtonId());

        if (v == root.findViewById(R.id.radio_all) || button == root.findViewById(R.id.radio_all)) {
            updateTime(v, "12:00:00 AM", "11:59:00 PM");
        }
        else if (v == root.findViewById(R.id.radio_morning) || button == root.findViewById(R.id.radio_morning)) {
            updateTime(v,"05:00:00 AM", "09:59:00 AM");
        }
        else if (v == root.findViewById(R.id.radio_school) || button == root.findViewById(R.id.radio_school)) {
            updateTime(v,"08:00:00 AM", "02:59:00 PM");
        }
        else if (v == root.findViewById(R.id.radio_evening) || button == root.findViewById(R.id.radio_evening)) {
            updateTime(v,"04:00:00 PM", "06:59:00 PM");
        }
        else if (v == root.findViewById(R.id.radio_late) || button == root.findViewById(R.id.radio_late)) {
            updateTime(v,"09:00:00 PM", "02:59:00 AM");
        }
        else if (v == root.findViewById(R.id.radio_custom) || button == root.findViewById(R.id.radio_custom))
        {
            if (timeStartFilled && timeEndFilled)
            {
                updateTime(v, timeRange.first, timeRange.second);
            }
        }
    }

    @Override public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnTimeFilterSelected) {
            mListener = (OnTimeFilterSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public interface OnTimeFilterSelected
    {
        void onTimeChanged(String startTime, String endTime);
    }
}
