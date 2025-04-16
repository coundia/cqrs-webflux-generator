package com.groupe2cs.generator.application.service.presentationservice;

import com.groupe2cs.generator.domain.engine.FieldTransformer;
import com.groupe2cs.generator.domain.engine.FileWriterService;
import com.groupe2cs.generator.domain.engine.TemplateEngine;
import com.groupe2cs.generator.infrastructure.config.GeneratorProperties;
import com.groupe2cs.generator.domain.model.EntityDefinition;
import com.groupe2cs.generator.shared.Utils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class FindByFieldControllerGeneratorService {

    private final TemplateEngine templateEngine;
    private final FileWriterService fileWriterService;
    private final GeneratorProperties generatorProperties;

    public FindByFieldControllerGeneratorService(TemplateEngine templateEngine, FileWriterService fileWriterService, GeneratorProperties generatorProperties) {
        this.templateEngine = templateEngine;
        this.fileWriterService = fileWriterService;
        this.generatorProperties = generatorProperties;
    }

    public void generate(EntityDefinition definition, String baseDir) {
        Map<String, Object> context = new HashMap<>(definition.toMap());

        String outputDir = baseDir + "/" + generatorProperties.getControllerPackage();
        context.put("package", Utils.getPackage(outputDir));
        context.put("nameLowerCase", definition.getName().toLowerCase());
        context.put("queryPackage", Utils.getPackage(baseDir + "/" + generatorProperties.getQueryPackage()));
        context.put("dtoPackage", Utils.getPackage(baseDir + "/" + generatorProperties.getDtoPackage()));

        var fields = definition.getFields();
        context.put("fields", FieldTransformer.transform(fields, definition.getName()));

        fields = fields.stream().filter(
                p -> p.isFilable()
        ).toList();

        for (var field : fields) {
            Map<String, Object> fieldContext = new HashMap<>(context);
            field.setNameCapitalized(capitalize(field.getName()));
            fieldContext.put("field", field);
            String className = "FindBy" + capitalize(field.getName()) + definition.getName() + "Controller";
            fieldContext.put("className", className);

            Set<String> imports = new LinkedHashSet<>();
            imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getVoPackage()) + ".*");
            imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getQueryHandlerPackage()) + ".*");
            imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getQueryPackage()) + ".*");
            imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getDtoPackage()) + ".*");
            fieldContext.put("imports", imports);

            String content = templateEngine.render("presentation/findByFieldController.mustache", fieldContext);
            fileWriterService.write(outputDir, className+".java", content);
        }
    }

    private String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
