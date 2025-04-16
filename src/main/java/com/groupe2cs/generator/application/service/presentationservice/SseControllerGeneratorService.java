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
public class SseControllerGeneratorService {

    private final TemplateEngine templateEngine;
    private final FileWriterService fileWriterService;
    private final GeneratorProperties generatorProperties;
    private final SsePublisherGeneratorService ssePublisherGeneratorService;

    public SseControllerGeneratorService(
            TemplateEngine templateEngine,
            FileWriterService fileWriterService,
            GeneratorProperties generatorProperties,
            SsePublisherGeneratorService ssePublisherGeneratorService
    ) {
        this.templateEngine = templateEngine;
        this.fileWriterService = fileWriterService;
        this.generatorProperties = generatorProperties;
        this.ssePublisherGeneratorService = ssePublisherGeneratorService;
    }

    public void generate(EntityDefinition definition, String baseDir) {
        Map<String, Object> context = new HashMap<>(definition.toMap());

        String outputDir = baseDir + "/" + generatorProperties.getControllerPackage();
        context.put("package", Utils.getPackage(outputDir));
        context.put("nameLowerCase", definition.getName().toLowerCase());

        Set<String> imports = new LinkedHashSet<>();
        imports.add("org.springframework.http.MediaType");
        imports.add("org.springframework.web.bind.annotation.*");
        imports.add("reactor.core.publisher.Flux");
        imports.add("java.time.Duration");
        imports.add("org.springframework.http.codec.ServerSentEvent");
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getDtoPackage()) + ".*");
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getMapperPackage()) + ".*");
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getRepositoryPackage()) + ".*");
        imports.add(Utils.getPackage(baseDir + "/" + generatorProperties.getPresentationPackage())+"." + definition.getName()+"Publisher");
        context.put("imports", imports);

        String content = templateEngine.render("presentation/sseController.mustache", context);
        fileWriterService.write(outputDir, definition.getName() + "SseController.java", content);

        //publisher
        this.ssePublisherGeneratorService.generate(definition, baseDir);
    }
}
