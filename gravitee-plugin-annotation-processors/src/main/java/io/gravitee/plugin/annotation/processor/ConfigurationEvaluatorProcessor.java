/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.plugin.annotation.processor;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.auto.service.AutoService;
import io.gravitee.plugin.annotation.ConfigurationEvaluator;
import io.gravitee.secrets.api.annotation.Secret;
import io.gravitee.secrets.api.el.FieldKind;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import lombok.Getter;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@SupportedAnnotationTypes({ "io.gravitee.plugin.annotation.ConfigurationEvaluator", "io.gravitee.secrets.api.annotation.Secret" })
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class ConfigurationEvaluatorProcessor extends AbstractProcessor {

    public static final String EVALUATED_CONFIGURATION_NAME = "evaluatedConfigurationName";
    private Messager messager;

    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<Element, FieldKind> secretFields = new HashMap<>();

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Secret.class)) {
            if (annotatedElement.getKind() == ElementKind.FIELD) {
                FieldKind kind = annotatedElement.getAnnotation(Secret.class).value();
                secretFields.put(annotatedElement, kind);
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "@Secret should be use on field", annotatedElement);
                return false;
            }
        }

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(ConfigurationEvaluator.class)) {
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                String attributePrefix = annotatedElement.getAnnotation(ConfigurationEvaluator.class).attributePrefix();

                if (attributePrefix == null || attributePrefix.isEmpty()) {
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "@ConfigurationEvaluator attributePrefix property must not be empty",
                        annotatedElement
                    );
                    return false;
                }

                String className = ((TypeElement) annotatedElement).getQualifiedName().toString();

                try {
                    writeEvaluatorFileFromTemplate(className, (TypeElement) annotatedElement, attributePrefix, secretFields);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "@ConfigurationEvaluator should be use on class", annotatedElement);
                return false;
            }
        }

        return true;
    }

    private void writeEvaluatorFileFromTemplate(
        final String className,
        final TypeElement currentElement,
        final String attributePrefix,
        final Map<Element, FieldKind> secretFields
    ) throws IOException {
        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String evaluatorClassName = className + "Evaluator";
        String evaluatorSimpleClassName = evaluatorClassName.substring(lastDot + 1);
        String evaluatedConfigurationName = toCamelCase(simpleClassName);

        JavaFileObject evaluatorFile = processingEnv.getFiler().createSourceFile(evaluatorClassName);

        try (PrintWriter out = new PrintWriter(evaluatorFile.openWriter())) {
            HashMap<String, Object> scopes = new HashMap<>();
            scopes.put("packageName", packageName);
            scopes.put("simpleClassName", simpleClassName);
            scopes.put("evaluatorClassName", evaluatorClassName);
            scopes.put("evaluatorSimpleClassName", evaluatorSimpleClassName);
            scopes.put(EVALUATED_CONFIGURATION_NAME, evaluatedConfigurationName);
            scopes.put("attributePrefix", attributePrefix);

            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mHeader = mf.compile("templates/evaluatorHeader.mustache");
            Mustache mFooter = mf.compile("templates/evaluatorFooter.mustache");
            Mustache mClass = mf.compile("templates/evalClass.mustache");
            Mustache mField = mf.compile("templates/evalField.mustache");
            Mustache mClose = mf.compile("templates/evalClose.mustache");

            //First header
            mHeader.execute(out, scopes);

            //Then eval method
            generateEvalMethods(
                currentElement,
                new MustacheParams(mClass, mField, mClose),
                out,
                "evaluatedConfiguration",
                "configuration",
                "",
                secretFields
            );

            //Then footer
            mFooter.execute(out, scopes);

            out.flush();
        }
    }

    private void generateEvalMethods(
        TypeElement currentElement,
        MustacheParams mustacheParams,
        Writer writer,
        String evaluatedConfigurationName,
        String originalConfigurationName,
        String currentAttributeSuffix,
        Map<Element, FieldKind> secretFields
    ) {
        // 3 things to manage : fields, inner class and object
        // We need to exclude the "builder" part if present
        List<VariableElement> fields = elementUtils
            .getAllMembers(currentElement)
            .stream()
            .filter(element ->
                element.getKind() == ElementKind.FIELD && !element.getSimpleName().toString().contains("Builder") && !isConstant(element)
            )
            .map(VariableElement.class::cast)
            .toList();

        Map<Boolean, List<FieldProperty>> convertedFields = fields
            .stream()
            .map(field -> new FieldProperty(field, elementUtils, evaluatedConfigurationName, originalConfigurationName, secretFields))
            .collect(Collectors.partitioningBy(fieldProperty -> "Object".equals(fieldProperty.getFieldType())));

        List<TypeElement> classes = elementUtils
            .getAllMembers(currentElement)
            .stream()
            .filter(element -> element.getKind() == ElementKind.CLASS && !element.getSimpleName().toString().contains("Builder"))
            .map(TypeElement.class::cast)
            .toList();

        // Process fields
        convertedFields.get(false).forEach(field -> mustacheParams.mField().execute(writer, field));

        // Process classes
        classes.forEach(classElement -> {
            String className = classElement.getSimpleName().toString();
            String classVariable = toCamelCase(className);
            String classGetter = getGetterMethod(className);
            String attributeSuffix = currentAttributeSuffix + "." + classVariable;
            String evaluatedConf = evaluatedConfigurationName + "." + classGetter;
            String originalConf = originalConfigurationName + "." + classGetter;

            Map<String, Object> scopes = new HashMap<>();
            scopes.put("className", className);
            scopes.put("attributeSuffix", attributeSuffix);
            scopes.put(EVALUATED_CONFIGURATION_NAME, evaluatedConf);
            mustacheParams.mClass().execute(writer, scopes);

            generateEvalMethods(
                classElement,
                new MustacheParams(mustacheParams),
                writer,
                evaluatedConf,
                originalConf,
                attributeSuffix,
                secretFields
            );

            mustacheParams.mClose().execute(writer, scopes);
        });

        // Process objects
        List<FieldProperty> objects = convertedFields
            .get(true)
            .stream()
            .filter(fieldProperty ->
                classes.stream().noneMatch(cl -> cl.getSimpleName().toString().equalsIgnoreCase(fieldProperty.getFieldName()))
            )
            .toList();

        objects.forEach(objectElement -> {
            String objectName = objectElement.fieldName;
            String objectGetter = getGetterMethod(objectName);
            String attributeSuffix = currentAttributeSuffix + "." + objectName;
            String evaluatedConf = evaluatedConfigurationName + "." + objectGetter;
            String originalConf = originalConfigurationName + "." + objectGetter;

            Map<String, Object> scopes = new HashMap<>();
            scopes.put("className", objectName);
            scopes.put("attributeSuffix", attributeSuffix);
            scopes.put(EVALUATED_CONFIGURATION_NAME, evaluatedConf);
            mustacheParams.mClass().execute(writer, scopes);

            TypeElement element = elementUtils.getTypeElement(((DeclaredType) objectElement.getField().asType()).asElement().toString());

            //Check if element is not null and throw an exception with debug info
            if (element == null) {
                throw new IllegalArgumentException(
                    "Element is null for " +
                    objectElement.getFieldName() +
                    " and type " +
                    objectElement.getField().asType().toString() +
                    " and asElement " +
                    ((DeclaredType) objectElement.getField().asType()).asElement().toString()
                );
            }

            generateEvalMethods(
                element,
                new MustacheParams(mustacheParams),
                writer,
                evaluatedConf,
                originalConf,
                attributeSuffix,
                secretFields
            );

            mustacheParams.mClose().execute(writer, scopes);
        });
    }

    private boolean isConstant(Element element) {
        return element.getModifiers().containsAll(Set.of(Modifier.STATIC, Modifier.FINAL));
    }

    private String getGetterMethod(final String field) {
        String startLetter = field.substring(0, 1).toUpperCase();
        return "get" + startLetter + field.substring(1) + "()";
    }

    private String toCamelCase(final String className) {
        String startLetter = className.substring(0, 1).toLowerCase();
        return startLetter + className.substring(1);
    }

    @Getter
    public static class FieldProperty {

        private final VariableElement field;
        private final Elements elementUtils;
        private final String fieldName;
        private final String fieldGetter;
        private final String fieldSetter;
        private final String fieldType;
        private final String fieldClass;
        private final String secretKind;

        private final boolean toEval;
        private final String evaluatedConfigurationName;
        private final String originalConfigurationName;

        public FieldProperty(
            VariableElement field,
            Elements elementUtils,
            String evaluatedConfigurationName,
            String originalConfigurationName,
            Map<Element, FieldKind> secretFields
        ) {
            this.field = field;
            this.elementUtils = elementUtils;
            this.fieldType = getFieldType(field);
            this.fieldName = field.getSimpleName().toString();
            this.fieldGetter = getGetterMethod(fieldName, fieldType);
            this.fieldSetter = getSetterMethod(fieldName);
            this.secretKind = secretFields.get(field) != null ? secretFields.get(field).name() : "";
            // Check if the type of this field is String to know if it's need to be evaluated by the template engine
            this.toEval = "String".equals(fieldType);
            this.evaluatedConfigurationName = evaluatedConfigurationName;
            this.originalConfigurationName = originalConfigurationName;
            this.fieldClass = "Enum".equals(fieldType) ? field.asType().toString() : "";
        }

        private String getGetterMethod(String field, String fieldType) {
            String startLetter = field.substring(0, 1).toUpperCase();
            String prefix = "get";
            if ("Boolean".equals(fieldType)) {
                prefix = "is";
            }
            return prefix + startLetter + field.substring(1);
        }

        private String getSetterMethod(String field) {
            String startLetter = field.substring(0, 1).toUpperCase();
            return "set" + startLetter + field.substring(1);
        }

        /**
         * Compare a field to various types to find it
         * @param field the field to test
         * @return a String representing the type found (if no type matches, the default type is Object)
         */
        private String getFieldType(VariableElement field) {
            if (is(field.asType(), String.class)) {
                return "String";
            } else if (is(field.asType(), Boolean.class) || is(field.asType(), boolean.class)) {
                return "Boolean";
            } else if (is(field.asType(), Set.class)) {
                return "Set";
            } else if (is(field.asType(), List.class)) {
                return "List";
            } else if (is(field.asType(), Integer.class) || is(field.asType(), int.class)) {
                return "Integer";
            } else if (is(field.asType(), Long.class) || is(field.asType(), long.class)) {
                return "Long";
            } else if (is(field.asType(), Double.class) || is(field.asType(), double.class)) {
                return "Double";
            } else if (is(field.asType(), Short.class) || is(field.asType(), short.class)) {
                return "Short";
            } else if (isEnum(field.asType())) {
                return "Enum";
            }
            return "Object";
        }

        /**
         * Check if the given type is an Enum
         * @param type  the type to test
         * @return true if type is an Enum
         */
        private static boolean isEnum(TypeMirror type) {
            if (!(type instanceof DeclaredType)) {
                return false;
            }

            return ((DeclaredType) type).asElement().getKind() == ElementKind.ENUM;
        }

        /**
         * Compare the type with the expected class
         * @param type      the type to test
         * @param expected  the expected type
         * @return true if the type matches the expected one
         */
        private static boolean is(TypeMirror type, Class<?> expected) {
            if (type instanceof PrimitiveType) {
                return type.getKind() == kind(expected);
            }

            if (!(type instanceof DeclaredType)) {
                return false;
            }

            Element element = ((DeclaredType) type).asElement();
            return ((TypeElement) element).getQualifiedName().contentEquals(expected.getName());
        }

        public static TypeKind kind(Class<?> type) {
            return switch (type.getName()) {
                case "boolean" -> TypeKind.BOOLEAN;
                case "byte" -> TypeKind.BYTE;
                case "short" -> TypeKind.SHORT;
                case "int" -> TypeKind.INT;
                case "long" -> TypeKind.LONG;
                case "float" -> TypeKind.FLOAT;
                case "double" -> TypeKind.DOUBLE;
                case "char" -> TypeKind.CHAR;
                case "void" -> TypeKind.VOID;
                default -> TypeKind.DECLARED;
            };
        }
    }

    private record MustacheParams(Mustache mClass, Mustache mField, Mustache mClose) {
        public MustacheParams(MustacheParams copy) {
            this(copy.mClass, copy.mField, copy.mClose);
        }
    }
}
