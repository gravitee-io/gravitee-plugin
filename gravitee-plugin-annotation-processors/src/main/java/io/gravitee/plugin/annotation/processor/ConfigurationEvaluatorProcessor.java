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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.auto.service.AutoService;
import io.gravitee.plugin.annotation.ConfigurationEvaluator;
import io.gravitee.secrets.api.annotation.Secret;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@SupportedAnnotationTypes(
    {
        "io.gravitee.plugin.annotation.ConfigurationEvaluator",
        "io.gravitee.secrets.api.annotation.Secret",
        "com.fasterxml.jackson.annotation.JsonTypeInfo",
    }
)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class ConfigurationEvaluatorProcessor extends AbstractProcessor {

    public static final String EVALUATED_CONFIGURATION_NAME = "evaluatedConfigurationName";
    private Messager messager;

    private Elements elementUtils;

    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<Element, JsonTypeElement> jsonTypeClass = new HashMap<>();

        List<String> dependencyClasses = new ArrayList<>();
        dependencyClasses.add("io.gravitee.definition.model.v4.ssl.TrustStore");
        dependencyClasses.add("io.gravitee.definition.model.v4.ssl.KeyStore");
        dependencyClasses.add("io.gravitee.plugin.configurations.ssl.TrustStore");
        dependencyClasses.add("io.gravitee.plugin.configurations.ssl.KeyStore");

        //Check external dependencies for JsonTypeInfo annotation
        for (String dependencyClass : dependencyClasses) {
            TypeElement dependencyClassElement = elementUtils.getTypeElement(dependencyClass);
            if (dependencyClassElement != null && dependencyClassElement.getAnnotation(JsonTypeInfo.class) != null) {
                jsonTypeClass.putAll(jsonAnnotationProcess(dependencyClassElement));
            }
        }

        //Check current sources for JsonTypeInfo
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(JsonTypeInfo.class)) {
            jsonTypeClass.putAll(jsonAnnotationProcess(annotatedElement));
        }

        //Check current sources for ConfigurationEvaluator
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(ConfigurationEvaluator.class)) {
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                String attributePrefix = annotatedElement.getAnnotation(ConfigurationEvaluator.class).attributePrefix();

                if (!attributePrefix.isEmpty() && !attributePrefix.startsWith("gravitee.attributes.")) {
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "@ConfigurationEvaluator attributePrefix property must be formed as follow: " +
                        "gravitee.attributes.[type].[id] e.g. gravitee.attributes.endpoint.kafka",
                        annotatedElement
                    );
                    return false;
                }

                String className = ((TypeElement) annotatedElement).getQualifiedName().toString();

                try {
                    writeEvaluatorFileFromTemplate(className, (TypeElement) annotatedElement, attributePrefix, jsonTypeClass);
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

    private Map<Element, JsonTypeElement> jsonAnnotationProcess(Element annotatedElement) {
        Map<Element, JsonTypeElement> jsonTypeClass = new HashMap<>();
        if (annotatedElement.getKind() == ElementKind.CLASS) {
            String property = annotatedElement.getAnnotation(JsonTypeInfo.class).property();
            TypeMirror typeMirror = null;
            //As Class object cannot be resolved during compile-time context, we need to catch the MirrorTypeException to access the TypeMirror
            try {
                Class<?> defaultImpl = annotatedElement.getAnnotation(JsonTypeInfo.class).defaultImpl();
            } catch (MirroredTypeException mte) {
                typeMirror = mte.getTypeMirror();
            }

            Optional<? extends Element> typeElem = annotatedElement
                .getEnclosedElements()
                .stream()
                .filter(f -> f.getKind() == ElementKind.FIELD && f.getSimpleName().contentEquals(property))
                .findFirst();

            Map<String, String> enumValues = new HashMap<>();

            try {
                if (typeElem.isPresent()) {
                    //Check if there is a field in the enum
                    Optional<? extends Element> valueField = typeUtils
                        .asElement(typeElem.get().asType())
                        .getEnclosedElements()
                        .stream()
                        .filter(f -> f.getKind() == ElementKind.FIELD)
                        .findFirst();

                    if (valueField.isPresent()) {
                        // Get the fully qualified name of the class
                        Name qualifiedName = ((TypeElement) typeUtils.asElement(typeElem.get().asType())).getQualifiedName();

                        // Try to get the corresponding file object
                        try {
                            FileObject fileObject = processingEnv
                                .getFiler()
                                .getResource(StandardLocation.SOURCE_PATH, "", qualifiedName.toString().replace('.', '/') + ".java");

                            CompilationUnit cu = StaticJavaParser.parse(fileObject.getCharContent(true).toString());

                            // Find the enum
                            Optional<EnumDeclaration> enumOpt = cu.findFirst(
                                EnumDeclaration.class,
                                e -> e.getNameAsString().equals(typeUtils.asElement(typeElem.get().asType()).getSimpleName().toString())
                            );
                            if (enumOpt.isPresent()) {
                                EnumDeclaration myEnum = enumOpt.get();

                                // Loop through enum constants
                                for (EnumConstantDeclaration constant : myEnum.getEntries()) {
                                    if (!constant.getArguments().isEmpty()) {
                                        var enumValue = constant.getArguments().get(0).toString().replaceAll("\"", "");
                                        if (!constant.getName().toString().equals(enumValue)) {
                                            enumValues.put(enumValue, constant.getName().toString());
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Error processing enum: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Error processing enum: " + e.getMessage());
            }

            JsonSubTypes.Type[] types = annotatedElement.getAnnotation(JsonSubTypes.class).value();

            List<JsonSubTypeElement> jsonSubTypeElements = Stream
                .of(types)
                .map(type -> {
                    List<String> names = new ArrayList<>();
                    if (!type.name().isEmpty()) {
                        names.add(type.name());
                    }
                    names.addAll(Arrays.stream(type.names()).toList());

                    List<String> enumValue = names.stream().filter(enumValues::containsKey).map(enumValues::get).toList();
                    names.addAll(enumValue);

                    TypeMirror subTypeMirror = null;
                    try {
                        var clazz = type.value();
                    } catch (MirroredTypeException mte) {
                        subTypeMirror = mte.getTypeMirror();
                    }

                    return new JsonSubTypeElement(names, subTypeMirror);
                })
                .toList();

            JsonTypeElement jsonTypeElement = new JsonTypeElement(property, typeMirror, jsonSubTypeElements);
            jsonTypeClass.put(annotatedElement, jsonTypeElement);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "@JsonTypeInfo in only supported at class level", annotatedElement);
        }
        return jsonTypeClass;
    }

    private void writeEvaluatorFileFromTemplate(
        final String className,
        final TypeElement currentElement,
        final String attributePrefix,
        final Map<Element, JsonTypeElement> jsonTypeClass
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
            Mustache mSubTypeHeader = mf.compile("templates/evalSubTypeHeader.mustache");
            Mustache mSubTypeFooter = mf.compile("templates/evalSubTypeFooter.mustache");
            Mustache mSubTypeCaseHeader = mf.compile("templates/evalSubTypeCaseHeader.mustache");
            Mustache mSubTypeCaseFooter = mf.compile("templates/evalSubTypeCaseFooter.mustache");

            //First header
            mHeader.execute(out, scopes);

            //Then eval method
            generateEvalMethods(
                currentElement,
                new MustacheParams(mClass, mField, mClose, mSubTypeHeader, mSubTypeFooter, mSubTypeCaseHeader, mSubTypeCaseFooter),
                out,
                "evaluatedConfiguration",
                "configuration",
                "",
                jsonTypeClass,
                Collections.emptyList()
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
        Map<Element, JsonTypeElement> jsonTypeClass,
        List<String> excludedFields
    ) {
        // 3 things to manage : fields, inner class and object
        // We need to exclude the "builder" part if present
        List<VariableElement> fields = elementUtils
            .getAllMembers(currentElement)
            .stream()
            .filter(element ->
                element.getKind() == ElementKind.FIELD &&
                !element.getSimpleName().toString().contains("Builder") &&
                !isConstant(element) &&
                !excludedFields.contains(element.getSimpleName().toString())
            )
            .map(VariableElement.class::cast)
            .toList();

        Map<Boolean, List<FieldProperty>> convertedFields = fields
            .stream()
            .map(field ->
                new FieldProperty(field, elementUtils, typeUtils, evaluatedConfigurationName, originalConfigurationName, jsonTypeClass)
            )
            .collect(Collectors.partitioningBy(fieldProperty -> "Object".equals(fieldProperty.getFieldType())));

        List<TypeElement> classes = elementUtils
            .getAllMembers(currentElement)
            .stream()
            .filter(element -> element.getKind() == ElementKind.CLASS && !element.getSimpleName().toString().contains("Builder"))
            .map(TypeElement.class::cast)
            .toList();

        // Process fields (2 cases: classic field and field member of a JsonType object)
        convertedFields
            .get(false)
            .forEach(field -> {
                if (field.isJsonType()) {
                    //Build the object that contains necessary data for mustache template
                    var jsonObjectTemplate = buildJsonObjectTemplate(field, jsonTypeClass, originalConfigurationName);
                    //Execute mustache specific template
                    mustacheParams.mSubTypeHeader().execute(writer, jsonObjectTemplate);

                    jsonObjectTemplate
                        .jsonSubTypeElementWithEnums()
                        .forEach(jsonSubTypeElementWithEnum -> {
                            if (!jsonSubTypeElementWithEnum.value().toString().equals(currentElement.asType().toString())) {
                                Object[] objects = { jsonObjectTemplate, jsonSubTypeElementWithEnum };

                                mustacheParams.mSubTypeCaseHeader().execute(writer, objects);

                                //Change originalConfigurationName and evaluatedConfigurationName to include cast
                                // configuration.getSslOptions().getTrustStore() -> ((PEMTrustStore)configuration.getSslOptions().getTrustStore())
                                var origConfig =
                                    "((" + jsonSubTypeElementWithEnum.value().toString() + ")" + originalConfigurationName + ")";
                                var evalConfig =
                                    "((" + jsonSubTypeElementWithEnum.value().toString() + ")" + evaluatedConfigurationName + ")";

                                generateEvalMethods(
                                    jsonSubTypeElementWithEnum.classElement(),
                                    new MustacheParams(mustacheParams),
                                    writer,
                                    evalConfig,
                                    origConfig,
                                    currentAttributeSuffix,
                                    jsonTypeClass,
                                    List.of(jsonTypeClass.get(currentElement).property())
                                );

                                mustacheParams.mSubTypeCaseFooter().execute(writer, jsonSubTypeElementWithEnum);
                            }
                        });

                    mustacheParams.mSubTypeFooter().execute(writer, jsonObjectTemplate);
                } else {
                    mustacheParams.mField().execute(writer, field);
                }
            });

        // Process classes
        classes.forEach(classElement -> {
            Optional<FieldProperty> fieldProperty = convertedFields
                .get(true)
                .stream()
                .filter(currentFieldProperty -> {
                    var element = typeUtils.asElement(currentFieldProperty.getField().asType());
                    if (element != null) {
                        return classElement.toString().equals(element.toString());
                    }
                    return classElement.toString().equals(currentFieldProperty.getField().asType().toString());
                })
                .findFirst();

            String className = classElement.getSimpleName().toString();
            if (fieldProperty.isPresent()) {
                className = fieldProperty.get().fieldName;
            }

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
                jsonTypeClass,
                Collections.emptyList()
            );

            mustacheParams.mClose().execute(writer, scopes);
        });

        // Process objects
        List<FieldProperty> objects = convertedFields
            .get(true)
            .stream()
            .filter(fieldProperty -> {
                var element = typeUtils.asElement(fieldProperty.getField().asType());
                if (element != null) {
                    return classes.stream().noneMatch(cl -> cl.toString().equals(element.toString()));
                }
                return classes.stream().noneMatch(cl -> cl.toString().equals(fieldProperty.getField().asType().toString()));
            })
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
                jsonTypeClass,
                Collections.emptyList()
            );

            mustacheParams.mClose().execute(writer, scopes);
        });
    }

    private boolean isConstant(Element element) {
        return element.getModifiers().containsAll(Set.of(Modifier.STATIC, Modifier.FINAL));
    }

    private String getGetterMethod(final String field) {
        return getGetterMethod(field, true);
    }

    private String getGetterMethod(final String field, final boolean withParenthesis) {
        String startLetter = field.substring(0, 1).toUpperCase();
        return "get" + startLetter + field.substring(1) + (withParenthesis ? "()" : "");
    }

    private String toCamelCase(final String className) {
        String startLetter = className.substring(0, 1).toLowerCase();
        return startLetter + className.substring(1);
    }

    private JsonObjectTemplate buildJsonObjectTemplate(
        FieldProperty field,
        Map<Element, JsonTypeElement> jsonTypeClass,
        String originalConfigurationName
    ) {
        var jsonType = jsonTypeClass.get(field.field.getEnclosingElement());
        List<JsonSubTypeElementWithEnum> subTypeElementWithEnums = new ArrayList<>();

        // Iterate over the enclosed elements of the enum
        for (Element enclosedElement : ((DeclaredType) field.field.asType()).asElement().getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.ENUM_CONSTANT) {
                // Add the enum constant name to the list
                var enumString = enclosedElement.getSimpleName().toString();

                jsonType
                    .jsonSubTypeElements()
                    .forEach(jsonSubTypeElement -> {
                        if (jsonSubTypeElement.names().contains(enumString)) {
                            TypeElement element = elementUtils.getTypeElement(typeUtils.asElement(jsonSubTypeElement.value()).toString());

                            subTypeElementWithEnums.add(new JsonSubTypeElementWithEnum(jsonSubTypeElement.value(), enumString, element));
                        }
                    });
            }
        }

        //Get setter for json object
        String setter = originalConfigurationName;
        // Replace the last "get" with "set"
        int lastGetIndex = setter.lastIndexOf("get");
        if (lastGetIndex != -1) {
            setter = setter.substring(0, lastGetIndex) + "set" + setter.substring(lastGetIndex + 3);
        }

        // Remove the last "()"
        int lastParenthesesIndex = setter.lastIndexOf("()");
        if (lastParenthesesIndex != -1) {
            setter = setter.substring(0, lastParenthesesIndex) + setter.substring(lastParenthesesIndex + 2);
        }

        return new JsonObjectTemplate(jsonType, subTypeElementWithEnums, field, setter);
    }

    @Getter
    @ToString
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
        private final boolean toEvalList;
        private final boolean toEvalHeaderList;
        private final boolean toEvalMap;
        private final boolean jsonType;
        private final String evaluatedConfigurationName;
        private final String originalConfigurationName;

        public FieldProperty(
            VariableElement field,
            Elements elementUtils,
            Types typeUtils,
            String evaluatedConfigurationName,
            String originalConfigurationName,
            Map<Element, JsonTypeElement> jsonTypeClass
        ) {
            this.field = field;
            this.elementUtils = elementUtils;
            this.fieldType = getFieldType(field, elementUtils, typeUtils);
            this.fieldName = field.getSimpleName().toString();
            this.fieldGetter = getGetterMethod(fieldName, fieldType);
            this.fieldSetter = getSetterMethod(fieldName);
            this.secretKind = getSecretKind(field);
            // Check if the type of this field is supported EL to know if it's need to be evaluated by the template engine
            this.toEval = "String".equals(fieldType);
            this.toEvalList = "ListString".equals(fieldType);
            this.toEvalHeaderList = "ListHeader".equals(fieldType);
            this.toEvalMap = "MapString".equals(fieldType);
            this.jsonType = isJsonTypeInfoProperty(field, jsonTypeClass);
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

        private boolean isJsonTypeInfoProperty(Element element, Map<Element, JsonTypeElement> jsonTypeElementMap) {
            if (jsonTypeElementMap.get(element.getEnclosingElement()) == null) {
                return false;
            }
            JsonTypeElement jsonTypeElement = jsonTypeElementMap.get(element.getEnclosingElement());
            return jsonTypeElement.property().equals(element.getSimpleName().toString());
        }

        private String getSecretKind(Element element) {
            Secret secret = element.getAnnotation(Secret.class);
            if (secret != null && secret.value() != null) {
                return secret.value().name();
            }
            return "";
        }

        /**
         * Compare a field to various types to find it
         * @param field the field to test
         * @return a String representing the type found (if no type matches, the default type is Object)
         */
        private String getFieldType(VariableElement field, Elements elementUtils, Types typeUtils) {
            if (is(field.asType(), String.class)) {
                return "String";
            } else if (is(field.asType(), Boolean.class) || is(field.asType(), boolean.class)) {
                return "Boolean";
            } else if (is(field.asType(), Set.class)) {
                return "Set";
            } else if (isStringList(field.asType(), elementUtils, typeUtils)) {
                return "ListString";
            } else if (isHeaderList(field.asType(), elementUtils, typeUtils)) {
                return "ListHeader";
            } else if (is(field.asType(), List.class)) {
                return "List";
            } else if (isStringMap(field.asType(), elementUtils, typeUtils)) {
                return "MapString";
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

        private static boolean isStringList(TypeMirror type, Elements elementUtils, Types typeUtils) {
            if (!(type instanceof DeclaredType) || ((DeclaredType) type).getTypeArguments().size() != 1) {
                return false;
            }

            TypeMirror firstType = ((DeclaredType) type).getTypeArguments().get(0);
            if (firstType == null) {
                return false;
            }
            var StringElem = elementUtils.getTypeElement("java.lang.String");
            return typeUtils.isSameType(firstType, StringElem.asType());
        }

        private static boolean isHeaderList(TypeMirror type, Elements elementUtils, Types typeUtils) {
            if (!(type instanceof DeclaredType) || ((DeclaredType) type).getTypeArguments().size() != 1) {
                return false;
            }

            TypeMirror firstType = ((DeclaredType) type).getTypeArguments().get(0);
            if (firstType == null) {
                return false;
            }
            var StringElem = elementUtils.getTypeElement("io.gravitee.common.http.HttpHeader");
            return typeUtils.isSameType(firstType, StringElem.asType());
        }

        private static boolean isStringMap(TypeMirror type, Elements elementUtils, Types typeUtils) {
            if (!(type instanceof DeclaredType) || ((DeclaredType) type).getTypeArguments().size() != 2) {
                return false;
            }

            TypeMirror firstType = ((DeclaredType) type).getTypeArguments().get(0);
            if (firstType == null) {
                return false;
            }

            TypeMirror secondType = ((DeclaredType) type).getTypeArguments().get(1);
            if (secondType == null) {
                return false;
            }

            var StringElemType = elementUtils.getTypeElement("java.lang.String").asType();
            return typeUtils.isSameType(firstType, StringElemType) && typeUtils.isSameType(secondType, StringElemType);
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

    private record MustacheParams(
        Mustache mClass,
        Mustache mField,
        Mustache mClose,
        Mustache mSubTypeHeader,
        Mustache mSubTypeFooter,
        Mustache mSubTypeCaseHeader,
        Mustache mSubTypeCaseFooter
    ) {
        public MustacheParams(MustacheParams copy) {
            this(
                copy.mClass,
                copy.mField,
                copy.mClose,
                copy.mSubTypeHeader,
                copy.mSubTypeFooter,
                copy.mSubTypeCaseHeader,
                copy.mSubTypeCaseFooter
            );
        }
    }

    private record JsonTypeElement(String property, TypeMirror defaultImpl, List<JsonSubTypeElement> jsonSubTypeElements) {}

    private record JsonSubTypeElement(List<String> names, TypeMirror value) {}

    private record JsonSubTypeElementWithEnum(TypeMirror value, String enumValue, TypeElement classElement) {}

    private record JsonObjectTemplate(
        JsonTypeElement jsonTypeElement,
        List<JsonSubTypeElementWithEnum> jsonSubTypeElementWithEnums,
        FieldProperty property,
        String setter
    ) {}
}
