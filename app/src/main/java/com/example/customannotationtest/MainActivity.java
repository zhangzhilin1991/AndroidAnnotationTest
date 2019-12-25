package com.example.customannotationtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.androidannotation.helper.BindHelper;
//import com.example.customannotation.GeneratedClass;
import com.example.customannotationtest.Fragment.AnnotaionTestFragment;
import com.example.injectannotation.annotation.BindView;

//@CustomAnnotation
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @BindView(R.id.annotation_test_tv)
    public TextView textView;

    @BindView(R.id.container)
    public FrameLayout container;

    Fragment fragment;

    //@CustomAnnotation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BindHelper.bind(this);

        textView.setText("hello inject processor!");

        //showAnnotationMessage();
        setFragment();
    }

    private void setFragment() {
        if (fragment == null) {
            fragment = new AnnotaionTestFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(container.getId(), fragment).addToBackStack(null).commit();
    }

    /*private void showAnnotationMessage() {
        GeneratedClass generatedClass = new GeneratedClass();
        String message = generatedClass.getMessage();
        // android.support.v7.app.AlertDialog
        new AlertDialog.Builder(this)
                .setPositiveButton("Ok", null)
                .setTitle("Annotation Processor Messages")
                .setMessage(message)
                .show();
    }
     */
}
