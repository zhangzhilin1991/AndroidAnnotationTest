package com.example.customannotationtest.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidannotation.helper.BindHelper;
import com.example.customannotationtest.R;
import com.example.injectannotation.annotation.BindView;

/**
 * Created by zhangzhilin on 12/25/19 6:50 PM.
 * Email: 1070627688@qq.com
 */
public class AnnotaionTestFragment extends Fragment {

    @BindView(R.id.annotation_fragment_test_tv)
    public TextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.layout_annotationfragment, container, false);
        //BindHelper.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BindHelper.bind(this, view);
        textView.setText("Hello, 我是Fragment里面添加注解获取的");
    }
}
