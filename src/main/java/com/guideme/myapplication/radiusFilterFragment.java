package com.guideme.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class radiusFilterFragment extends Fragment
{
    private static final String PROGRESS = "param1";
    private int seekBarProgress;

    private OnRadiusFilterSelected mListener;

    public radiusFilterFragment() {}

    public static radiusFilterFragment newInstance(int seekBarProgress) {
        radiusFilterFragment fragment = new radiusFilterFragment();
        Bundle args = new Bundle();
        args.putInt(PROGRESS, seekBarProgress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            this.seekBarProgress = getArguments().getInt(PROGRESS);
        }
        setRetainInstance(true);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View root = inflater.inflate(R.layout.fragment_radius_filter, container, false);

        final TextView seekBarTxt = root.findViewById(R.id.seekBarTxt);

        final SeekBar seekBar = (SeekBar)root.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (progress < 1)
                {
                    seekBar.setProgress(1);
                }
                else
                {
                    //seekBarTxt.setText(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar)
            {
                mListener.onRadiusChanged(seekBar.getProgress()*CrimeDisplay.MILE);
            }
        });

        Button applyBtn = root.findViewById(R.id.applyRadius);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mListener.onRadiusChanged(seekBar.getProgress()*CrimeDisplay.MILE);
            }});
        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onRadiusChanged(0.0);
        }
    }

    @Override public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnRadiusFilterSelected) {
            mListener = (OnRadiusFilterSelected) context;
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

    public interface OnRadiusFilterSelected
    {
        void onRadiusChanged(double radius);
    }
}
