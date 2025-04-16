package com.groupe2cs.generator.application.service.presentationservice;

import com.groupe2cs.generator.domain.engine.FileWriterService;
import com.groupe2cs.generator.domain.engine.TemplateEngine;
import com.groupe2cs.generator.infrastructure.config.GeneratorProperties;
import com.groupe2cs.generator.domain.model.EntityDefinition;
import com.groupe2cs.generator.shared.Utils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ListControllerGeneratorService {

    private final TemplateEngine templateEngine;
    private final FileWriterService fileWriterService;
    private final GeneratorProperties properties;
    private final SseControllerGeneratorService sseControllerGeneratorService;

    public ListControllerGeneratorService(TemplateEngine templateEngine,
                                          FileWriterService fileWriterService,
                                          GeneratorProperties properties,
                                          SseControllerGeneratorService sseControllerGeneratorService
    ) {
        this.templateEngine = templateEngine;
        this.fileWriterService = fileWriterService;
        this.properties = properties;
        this.sseControllerGeneratorService = sseControllerGeneratorService;
    }

    public void generate(EntityDefinition definition, String baseDir) {
        Map<String, Object> context = new HashMap<>(definition.toMap());

        String outputDir = baseDir + "/" + properties.getControllerPackage();
        context.put("package", Utils.getPackage(outputDir));
        context.put("nameLower", definition.getName().toLowerCase());

        Set<String> imports = new LinkedHashSet<>();
        imports.add(Utils.getPackage(baseDir + "/" + properties.getDtoPackage()) + "." + definition.getName() + "Response");
        imports.add(Utils.getPackage(baseDir + "/" + properties.getQueryPackage()) + ".List" + definition.getName() + "Query");
        imports.add(Utils.getPackage(baseDir + "/" + properties.getDtoPackage()+".*"));
        imports.add(Utils.getPackage(baseDir + "/" + properties.getQueryHandlerPackage()+".*"));

        context.put("imports", imports);

        String content = templateEngine.render("presentation/listController.mustache", context);
        fileWriterService.write(outputDir, definition.getName() + "ListController.java", content);

        //sse generator
        sseControllerGeneratorService.generate(definition,baseDir);
    }
}
