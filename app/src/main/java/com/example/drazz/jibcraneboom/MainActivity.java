package com.example.drazz.jibcraneboom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.xml.datatype.Duration;

public class MainActivity extends AppCompatActivity {

    Activity activity;
    Context context;
    Bitmap crane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        context = this;

        crane = BitmapFactory.decodeResource(getResources(), R.drawable.mec);

        LinearLayout main = (LinearLayout) findViewById(R.id.mainLinLay);
        main.setBackgroundColor(Color.parseColor("#607D8B"));
        LinearLayout res = (LinearLayout) findViewById(R.id.resultLinLay);

        final Line AB = new Line("AB[m]", main, this);
        //AB.setValue(4.2);
        final Line AC = new Line("AC[m]", main, this);
        //AC.setValue(2.6);
        final Line BD = new Line("BD[m]", main, this);
        //BD.setValue(0.3);
        final Line AH = new Line("AH[m]", main, this);
        //AH.setValue(2);
        final Line W = new Line("W[kN-m]", main, this);
        //W.setValue(12);
        final Line oDiam = new Line("diameter[mm]", main, this);
        //oDiam.setValue(200);
        final Line walThickness = new Line("wall thickness[mm]\n", main, this);
        //walThickness.setValue(8);

        final Line Ax = new Line("Ax", res, this);
        Ax.lock();
        final Line Ay = new Line("Ay", res, this);
        Ay.lock();
        final Line F1 = new Line("F1", res, this);
        F1.lock();
        final Line roH = new Line("stressH", res, this);
        roH.lock();
        final Line roK = new Line("stressK", res, this);
        roK.lock();

        Button buttonCalculate = (Button) findViewById(R.id.buttonCalculate);

        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double angle = Math.atan(AC.getValue()/AB.getValue());
                double momentum = W.getValue()*AB.getValue()*AB.getValue()/2;
                double inertia = Math.PI/4.0*(Math.pow((oDiam.getValue()/2.0), 4)
                        -Math.pow((oDiam.getValue()/2.0)-walThickness.getValue(), 4));

                double area = Math.pow(oDiam.getValue()/2.0, 2)*Math.PI
                        -Math.pow((oDiam.getValue()-2.0*walThickness.getValue())/2.0, 2)*Math.PI;



                double F1res = momentum/((AC.getValue()+BD.getValue())*Math.cos(angle));
                F1.setValue(F1res);
                double Ayres = F1res*Math.cos(angle);
                Ay.setValue(Ayres);
                double Axres = -F1res*Math.sin(angle)+W.getValue()*AB.getValue();
                Ax.setValue(Axres);


                double axial = -Ayres/area*1000;

                double whateva = Axres*AH.getValue()-W.getValue()*AH.getValue()*AH.getValue()/2.0;
                double bend = whateva*1000000.0*oDiam.getValue()/2.0/inertia;
                roH.setValue(axial+bend);
                roK.setValue(axial-bend);

            }
        });


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FrameLayout PicFrame = (FrameLayout) findViewById(R.id.PicFrame);
        double width = PicFrame.getWidth();
        double n = crane.getWidth()/crane.getHeight();
        crane = Bitmap.createScaledBitmap(crane, (int) width, (int) (width/n), true);


        Drawable drawable = new BitmapDrawable(getResources(), crane);
        PicFrame.setBackground(drawable);

    }

    class Line{
        Activity activity;
        Context context;
        LinearLayout parentLinLay;
        EditText editText;
        LinearLayout new_line;

        Line(String label, LinearLayout parentLinLay, Activity activity) {
            this.activity = activity;
            this.context = activity.getApplicationContext();
            this.parentLinLay = parentLinLay;

            new_line = new LinearLayout(context);
            new_line.setOrientation(LinearLayout.HORIZONTAL);
            new_line.setPadding(20,20,20,20);

            TextView labelView = new TextView(context);
            //labelView.setLayoutParams(lParams);
            labelView.setText(label);
            labelView.setWidth(240);
            labelView.setTextColor(Color.WHITE);

            editText = new EditText(context);
            editText.setMinEms(4);
            editText.setTextColor(Color.WHITE);
            //editText.setLayoutParams(lParams);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));

            new_line.addView(labelView);
            new_line.addView(editText);
            parentLinLay.addView(new_line);
        }

        public float getValue(){

            if(editText.getText().toString().matches("")){
                //Toast.makeText(context,"empty", Toast.LENGTH_SHORT).show();
                return 0.000001f;
            }else{
                //Toast.makeText(context,"not_empty", Toast.LENGTH_SHORT).show();
                return Float.valueOf(editText.getText().toString());
            }
        }

        public void setValue(double value){
            editText.setText(String.format("%.2f", value));
        }

        public void lock(){
            editText.setEnabled(false);
        }
    }
}
