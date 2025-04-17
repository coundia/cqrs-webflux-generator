package com.groupe2cs.generator.application.service.testservice;

import com.groupe2cs.generator.domain.engine.FieldTransformer;
import com.groupe2cs.generator.domain.engine.FileWriterService;
import com.groupe2cs.generator.domain.engine.TemplateEngine;
import com.groupe2cs.generator.domain.model.EntityDefinition;
import com.groupe2cs.generator.infrastructure.config.GeneratorProperties;
import com.groupe2cs.generator.shared.Utils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommandApplicationTestGeneratorService {

    private final TemplateEngine templateEngine;
    private final FileWriterService fileWriterService;
    private final GeneratorProperties generatorProperties;

    public CommandApplicationTestGeneratorService(
            TemplateEngine templateEngine,
            FileWriterService fileWriterService,
            GeneratorProperties generatorProperties
    ) {
        this.templateEngine = templateEngine;
        this.fileWriterService = fileWriterService;
        this.generatorProperties = generatorProperties;
    }

    public void generate(EntityDefinition definition, String outputDir) {
        List<String> commandTypes = List.of("Create", "Update", "Delete");

        for (String type : commandTypes) {
            generateCommandHandlerTest(type, definition, outputDir);
        }
    }

    private void generateCommandHandlerTest(String prefix, EntityDefinition definition, String baseDir) {
        Map<String, Object> context = new HashMap<>(definition.toMap());

        String fullPath = baseDir + "/" + generatorProperties.getApplicationPackage();
        String packageName = Utils.getTestPackage(fullPath);
        String outputDir = Utils.getTestDir(fullPath);

        context.put("package", Utils.getPackage(packageName));
        context.put("entity", definition.getName());
        context.put("name", prefix + definition.getName());
        context.put("commandName", prefix + definition.getName() + "Command");
        context.put("eventName", definition.getName() + "CreatedEvent");
        context.put("className", prefix + definition.getName() + "CommandHandlerTest");
        context.put("fields", FieldTransformer.transform(definition.getFields(), definition.getName()));
        context.put("isDelete", prefix.equalsIgnoreCase("Delete"));
        context.put("isUpdate", prefix.equalsIgnoreCase("Update"));
        context.put("isCreate", prefix.equalsIgnoreCase("Create"));

        Set<String> imports = new LinkedHashSet<>();
        imports.add("reactor.core.publisher.Mono");
        imports.add("org.junit.jupiter.api.Test");
        imports.add("org.mockito.Mockito");
        imports.add( Utils.getTestPackage(baseDir + "/" + generatorProperties.getCommandPackage()) + ".*");
        imports.add( Utils.getTestPackage(baseDir + "/" + generatorProperties.getCommandHandlerPackage()) + ".*");
        imports.add( Utils.getTestPackage(baseDir + "/" + generatorProperties.getRepositoryPackage()) + ".*");
        imports.add( Utils.getTestPackage(baseDir + "/" + generatorProperties.getVoPackage()) + ".*");
        imports.add( Utils.getTestPackage(baseDir + "/" + generatorProperties.getSsePackage()) + ".*");
        imports.add( Utils.getTestPackage(baseDir + "/" + generatorProperties.getEntityPackage()) + ".*");
        imports.add( Utils.getTestPackage(baseDir + "/" + generatorProperties.getSharedPackage()) + ".*");
        imports.add("java.util.UUID");
        context.put("imports", imports);

        String content = templateEngine.render("tests/CommandApplicationTests.mustache", context);
        fileWriterService.write(outputDir, context.get("className") + ".java", content);
    }
}
