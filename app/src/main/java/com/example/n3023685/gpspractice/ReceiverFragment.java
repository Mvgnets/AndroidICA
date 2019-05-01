package com.example.n3023685.gpspractice;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by n3023685 on 01/05/19.
 */

public class ReceiverFragment extends Fragment {
    public ReceiverFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReceiverFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReceiverFragment newInstance() {
        ReceiverFragment fragment = new ReceiverFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receiver, container, false);
    }

    public void updateText(final String text) {
        TextView tv = getView().findViewById(R.id.messageText);

        tv.setText(text);
    }
}
