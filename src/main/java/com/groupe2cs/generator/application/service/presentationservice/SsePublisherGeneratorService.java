package com.groupe2cs.generator.application.service.presentationservice;

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
public class SsePublisherGeneratorService {

    private final TemplateEngine templateEngine;
    private final FileWriterService fileWriterService;
    private final GeneratorProperties generatorProperties;

    public SsePublisherGeneratorService(
            TemplateEngine templateEngine,
            FileWriterService fileWriterService,
            GeneratorProperties generatorProperties
    ) {
        this.templateEngine = templateEngine;
        this.fileWriterService = fileWriterService;
        this.generatorProperties = generatorProperties;
    }

    public void generate(EntityDefinition definition, String baseDir) {
        Map<String, Object> context = new HashMap<>(definition.toMap());

        String outputDir = baseDir + "/" + generatorProperties.getSsePackage();
        context.put("package", Utils.getPackage(outputDir));
        context.put("name", definition.getName());
        context.put("nameLowerCase", definition.getName().toLowerCase());

        Set<String> imports = new LinkedHashSet<>();
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getDtoPackage()+".*"));
        context.put("imports", imports);

        String content = templateEngine.render("presentation/publisher.mustache", context);
        fileWriterService.write(outputDir, definition.getName() + "Publisher.java", content);
    }
}
