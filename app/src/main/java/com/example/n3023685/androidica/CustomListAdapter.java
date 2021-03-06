package com.example.n3023685.androidica;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by n3023685 on 23/04/19.
 */

public class CustomListAdapter extends ArrayAdapter {
    private final Activity context;

    private final String[] nameArray;

    private final String[] infoArray;
    TextView nameTextField;
    TextView infoTextField;

    public CustomListAdapter(Activity context, String[] nameArrayParam, String[] infoArrayParam) {

        super(context, R.layout.listview_row, nameArrayParam);

        this.context = context;
        this.nameArray = nameArrayParam;
        this.infoArray = infoArrayParam;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.listview_row, null, true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = rowView.findViewById(R.id.nameTextViewID);
        TextView infoTextField = rowView.findViewById(R.id.infoTextViewID);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray[position]);
        infoTextField.setText(infoArray[position]);

        return rowView;
    }

}
