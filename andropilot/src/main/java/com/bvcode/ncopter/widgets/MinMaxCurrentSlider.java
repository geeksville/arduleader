package com.bvcode.ncopter.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.geeksville.andropilot.R;

public class MinMaxCurrentSlider extends TableLayout {
	
	TextView current, min, max;
	SeekBar bar;
	
	int minRange, maxRange;
	int stable = 0;
	int lastValue = 0;
	boolean minGood = false;
	boolean maxGood = false;
	
	private int minValue, maxValue;
	private int currentValue;
	
	double scale=1, offset=0;
	
	public MinMaxCurrentSlider(Context context, AttributeSet attrs) {
		super(context, attrs);

		 LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	     inflater.inflate(R.layout.min_max_current_slider, this, true);
	 	     
	     current =(TextView) findViewById(R.id.minmax_Current);
	     min = (TextView) findViewById(R.id.minmax_Min);
	     max = (TextView) findViewById(R.id.minmax_Max);
	     bar = (SeekBar) findViewById(R.id.minmax_seekBar);
	     
	     minRange = 0; 
	     maxRange = 100;
	     
	}
	
	public void setName(String name){
		 TextView n = (TextView) this.findViewById(R.id.minmax_name);
		 n.setText(name);
				
	}

	public void setRange(int _min, int _max) {
		minRange = _min;
		maxRange = _max;
		
	}

	public void setCurrent(int i) {
		current.setText(i+"");
		max.setText("Max : " + maxValue);
		min.setText("Min : " + minValue);

	
		double range = maxRange - minRange;
		double done = (double)(i-minRange);
		bar.setProgress((int) (done/range * 100));
		
		currentValue = i;
		
		if( i < 1500-200 || i > 1500+200){
			// record the current extreme.
			if( Math.abs( i - lastValue) <= 3){
				stable++;
				
			}else{
				lastValue = i;
				stable = 0;
			}
			
			if( i < minValue){
				minValue = i;
				min.setText("Min: " + i);

			}
			
			if( i > maxValue){
				maxValue = i;
				max.setText("Max: " + i);			
				
			}
			
			if(stable > 10){ //require 10 stable readings before accepting
				if( i < 1500){
					min.setTextColor(0xFF00FF00);
					minGood = true;
				}
				
				if( i > 1500){
					max.setTextColor(0xFF00FF00);
					maxGood = true;
				}
				
			}
		}		
	}
	
	

	public int getMinValue(){
		if( minGood)
			return minValue;
		return -1;
		
	}
	
	public int getMaxValue(){
		if( maxGood)
			return maxValue;
		return -1;
		
	}
	
	public int getCurrent(){
		return currentValue;
		
	}
	
	public void resetMinMax(int i) {
		min.setText("Min : " + i);
		min.setTextColor(0xFFFFFF00);
		minValue = i;
		minGood = false;
		
		max.setText("Max : " + i);
		max.setTextColor(0xFFFFFF00);
		maxValue = i;
		maxGood = false;
		
	}

	public void setMax(int max_){
		maxValue = max_;
		maxGood = false;
		
	}
	
	public void setMin(int min_){
		minValue = min_;
		minGood = false;
		
	}
	
	public void setScaleAndOffset(double scale_, double offset_) {
		scale = scale_;
		offset = offset_;
		
	}
}
