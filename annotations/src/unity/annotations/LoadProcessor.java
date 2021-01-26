package unity.annotations;

import arc.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import unity.annotations.Annotations.*;

import java.io.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;

import com.squareup.javapoet.*;

@SupportedAnnotationTypes({
    "unity.annotations.Annotations.Load",
    "unity.annotations.Annotations.LoadRegs"
})
public class LoadProcessor extends BaseProcessor{
    Seq<VariableElement> toLoad = new Seq<>();
    ObjectSet<String> genericRegs = new ObjectSet<>();

    {
        rounds = 2;
    }

    @Override
    public void process(RoundEnvironment roundEnv) throws Exception{
        if(round == 1){
            for(Element e : roundEnv.getElementsAnnotatedWith(LoadRegs.class)){
                LoadRegs regs = e.getAnnotation(LoadRegs.class);

                genericRegs.addAll(regs.value());
                if(regs.outline()){
                    genericRegs.addAll(Seq.with(regs.value()).map(n -> n + "-outline"));
                }
            }

            processGenerics();
        }else{

        }
    }

    void processGenerics() throws IOException{
        TypeSpec.Builder spec = TypeSpec.classBuilder("Regions").addModifiers(Modifier.PUBLIC)
            .addJavadoc("Generic texture regions");

        MethodSpec.Builder load = MethodSpec.methodBuilder("load").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("Loads the texture regions");

        for(String reg : genericRegs){
            String name = Strings.kebabToCamel(reg) + "Region";
            FieldSpec.Builder var = FieldSpec.builder(
                cName(TextureRegion.class),
                name,
                Modifier.PUBLIC, Modifier.STATIC
            );

            spec.addField(var.build());
            load.addStatement("$L = $T.atlas.find($S)", name, cName(Core.class), "unity-" + reg);
        }

        spec.addMethod(load.build());

        write(spec.build());
    }
}