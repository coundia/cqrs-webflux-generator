package com.groupe2cs.generator.domain.model;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class EntityDefinition implements Serializable {

    private  String name;
    private  String table;
    private  List<FieldDefinition> fields;

    public EntityDefinition() {
    }

    public String getName() {
        return name;
    }

    public String getTable() {
        return table;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setFields(List<FieldDefinition> fields) {
        this.fields = fields;
    }

    public List<FieldDefinition> getFields() {
        return fields.stream()
                .filter(f -> !"oneToMany".equalsIgnoreCase(f.getRelation()))
                .filter(f -> !f.isFileType())
                .toList();
    }

    public List<FieldDefinition> getFieldsWithoutRelations() {
        return fields.stream()
                .filter(f -> !f.isFileType())
                .filter(f -> !"oneToMany".equalsIgnoreCase(f.getRelation()))
                .toList();
    }

    public List<FieldDefinition> getFieldsWithRelations() {
        return fields.stream()
                .filter(f -> !f.isFileType())
                .toList();
    }

    public List<FieldDefinition> getFieldsWithoutId() {
        return fields.stream()
                .filter(f -> !f.isId())
                .toList();
    }

    public List<FieldDefinition> getAllFields() {
        return fields.stream()
                .toList();
    }

    public List<FieldDefinition> getAllFieldsWithoutOneToMany() {
        return fields.stream()
                .filter(f -> !"oneToMany".equalsIgnoreCase(f.getRelation()))
                .toList();
    }
    public EntityDefinition(String name, List<FieldDefinition> fields, String table) {
        this.name = name;
        this.fields = fields;
        this.table = table;
    }

    public EntityDefinition(String name, List<FieldDefinition> fields) {
        this.name = name;
        this.fields = fields;
    }

    public static EntityDefinition fromClassName(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            String simpleName = clazz.getSimpleName();
            List<FieldDefinition> fields = List.of(clazz.getDeclaredFields()).stream()
                    .map(f -> new FieldDefinition(f.getType().getSimpleName(), f.getName()))
                    .collect(Collectors.toList());
            return new EntityDefinition(simpleName, fields);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Entity class not found: " + className);
        }
    }

    public static EntityDefinition fromSource(String className, String sourceRoot) {
        try {
            CompilationUnit cu = getCompilationUnit(className, sourceRoot);
            ClassOrInterfaceDeclaration clazz = cu.getClassByName(getSimpleName(className))
                    .orElseThrow(() -> new RuntimeException("Class not found in file: " + className));

            List<FieldDefinition> fields = clazz.getFields().stream()
                    .flatMap(f -> f.getVariables().stream())
                    .map(v -> new FieldDefinition(v.getType().asString(), v.getNameAsString()))
                    .collect(Collectors.toList());

            return new EntityDefinition(clazz.getNameAsString(), fields);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse source for class 'sourceRoot': " + className, e);
        }
    }

    public static CompilationUnit getCompilationUnit(String className, String sourceRoot) {
        try {
            Path sourceFile = Paths.get(sourceRoot).resolve(className.replace('.', '/') + ".java");
            if (!Files.exists(sourceFile)) {
                throw new IllegalArgumentException("Class file not found: " + sourceFile);
            }
            return StaticJavaParser.parse(sourceFile);
        } catch (IOException e) {
            throw new RuntimeException("Error reading or parsing source file", e);
        }
    }

    public static EntityDefinition fromClass(Class<?> clazz) {
        List<FieldDefinition> fields = List.of(clazz.getDeclaredFields()).stream()
                .map(f -> new FieldDefinition(f.getType().getSimpleName(), f.getName()))
                .collect(Collectors.toList());
        return new EntityDefinition(clazz.getSimpleName(), fields);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("fields", fields);
        map.put("aggregateIdField", findIdField());
        map.put("aggregateIdType", findIdType());
        map.put("package", "com.pcoundia.app." + name.toLowerCase());
        return map;
    }

    private String findIdField() {
        return fields.stream()
                .filter(f -> f.getName().equalsIgnoreCase("id"))
                .map(FieldDefinition::getName)
                .findFirst()
                .orElse("id");
    }

    private String findIdType() {
        return fields.stream()
                .filter(f -> f.getName().equalsIgnoreCase("id"))
                .map(FieldDefinition::getType)
                .findFirst()
                .orElse("String");
    }

    private static String getSimpleName(String className) {
        int lastDot = className.lastIndexOf(".");
        return lastDot != -1 ? className.substring(lastDot + 1) : className;
    }

    public   String  getIdentifier(){
        return "id";
    }

    public List<FieldDefinition> getFieldFiles() {
        return fields.stream()
                .filter(f -> f.isFileType())
                .toList();
    }
    public List<FieldDefinition> searchFields() {
        return getFieldsWithoutRelations();
    }

    public boolean hasRelation(String relationType) {
        return fields.stream()
                .anyMatch(f -> relationType.equalsIgnoreCase(f.getRelation()));
    }


}
