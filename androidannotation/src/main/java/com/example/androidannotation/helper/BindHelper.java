package com.example.androidannotation.helper;

import android.app.Activity;
import android.view.View;

import com.example.injectannotation.BindProcessor;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by zhangzhilin on 12/25/19 10:45 AM.
 * Email: 1070627688@qq.com
 */
public class BindHelper {
    public static void bind(Activity context) {
        String packageName = BindProcessor.GENERATED_PACKAGE_NAME;
        String simpleName = context.getClass().getSimpleName();
        Class<?> clz = null;
        try {
            clz = context.getClassLoader().loadClass(packageName + File.separator + simpleName+"$BindView");
            Constructor constructor = clz.getConstructor(context.getClass());
            constructor.newInstance(context);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注入View
     * @param context
     * @param view
     */
    public static void bind(Object context, View view) {
        String packageName = BindProcessor.GENERATED_PACKAGE_NAME;
        String simpleName = context.getClass().getSimpleName();
        try {
            Class<?> clz = context.getClass().getClassLoader().loadClass(packageName + File.separator + simpleName+"$BindView");
            Constructor constructor = clz.getConstructor(context.getClass(), View.class);
            constructor.newInstance(context, view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
