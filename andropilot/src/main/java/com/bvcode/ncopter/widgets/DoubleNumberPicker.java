package com.bvcode.ncopter.widgets;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.geeksville.andropilot.R;

public class DoubleNumberPicker extends TableRow implements OnClickListener{

    private TextView label;
    private EditText lP;
    private Button minS, minL, plusS, plusL;
   
	public DoubleNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	 
		 LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	     inflater.inflate(R.layout.double_number_picker, this, true);
	    
	     lP = (EditText) findViewById(R.id.TextP);
	     label = (TextView)findViewById(R.id.valueName);

	     minS = (Button) findViewById(R.id.doubleMinusSmall);
	     minS.setOnClickListener(this);
	     
	     minL = (Button) findViewById(R.id.doubleMinusLarge);
	     minL.setOnClickListener(this);
	     
	     plusS = (Button) findViewById(R.id.doublePlusSmall);
	     plusS.setOnClickListener(this);
	     
	     plusL = (Button) findViewById(R.id.doublePlusLarge);
	     plusL.setOnClickListener(this);

	}

	public void setLabel(String s){
		label.setText(s);
		
	}
	
	public float getValue() {
		String val = lP.getText().toString();
		
		try {
			return Float.valueOf(val);
			
		} catch (NumberFormatException e) {
			
		}
		
		return Float.NaN;
		
	}
	
	private Handler mHandler = new Handler();
	Runnable r = new Runnable() {
		@Override
		public void run() {
			lP.setBackgroundColor( Color.WHITE );
		}
	};
	
	public void setValue(float param_value, boolean isConfirm) {
		param_value = Math.round((float)(param_value * 10000)) / 10000.0f;
		    
		//value = param_value;
		lP.setText(param_value + "");
		if(isConfirm){
			lP.setBackgroundColor( Color.GREEN );
			
			//Show confirm indication for 2 seconds
			mHandler.postDelayed(r, 2000);
			
		}
	}

	@Override
	public void onClick(View v) {
		float value = getValue();
		if( v == minS){
			value -= 0.01;
		}else if( v == minL){
			value -= 0.1;
		}else if( v == plusS){
			value += 0.01;
		}else if( v == plusL){
			value += 0.1;
		}
		
		setValue(value, false);
	}

	public void saving() {
		lP.setBackgroundColor( Color.YELLOW );
		
	}
}
