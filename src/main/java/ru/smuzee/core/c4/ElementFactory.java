package ru.smuzee.core.c4;

import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.SoftwareSystem;

import java.util.ArrayList;

import static ru.smuzee.core.c4.SmuzeeTags.*;
import static ru.smuzee.core.c4.Names.*;


public class ElementFactory {

    /**
     * Задает базовые элементы, необходимые для включения приложения в Spring Cloud среду Системы
     *
     * @param platform    - Родительская микросервисная платформа
     * @param name        - Наименование микросервиса
     * @param description - Описание микросервиса
     * @param technology  - Технология микросервиса
     * @return - Каркас микросервиса
     */
    static Container prepareMicroservice(SoftwareSystem platform, String name, String description, String technology) {

        Container microservice = platform.addContainer( name, description, technology );
        microservice.addTags( MICROSERVICE_TAG );

        Component api = microservice.addComponent( "API" );
        api.addTags( API_TAG );
        Component consulClient = microservice.addComponent(WORKER.CONSUL_CLIENT, "Получение config, регистрация, обнаружение сервисов" );
        consulClient.addTags( SPRING_CLOUD_TAG );
        Component logback = microservice.addComponent( WORKER.LOGBACK, "Система логирования" );
        logback.addTags( SPRING_CLOUD_TAG );
        return microservice;
    }

    /**
     * Создает воркер - микросервис, оркестрируемый камундой
     *
     * @param platform    - Родительская микросервисная платформа
     * @param name        - Наименование микросервиса
     * @param description - Описание микросервиса
     * @param technology  - Технология микросервиса
     * @return - Каркас воркера
     */
    static Container prepareWorker(SoftwareSystem platform, String name, String description, String technology) {
        String WORKER_COMPONENTS_GROUP = "External Task Client";
        Container workerService = prepareMicroservice( platform, name, description, technology );
        workerService.addTags( WORKER_TAG );
        workerService.getComponentWithName( API ).setDescription( "Безопасные операции (GET, OPTIONS, HEAD)" );
        Component worker = workerService.addComponent(WORKER.WORKER_LIB, "Функциональный фасад операций", "FunctionalInterface" );
        Component topicManager = workerService.addComponent(WORKER.TOPIC_MNG, "Хореография выполнения задачи бизнес-процесса", "Runnable" );
        Component camundaClient = workerService.addComponent(WORKER.CAMUNDA_CLIENT, "Забирает внешние задачи по `business key` и `topic`", "HTTP client" );
        worker.setGroup( WORKER_COMPONENTS_GROUP );
        topicManager.setGroup( WORKER_COMPONENTS_GROUP );
        camundaClient.setGroup( WORKER_COMPONENTS_GROUP );

        topicManager.uses( camundaClient, "Объявляет топики" );
        topicManager.uses( worker, "Запускает выполнение" );
        worker.uses( camundaClient, "Завершает задачу", "complete()" );
        worker.uses( workerService.getComponentWithName( "Logback" ), "LOG" );
        worker.uses( workerService.getComponentWithName( "Consul Client" ), "Конфиг подисок" );
        camundaClient.uses( workerService.getComponentWithName( "Consul Client" ), "Конфиг подключения Camunda" );
        return workerService;
    }

    static void gatewayFilterGenerator(Container apiGateway) {
        ArrayList<Component> filters = new ArrayList<>();
        filters.add( apiGateway.addComponent( "Ограничительный фильтр", "Устанавливает предельное число запросов в единицу времени", "RateLimiter" ) );
        filters.add( apiGateway.addComponent( "Фильтр-логгер", "Логирует запросы и ответы", "Logging Filter" ) );
        filters.add( apiGateway.addComponent( "Предфильтры", "Применяются перед маршрутизацией, обеспечивая препроцессинг", "Pre-routing Filters" ) );
        filters.add( apiGateway.addComponent( "Фильтры маршрута", "Специфичны для конкретного маршрута", "Route Filters" ) );
        filters.add( apiGateway.addComponent( "Постфильтры", "Применяются после маршрутизации, то есть фильтруют ответы сервисов", "Post-routing Filters" ) );
        filters.forEach( f -> {
            f.setGroup( "Фабрики фильтров" );
            f.addTags( TINY_TAG );
        } );

        filters.get( 1 ).uses( apiGateway.getComponentWithName( "Logback" ), "LOG" );

        Component webHandler = apiGateway.getComponentWithName( "Обработчик" );
        webHandler.uses( filters.get( 0 ), "устойчивость" );
        webHandler.uses( filters.get( 1 ), "мониторинг" );
        webHandler.uses( filters.get( 2 ), "системная логика" );
        webHandler.uses( filters.get( 3 ), "доменная логика" );
        webHandler.uses( filters.get( 4 ), "UX" );
    }


}
