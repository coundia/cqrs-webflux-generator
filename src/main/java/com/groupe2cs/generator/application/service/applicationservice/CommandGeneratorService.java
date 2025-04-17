package com.groupe2cs.generator.application.service.applicationservice;

import com.groupe2cs.generator.domain.engine.FieldTransformer;
import com.groupe2cs.generator.domain.engine.FileWriterService;
import com.groupe2cs.generator.domain.engine.TemplateEngine;
import com.groupe2cs.generator.infrastructure.config.GeneratorProperties;
import com.groupe2cs.generator.domain.model.EntityDefinition;
import com.groupe2cs.generator.shared.Utils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommandGeneratorService {

    private final TemplateEngine templateEngine;
    private final FileWriterService fileWriterService;
    private final GeneratorProperties generatorProperties;

    public CommandGeneratorService(
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
            generateCommand(type, definition, outputDir);
            generateHandler(type, definition, outputDir);
        }
    }

    private void generateCommand(String prefix, EntityDefinition definition, String baseDir) {
        Map<String, Object> context = new HashMap<>(definition.toMap());

        String commandDir = baseDir + "/" + generatorProperties.getCommandPackage();
        context.put("package", Utils.getPackage(commandDir));
        context.put("commandType", prefix);
        context.put("entity", definition.getName());
        context.put("fields", FieldTransformer.transform(definition.getFields(), definition.getName()));
        context.put("isDelete", prefix.equalsIgnoreCase("Delete"));
        context.put("name", prefix + definition.getName());

        Set<String> imports = new LinkedHashSet<>();
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getVoPackage()) + ".*");
        context.put("imports", imports);

        String content = templateEngine.render("application/command.mustache", context);
        fileWriterService.write(commandDir, prefix + definition.getName() + "Command.java", content);
    }

    private void generateHandler(String prefix, EntityDefinition definition, String baseDir) {
        Map<String, Object> context = new HashMap<>(definition.toMap());

        String handlerDir = baseDir + "/" + generatorProperties.getCommandHandlerPackage();
        context.put("package", Utils.getPackage(handlerDir));
        context.put("entity", definition.getName());
        context.put("name", prefix + definition.getName());
        context.put("fields", FieldTransformer.transform(definition.getFields(), definition.getName()));
        context.put("isDelete", prefix.equalsIgnoreCase("Delete"));
        context.put("isUpdate", prefix.equalsIgnoreCase("Update"));
        context.put("isCreate", prefix.equalsIgnoreCase("Create"));

        Set<String> imports = new LinkedHashSet<>();
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getCommandPackage()) + "." + prefix + definition.getName() + "Command");
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getRepositoryPackage()) + "." + definition.getName() + "Repository");
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getEntityPackage()) + "." + definition.getName());
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getSsePackage()) + "." + definition.getName()+"Publisher");
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getMapperPackage()) + "." + definition.getName() + "Mapper");
        context.put("imports", imports);

        String content = templateEngine.render("application/commandHandler.mustache", context);
        fileWriterService.write(handlerDir, prefix + definition.getName() + "CommandHandler.java", content);
    }
}
