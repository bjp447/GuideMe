package com.guideme.myapplication;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Calendar;

public class dateFilterFragment extends Fragment
        implements View.OnClickListener
{
    public static final Pair<String, String> defaultDateRange = Pair.create("01/17/2018", "09/16/2018");

        public Pair<String, String> dateRange = new Pair<>("", "");

        private EditText startDateEdit;
        private EditText endDateEdit;
        private boolean dateStartFilled = false;
        private boolean dateEndFilled = false;

    private OnDateFilterSelected mListener;

    public dateFilterFragment() {}

    public static dateFilterFragment newInstance(String param1, String param2)
    {
        dateFilterFragment fragment = new dateFilterFragment();
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

    public Pair<String, String> getDateRange()
    {
        return this.dateRange;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_date_filter, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        View root = view;

        final RadioGroup group = root.findViewById(R.id.dateRadioGroup);
        this.startDateEdit = root.findViewById(R.id.date_from);
        this.endDateEdit = root.findViewById(R.id.date_to);
        final Button applyBtn = root.findViewById(R.id.applyDate);

        this.startDateEdit.setShowSoftInputOnFocus(false);
        this.endDateEdit.setShowSoftInputOnFocus(false);

        startDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                DatePickerDialog pickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        String m = (month < 10) ? "0" + (month+1) : String.valueOf(month+1);
                        String d = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                        updateDate(view, m + "/" + d + "/" + year, dateRange.second);
                        dateStartFilled = true;
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                pickerDialog.show();
            }});
        endDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                DatePickerDialog pickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        String m = (month < 10) ? "0" + (month+1) : String.valueOf(month+1);
                        String d = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                        updateDate(view, dateRange.first, m + "/" + d + "/" + year);
                        dateEndFilled = true;
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                pickerDialog.show();
            }});

        for (int i=0; i < group.getChildCount(); ++i) {
            group.getChildAt(i).setOnClickListener(this);
        }

        final RadioButton button = root.findViewById(group.getCheckedRadioButtonId());
        onClick(button);

        applyBtn.setOnClickListener(this);
    }

    private void updateDate(View v, String dateStart, String dateEnd)
    {
        this.startDateEdit.setText(dateStart);
        this.endDateEdit.setText(dateEnd);
        this.dateRange = Pair.create(dateStart, dateEnd);
        this.mListener.onDateChanged(dateStart, dateEnd);
    }

    @Override public void onClick(View v)
    {
        //TODO: MAKE WORK FOR PRESENT DAY
        final View root = getView();
        final RadioGroup group = root.findViewById(R.id.dateRadioGroup);
        final RadioButton button = root.findViewById(group.getCheckedRadioButtonId());

        if (v == root.findViewById(R.id.radio_week) || button == root.findViewById(R.id.radio_week)) {
            updateDate(v, "09/09/2018", "09/16/2018");
        }
        else if (v == root.findViewById(R.id.radio_month) || button == root.findViewById(R.id.radio_month)) {
            updateDate(v, "08/17/2018", "09/16/2018");
        }
        else if (v == root.findViewById(R.id.radio_3month) || button == root.findViewById(R.id.radio_3month)) {
            updateDate(v, "06/17/2018", "09/16/2018");
        }
        else if (v == root.findViewById(R.id.radio_6month) || button == root.findViewById(R.id.radio_6month)) {
            updateDate(v, "03/17/2018", "09/16/2018");
        }
        else if (v == root.findViewById(R.id.radio_12month) || button == root.findViewById(R.id.radio_12month)) {
            updateDate(v, "01/17/2018", "09/16/2018");
        }
        else if (v == root.findViewById(R.id.radio_date_custom) || button == root.findViewById(R.id.radio_date_custom))
        {
            if (dateStartFilled && dateEndFilled)
            {
                updateDate(v, dateRange.first, dateRange.second);
            }
        }
    }

    @Override public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnDateFilterSelected)
        {
            mListener = (OnDateFilterSelected) context;
        }
        else
            {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public interface OnDateFilterSelected
    {
        void onDateChanged(String dateStart, String dateEnd);
    }
}
