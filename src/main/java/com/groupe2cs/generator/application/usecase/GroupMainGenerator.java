package com.groupe2cs.generator.application.usecase;

import com.groupe2cs.generator.application.service.applicationservice.*;
import com.groupe2cs.generator.application.service.domainservice.AggregateGeneratorService;
import com.groupe2cs.generator.application.service.domainservice.ExceptionGeneratorService;
import com.groupe2cs.generator.application.service.domainservice.VoGeneratorService;
import com.groupe2cs.generator.application.service.infrastructureservice.EntityGeneratorService;
import com.groupe2cs.generator.application.service.infrastructureservice.RepositoryGeneratorService;
import com.groupe2cs.generator.application.service.presentationservice.*;
import com.groupe2cs.generator.application.service.shared.SharedGeneratorService;
import com.groupe2cs.generator.application.service.testservice.AllTestGeneratorService;
import com.groupe2cs.generator.application.service.testservice.SharedTestGeneratorService;
import com.groupe2cs.generator.infrastructure.config.GeneratorProperties;
import com.groupe2cs.generator.application.dto.ApiResponseDto;
import com.groupe2cs.generator.application.dto.EntityDefinitionDTO;
import com.groupe2cs.generator.domain.model.EntityDefinition;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class GroupMainGenerator {

    private final GeneratorProperties properties;
    private final CommandGeneratorService commandGenerator;
    private final AggregateGeneratorService aggregateGenerator;
    private final VoGeneratorService voGenerator;
    private final DtoRequestGeneratorService dtoRequestGeneratorService;
    private final DtoResponseGeneratorService dtoResponseGeneratorService;
    private final MapperGeneratorService mapperGenerator;
    private final ExceptionGeneratorService exceptionGeneratorService;
    private final RepositoryGeneratorService repositoryGeneratorService;
    private final EntityGeneratorService entityGeneratorService;
    private final ListQueryGeneratorService listQueryGeneratorService;
    private final ListQueryHandlerGeneratorService listQueryHandlerGeneratorService;
    private final ListControllerGeneratorService listControllerGeneratorService;
    private final PagedResponseGeneratorService pagedResponseGeneratorService;
    private final CreateControllerGeneratorService createControllerGeneratorService;
    private final DeleteControllerGeneratorService deleteControllerGeneratorService;
    private final FindByFieldControllerGeneratorService findByFieldControllerGeneratorService;
    private final UpdateControllerGeneratorService updateControllerGeneratorService;
    private final FindByFieldQueryGeneratorService findByFieldQueryGeneratorService;
    private final FindByFieldQueryHandlerGeneratorService findByFieldQueryHandlerGeneratorService;

    private final AllTestGeneratorService allTestGeneratorService;
    private final SharedGeneratorService sharedGeneratorService;
    private final SharedTestGeneratorService sharedTestGeneratorService;


    private EntityDefinition loadFromFileDefinition() {
        return EntityDefinition.fromSource(
                properties.getEntityName(),
                properties.getSourceRoot()
        );
    }

    private void log(String message) {
        System.out.println(message);
    }

    public GroupMainGenerator(
            GeneratorProperties properties,
            CommandGeneratorService commandGenerator,
            AggregateGeneratorService aggregateGenerator,
            VoGeneratorService voGenerator,
            DtoRequestGeneratorService dtoRequestGeneratorService,
            MapperGeneratorService mapperGenerator,
            DtoResponseGeneratorService dtoResponseGeneratorService,
            ExceptionGeneratorService exceptionGeneratorService,
            RepositoryGeneratorService repositoryGeneratorService,
            EntityGeneratorService entityGeneratorService,
            ListQueryGeneratorService listQueryGeneratorService,
            ListQueryHandlerGeneratorService listQueryHandlerGeneratorService,
            ListControllerGeneratorService listControllerGeneratorService,
            PagedResponseGeneratorService pagedResponseGeneratorService,
            CreateControllerGeneratorService createControllerGeneratorService,
            DeleteControllerGeneratorService deleteControllerGeneratorService,
            FindByFieldControllerGeneratorService findByFieldControllerGeneratorService,
            UpdateControllerGeneratorService updateControllerGeneratorService,
            FindByFieldQueryGeneratorService findByFieldQueryGeneratorService,
            FindByFieldQueryHandlerGeneratorService findByFieldQueryHandlerGeneratorService,
            AllTestGeneratorService allTestGeneratorService,
            SharedGeneratorService sharedGeneratorService,
            SharedTestGeneratorService sharedTestGeneratorService

    ) {
        this.properties = properties;
        this.commandGenerator = commandGenerator;
        this.aggregateGenerator = aggregateGenerator;
        this.voGenerator = voGenerator;
        this.dtoRequestGeneratorService = dtoRequestGeneratorService;
        this.mapperGenerator = mapperGenerator;
        this.dtoResponseGeneratorService = dtoResponseGeneratorService;
        this.exceptionGeneratorService = exceptionGeneratorService;
        this.repositoryGeneratorService = repositoryGeneratorService;
        this.entityGeneratorService = entityGeneratorService;
        this.listQueryGeneratorService = listQueryGeneratorService;
        this.listQueryHandlerGeneratorService = listQueryHandlerGeneratorService;
        this.listControllerGeneratorService = listControllerGeneratorService;
        this.pagedResponseGeneratorService = pagedResponseGeneratorService;
        this.createControllerGeneratorService = createControllerGeneratorService;

        this.deleteControllerGeneratorService = deleteControllerGeneratorService;
        this.findByFieldControllerGeneratorService = findByFieldControllerGeneratorService;
        this.updateControllerGeneratorService = updateControllerGeneratorService;

        this.findByFieldQueryGeneratorService=findByFieldQueryGeneratorService;
        this.findByFieldQueryHandlerGeneratorService=findByFieldQueryHandlerGeneratorService;
        this.sharedGeneratorService = sharedGeneratorService;
        this.allTestGeneratorService = allTestGeneratorService;
        this.sharedTestGeneratorService = sharedTestGeneratorService;
    }

    public Flux<ApiResponseDto> generateStreaming(EntityDefinitionDTO definitionDto) {
        Sinks.Many<ApiResponseDto> sink = Sinks.many().unicast().onBackpressureBuffer();

        EntityDefinition definition = definitionDto.getDefinition();
        String outputDir = definitionDto.getOutputDir();

        Mono.fromRunnable(() -> {
            try {
                emit(sink, "Generating Value Objects...");
                voGenerator.generate(definition, outputDir);

                emit(sink, "Generating Aggregate...");
                aggregateGenerator.generate(definition, outputDir);

                emit(sink, "Generating Exception...");
                exceptionGeneratorService.generate(definition, outputDir);

                emit(sink, "Generating Commands...");
                commandGenerator.generate(definition, outputDir);

                emit(sink, "Generating Queries...");
                listQueryGeneratorService.generate(definition, outputDir);
                listQueryHandlerGeneratorService.generate(definition, outputDir);
                findByFieldQueryGeneratorService.generate(definition, outputDir);
                findByFieldQueryHandlerGeneratorService.generate(definition, outputDir);

                emit(sink, "Generating DTO Requests...");
                dtoRequestGeneratorService.generate(definition, outputDir);

                emit(sink, "Generating DTO Responses...");
                dtoResponseGeneratorService.generate(definition, outputDir);
                pagedResponseGeneratorService.generate(definition, outputDir);

                emit(sink, "Generating entity...");
                entityGeneratorService.generate(definition, outputDir);

                emit(sink, "Generating Mappers...");
                mapperGenerator.generate(definition, outputDir);

                emit(sink, "Generating Repositories...");
                repositoryGeneratorService.generate(definition, outputDir);


                emit(sink, "Generating Controllers...");
                listControllerGeneratorService.generate(definition, outputDir);
                createControllerGeneratorService.generate(definition, outputDir);
                deleteControllerGeneratorService.generate(definition, outputDir);
                findByFieldControllerGeneratorService.generate(definition, outputDir);
                updateControllerGeneratorService.generate(definition, outputDir);

                emit(sink, "Generating tests...");
                sharedTestGeneratorService.generate(outputDir);
                allTestGeneratorService.generate(definition,outputDir);
                sharedGeneratorService.generate(definition,outputDir);

                emit(sink, "✅ Code generation complete!");

                sink.tryEmitComplete();

            } catch (Exception e) {
                sink.tryEmitNext(ApiResponseDto.builder()
                        .code(500)
                        .message("❌ Error during generation: " + e.getMessage())
                        .build());
                sink.tryEmitComplete();
            }
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()).subscribe();

        return sink.asFlux();
    }


    private void emit(Sinks.Many<ApiResponseDto> sink, String msg) {
        sink.tryEmitNext(ApiResponseDto.builder()
                .code(200)
                .message(msg)
                .build());
    }


}
