package com.example.customannotation;

import com.example.customannotation.annotation.CustomAnnotation;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

//@SupportedAnnotationTypes("com.example.customannotation.annotation.CustomAnnotation")
//@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CustomAnnotationProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(CustomAnnotation.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        StringBuilder builder = new StringBuilder()
                .append("package com.example.customannotation;\n\n")
                .append("public class GeneratedClass {\n\n") //open class
                .append("\tpublic String getMessage(){\n") //open method
                .append("\t\treturn \"");

        // for each javax.lang.model.element.Element annotated with the CustomAnnotation
        for (Element element: roundEnvironment.getElementsAnnotatedWith(CustomAnnotation.class)) {
            String objectType = element.getSimpleName().toString();

            //this is appending to the return statement
            builder.append(objectType).append(" says hello! ");
        }

        builder.append("\";\n")
                .append("\t}\n\n")
                .append("}\n");

        try {
            JavaFileObject source = processingEnv.getFiler().createSourceFile("com.example.customannotation.generated.GeneratedClass");

            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
            e.printStackTrace();
        }

        return true;
    }
}
