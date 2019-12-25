package com.example.injectannotation;

import com.example.injectannotation.annotation.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by zhangzhilin on 12/25/19 9:15 AM.
 * Email: 1070627688@qq.com
 */
@AutoService(Processor.class)
public class BindProcessor extends AbstractProcessor {

    public static final String GENERATED_PACKAGE_NAME = "com.example.injectannotation";

    //输出日志信息
    private Messager messager;
    //文件写入
    private Filer filer;
    //获取类型工具类
    private Types types;
    //模块名称
    private String moduleName = null;
    //Element的工具
    private Elements elementsUtls;
    // View所在包名
    private static final String ViewClassName = "android.view.View";
    private static final String ActivityClassName = "android.app.Activity";

    /**
     * 初始化各种变量
     *
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        elementsUtls = processingEnvironment.getElementUtils();

        moduleName = processingEnvironment.getOptions().get("route_module_name");
        types = processingEnvironment.getTypeUtils();
        LoggerInfo("moduleName = " + moduleName);
    }

    /**
     * 设置支持的注解类型
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<String>();
        annotations.add(BindView.class.getCanonicalName());
        return annotations;
    }

    /**
     * 源代码java版本
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 日志输出信息
     *
     * @param msg
     */
    public void LoggerInfo(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, ">> " + msg);
    }

    /**
     * 注解处理的核心方法
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) {
            return false;
        }
        LoggerInfo("process start");
        // 获取所有被 @BindView 注解的对象
        Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        // 把所有被注解的成员变量根据类搜集起来
        Map<TypeElement, Set<Element>> routeMap = new HashMap<>();
        // 存放注解成员变量
        Set<Element> bindViews = null;

        try {
        if (bindViewElements != null && bindViewElements.size() > 0) {
            //遍历每个 @BindView注解的元素
            for (Element element: bindViewElements) {
                //获取注解元素所在的类对象
                TypeElement typeElement = (TypeElement) element.getEnclosingElement();
                Set<Element> elements = routeMap.get(typeElement);
                if (elements != null) {
                    elements.add(element);
                } else {
                    bindViews = new HashSet<>();
                    bindViews.add(element);
                    routeMap.put(typeElement, bindViews);
                }
            }

            for (Map.Entry<TypeElement, Set<Element>> entry: routeMap.entrySet()) {
                writeFile(entry.getKey(), entry.getValue());
            }
        }
    } catch (Exception e) {
        LoggerInfo("process: " + e.getMessage());
        e.printStackTrace();
    }
        return true;
    }

    /**
     * 根据注解元素及所在类的信息，生成文件并写入
     *
     * @param typeElement
     * @param routes
     */
    public void writeFile(TypeElement typeElement, Set<Element> routes) {
        //Activity的类型
        TypeMirror activityMirror = elementsUtls.getTypeElement(ActivityClassName).asType();
        TypeMirror viewMirror = elementsUtls.getTypeElement(ViewClassName).asType();

        //获取所在类文件的类型
        TypeMirror targetMirror = elementsUtls.getTypeElement(typeElement.getQualifiedName()).asType();

        //需要的View参数
        ParameterSpec sourceView = ParameterSpec.builder(TypeName.get(viewMirror), "sourceView").build();
        //需要的Target参数
        ParameterSpec target = ParameterSpec.builder(TypeName.get(targetMirror), "target").build();

        LoggerInfo("start inject");

        //是否是Activity
        boolean isActivity = types.isSubtype(typeElement.asType(), activityMirror);

        // 构建构造方法，处理Activity
        MethodSpec injectForActivity = null;
        if (isActivity) {
            injectForActivity = createConstructorForActivity(target);
        }
        // 构建构造方法，处理指定的View
        MethodSpec injectView = createConstructorForView(target, sourceView, routes);
        //创建类
        TypeSpec.Builder bindBuilder = TypeSpec.classBuilder(typeElement.getSimpleName().toString() + "$BindView")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(injectView);
        //target是Activity类型的生成对应的bind方法
        if (isActivity) {
            bindBuilder.addMethod(injectForActivity);
        }
        TypeSpec bindHelper = bindBuilder.build();
        JavaFile javaFile = JavaFile.builder(GENERATED_PACKAGE_NAME, bindHelper).build();

        //写入文件
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            LoggerInfo("writeFile error: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 真正处理逻辑
     *
     * @param target
     * @param sourceView
     * @param routes
     * @return
     */
    private MethodSpec createConstructorForView(ParameterSpec target, ParameterSpec sourceView, Set<Element> routes) {
        MethodSpec.Builder methodBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(target)
                .addParameter(sourceView);
        // 在方法里插入代码
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.addStatement("if(target == null) { return; }");
        codeBlock.addStatement("if(sourceView == null) { return; }");
        methodBuilder.addCode(codeBlock.build());
        for (Element element: routes) {
            //变量名
            String fieldName = element.getSimpleName().toString();
            //变量类型
            String fieldType = element.asType().toString();
            //控件ID
            int resId = element.getAnnotation(BindView.class).value();
            methodBuilder.addStatement("target.$L = ($N)sourceView.findViewById($L)", fieldName, fieldType, resId);
        }
        return methodBuilder.build();
    }

    /**
     * 处理Activity注入，省去传递View的操作
     *
     * @param target
     * @return
     */
    private MethodSpec createConstructorForActivity(ParameterSpec target) {
        MethodSpec.Builder methodBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(target, target.getWindow().getDecorView())")
                .addParameter(target);
        return methodBuilder.build();
    }
}
