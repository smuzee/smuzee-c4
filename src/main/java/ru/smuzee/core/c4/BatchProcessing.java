package ru.smuzee.core.c4;

import com.structurizr.model.*;

import java.util.Objects;

import static ru.smuzee.core.c4.Names.*;
import static ru.smuzee.core.c4.SmuzeeTags.*;

public class BatchProcessing {

    public static void modelBatching(Model model) {
        SoftwareSystem smuzee=model.getSoftwareSystemWithName(SMUZEE);
        Component webHandler = smuzee.getContainerWithName(API_GATEWAY.SELF).getComponentWithName("Обработчик");
        Container webApplication = smuzee.getContainerWithName(NGINX);

        //Все фронты связываем с веб-сервером
        model.getSoftwareSystems().stream()
                .filter( s -> s.hasTag( WEB_BROWSER_TAG ) )
                .filter( s -> s.getRelationships().stream().noneMatch( r -> r.hasTag( "SPA" ) ) )
                .forEach( web -> web.uses( webApplication, "Контент", "HTTPS", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG, "SPA"} ) );

        //Создать HTTP-запросы от Gateway ко всем API
        model.getElements().stream()
                .filter( e -> e instanceof Component )
                .filter( c -> c.hasTag( API_TAG ) && !c.getCanonicalName().equals( "Component://Hunt.API Gateway.API" ) )
                .forEach( api -> webHandler.uses( (Component) api, "downstream route", "HTTP", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG} ) );

        //Маркировать API calls к воркерам как 'безопасные'
        model.getRelationships().stream()
                .filter( r -> r.hasTag( HTTP_REQUEST_TAG ) )
                .filter( r -> r.getDestination().hasTag( API_TAG ) )
                .filter( r -> r.getDestination().getParent().hasTag( WORKER_TAG ) )
                .forEach( r -> r.addTags( SAFE_REQUEST_TAG ) );
    }

    public static void systemBatching(SoftwareSystem smuzee) {
        Container filebeat = smuzee.getContainerWithName(FILEBEAT);
        Container apiGateway = smuzee.getContainerWithName(API_GATEWAY.SELF);
        assert apiGateway != null;
        Component externalTaskQueue = smuzee.getContainerWithName(CAMUNDA).getComponentWithName( "Очередь внешних задач" );
        Component consulApi = smuzee.getContainerWithName(CONSUL).getComponentWithName( "Consul API" );

        //Озеленим Spring Cloud контейнеры внутри
        smuzee.getContainers().stream()
                .filter( c -> c.hasTag( SPRING_CLOUD_TAG ) )
                .filter( c -> !c.equals( apiGateway ) )
                .flatMap( container -> container.getComponents().stream() )
                .forEach( component -> component.addTags( SPRING_CLOUD_TAG ) );

        //Связать все воркеры с Камундой
        smuzee.getContainers().stream()
                .filter( c -> c.hasTag( WORKER_TAG ) )
                .map( container -> container.getComponentWithName( Names.WORKER.CAMUNDA_CLIENT ) )
                .filter( Objects::nonNull )
                .forEach( client -> client.uses( externalTaskQueue, "Подписка на топики", "HTTP (long polling)", InteractionStyle.Asynchronous, new String[]{HTTP_REQUEST_TAG, LEAP_INTERACTION_TAG} ) );

        //Зарегистрировать сервисы в Consul
        smuzee.getContainers().stream()
                .filter( c -> c.hasTag( MICROSERVICE_TAG ) )
                .map( container -> container.getComponentWithName( "Consul Client" ) )
                .filter( Objects::nonNull )
                .forEach( client -> client.uses( consulApi, "Регистрация, конфигурация", "HTTP", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG, SERVICE_REGISTRATION_TAG} ) );

        //Связать сборщика логов
        smuzee.getContainers().stream()
                .map( e -> e.getComponentWithName( "Logback" ) )
                .filter( Objects::nonNull )
                .forEach( log -> filebeat.uses( log, "Выгрузка логов", "Docker Discovery", InteractionStyle.Asynchronous, new String[]{LEAP_INTERACTION_TAG} ) );

    }
}
