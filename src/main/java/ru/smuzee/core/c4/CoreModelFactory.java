package ru.smuzee.core.c4;

import com.structurizr.Workspace;
import com.structurizr.importer.documentation.DefaultDocumentationImporter;
import com.structurizr.importer.documentation.DefaultImageImporter;
import com.structurizr.model.*;

import java.io.File;

import static ru.smuzee.core.c4.ElementFactory.*;
import static ru.smuzee.core.c4.SmuzeeTags.*;
import static ru.smuzee.core.c4.Names.*;


public class CoreModelFactory {


    public static Model prepareFull(Workspace workspace){
        Model model = prepareBasic(workspace);
        addCommonMicroservices(model);
        addBasicDocs(model);
        addMicroserviceDocs(model);
        return model;
    }

    public static Model prepareBasic(Workspace workspace) {
        Model model = workspace.getModel();


        //  ooooooooo.                                  oooo
        //  `888   `Y88.                                `888
        //   888   .d88'  .ooooo.   .ooooo.  oo.ooooo.   888   .ooooo.
        //   888ooo88P'  d88' `88b d88' `88b  888' `88b  888  d88' `88b
        //   888         888ooo888 888   888  888   888  888  888ooo888
        //   888         888    .o 888   888  888   888  888  888    .o
        //  o888o        `Y8bod8P' `Y8bod8P'  888bod8P' o888o `Y8bod8P'
        //                                    888
        //                                    o888o
        Person businessAnalyst = model.addPerson( "Бизнес-аналитик", "Создает и поддерживает процессы верхнего уровня" );
        businessAnalyst.addTags( "Analyst" );
        Person systemAnalyst = model.addPerson( "Системный аналитик", "Создает и поддерживает процессы нижнего уровня" );
        systemAnalyst.addTags( "Analyst" );
        Person operator = model.addPerson( "Оператор", "Участник процесса выдачи охотбилетов" );
        operator.addTags( "Operator" );
        Person admin = model.addPerson( "DevOps", "Системный администратор\nПоддерживает Систему" );
        admin.addTags( "Administrator" );
        Person security = model.addPerson( "Администратор безопасности", "Управление доступом" );
        security.addTags( "Security" );


        //  oooooooooooo                                    .   oooooooooooo                   .o8
        //  `888'     `8                                  .o8   `888'     `8                  "888
        //   888         oooo d8b  .ooooo.  ooo. .oo.   .o888oo  888         ooo. .oo.    .oooo888
        //   888oooo8    `888""8P d88' `88b `888P"Y88b    888    888oooo8    `888P"Y88b  d88' `888
        //   888    "     888     888   888  888   888    888    888    "     888   888  888   888
        //   888          888     888   888  888   888    888 .  888       o  888   888  888   888
        //  o888o        d888b    `Y8bod8P' o888o o888o   "888" o888ooooood8 o888o o888o `Y8bod88P"

        SoftwareSystem smev = model.addSoftwareSystem( "СМЭВ 3", "Система межведомственного электронного взаимодействия" );
        smev.addTags( "SMEV" );

        SoftwareSystem kibana = model.addSoftwareSystem( "Kibana", "Просмотр логов микросервисов" );
        kibana.addTags( WEB_BROWSER_TAG, "Kibana" );

        SoftwareSystem consulUI = model.addSoftwareSystem( "Consul WEB", "Настройки, конфигурации health check, etc." );
        consulUI.addTags( WEB_BROWSER_TAG, "Consul" );

        SoftwareSystem keycloakUI = model.addSoftwareSystem( "Keycloak WEB", "Управление пользователями, ролями, доступом" );
        keycloakUI.addTags( WEB_BROWSER_TAG, "Keycloak" );

        SoftwareSystem singlePageApplication = model.addSoftwareSystem( "Single-Page Application", "Предоставляет доступ к функциям системы посредством web-интерфейса" );
        singlePageApplication.addTags( WEB_BROWSER_TAG );

        businessAnalyst.uses( singlePageApplication, "Интерфейс BPM-моделера" + "BPMN 2.0, CockPit" );
        systemAnalyst.uses( singlePageApplication, "BPM UI + скрипты", "JavaScript" );
        operator.uses( singlePageApplication, "Реестры, формы, задачи", "TaskList" );
        security.uses( keycloakUI, "Предоставляет доступ" );
        kibana.delivers( security, "Логи безопасности" );
        admin.uses( consulUI, "Конфигурирует микросервисы" );
        kibana.delivers( admin, "Системные логи" );
        systemAnalyst.uses( consulUI, "Управляет метаданными воркеров" );


        //   .--. .-..-.      .----.
        //  : .--': `' :      `--. :
        //  `. `. : .. :.-..-.  ,',' .--.  .--.
        //   _`, :: :; :: :; :.'.'_ ' '_.'' '_.'
        //  `.__.':_;:_;`.__.':____;`.__.'`.__.'

        SoftwareSystem smuzee = model.addSoftwareSystem( SMUZEE, "Система автоматизации предоставления государственных услуг" );
        smuzee.addTags( "SMuZee" );

        //CONSUL
        Container consul = smuzee.addContainer( CONSUL, "Service Discovery & Distributed Config" );
        Component consulConfig = consul.addComponent( "K/V сервис конфигураций", "", "Spring Cloud Consul Config" );
        Component consulApi = consul.addComponent( "Consul API", "", "REST" );
        consulApi.addTags( API_TAG );
        Component consulDiscovery = consul.addComponent( "Service Discovery", "Регистрация и обнаружение микросервисов", "Spring Cloud Consul Service Discovery" );
        consulApi.uses( consulDiscovery, "" );
        consulApi.uses( consulConfig, "" );

        //LOGGING
        Container elastic = smuzee.addContainer( "ElasticSearch", "Хранение логов + движок поиска" );
        Container logstash = smuzee.addContainer( "Logstash", "Обработчик логов" );
        Container filebeat = smuzee.addContainer( FILEBEAT, "Сборщик логов докер-контейнеров" );
        filebeat.uses( logstash, "Отправляет логи" );
        logstash.uses( elastic, "Сохраняет логи" );
        kibana.uses( elastic, "Запрашивает логи" );
        consul.addTags( SPRING_CLOUD_TAG );
        logstash.addTags( SPRING_CLOUD_TAG );
        filebeat.addTags( SPRING_CLOUD_TAG );
        consulUI.uses( consul, "", "HTTPS", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG, "SPA"} );

        Container webApplication = smuzee.addContainer( NGINX, "Предоставляет браузеру статический контент и SPA", "Nginx" );


        //    .oooooo.          .o.       ooooooooooooo oooooooooooo oooooo   oooooo     oooo       .o.       oooooo   oooo
        //   d8P'  `Y8b        .888.      8'   888   `8 `888'     `8  `888.    `888.     .8'       .888.       `888.   .8'
        //  888               .8"888.          888       888           `888.   .8888.   .8'       .8"888.       `888. .8'
        //  888              .8' `888.         888       888oooo8       `888  .8'`888. .8'       .8' `888.       `888.8'
        //  888     ooooo   .88ooo8888.        888       888    "        `888.8'  `888.8'       .88ooo8888.       `888'
        //  `88.    .88'   .8'     `888.       888       888       o      `888'    `888'       .8'     `888.       888
        //   `Y8bood8P'   o88o     o8888o     o888o     o888ooooood8       `8'      `8'       o88o     o8888o     o888o

        Container apiGateway = prepareMicroservice( smuzee, API_GATEWAY.SELF, "Единая точка входа, маршрутизация и фильтрация, агрегация совокупных конфигураций", "Spring Cloud Gateway" );
        apiGateway.addTags( SPRING_CLOUD_TAG, ORCHESTRATOR_TAG );

        Component handlerMapper = apiGateway.getComponentWithName( API );
        Component gatewayConsul = apiGateway.getComponentWithName( WORKER.CONSUL_CLIENT );
        handlerMapper.setDescription( "Маппер запросов - распределеяет запросы по обработчикам" );
        handlerMapper.setTechnology( "Gateway Handler Mapper" );
        Component cacher = apiGateway.addComponent( "Модуль кэширования" );
        Component securityGate = apiGateway.addComponent( "Контроллер доступа", "Запрос JWT, настройки безопасности и нагрузки" );
        Component webHandler = apiGateway.addComponent( "Обработчик", "Выполняет запросы и формирует ответ для сопоставленного маршрута", "Gateway Web Handler" );
        webHandler.addTags( SPRING_CLOUD_TAG );
        securityGate.uses( cacher, "Кэш сессии" );
        handlerMapper.uses( webHandler, "Передает запрос" );
        singlePageApplication.uses( handlerMapper, "REST", "HTTPS", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG} );
        gatewayConsul.uses( consulDiscovery, "Координаты сервисов", "HTTP", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG, LEAP_INTERACTION_TAG} );
        handlerMapper.uses( gatewayConsul, "Конфигурация маршрутов, предикат" );
        handlerMapper.uses( securityGate, "Проверка доступа" );
        gatewayFilterGenerator( apiGateway );

        Container redis = smuzee.addContainer( "GatewayDB", "Кэш сессий и конфигураций", "Redis" );
        redis.addTags( DATABASE_TAG, "Redis" );
        cacher.uses( redis, "Хранение" );


        // oooo    oooo                                 oooo                      oooo
        // `888   .8P'                                  `888                      `888
        //  888  d8'     .ooooo.  oooo    ooo  .ooooo.   888   .ooooo.   .oooo.    888  oooo
        //  88888[      d88' `88b  `88.  .8'  d88' `"Y8  888  d88' `88b `P  )88b   888 .8P'
        //  888`88b.    888ooo888   `88..8'   888        888  888   888  .oP"888   888888.
        //  888  `88b.  888    .o    `888'    888   .o8  888  888   888 d8(  888   888 `88b.
        // o888o  o888o `Y8bod8P'     .8'     `Y8bod8P' o888o `Y8bod8P' `Y888""8o o888o o888o
        //                       .o..P'
        //                       `Y8P'
        Container keycloak = smuzee.addContainer( KEYCLOAK, "Аутентификация и авторизация пользователей", "Java" );

        Component keycloakRepo = keycloak.addComponent( "Пользователи и роли", "CRUD операции", "Spring Data JPA" );
        Component keycloakApi = keycloak.addComponent( "Keycloak API", "", "REST" );
        keycloakApi.addTags( API_TAG );
        Component realm = keycloak.addComponent( "Keycloak Realm", "Политики доступа, выпуск JWT", "REST" );
        Component esia = keycloak.addComponent( "Провайдер ЕСИА", "Сертификационный шлюз", "SAML2.0, HTTPS" );
        keycloakApi.uses( keycloakRepo, "" );
        keycloakApi.uses( realm, "" );
        keycloakApi.uses( esia, "" );
        esia.uses( realm, "" );
        esia.uses( keycloakRepo, "" );
        keycloakUI.uses( keycloakApi, "REST", "HTTPS", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG} );
        securityGate.uses( realm, "Проверка JWT", "HTTPS", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG, LEAP_INTERACTION_TAG} );
        singlePageApplication.uses( realm, "Запрос JWT (авторизация)", "HTTPS", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG, LEAP_INTERACTION_TAG} );

        Container keycloakDB = smuzee.addContainer( "User Storage", "Хранение пользователей, политик, релмов и пр.", "PostreSQL" );
        keycloakDB.addTags( DATABASE_TAG, POSTGRESQL_TAG );
        keycloakRepo.uses( keycloakDB, "", "JDBC", InteractionStyle.Synchronous );


        //    .oooooo.                                                             .o8
        //   d8P'  `Y8b                                                           "888
        //  888           .oooo.   ooo. .oo.  .oo.   oooo  oooo  ooo. .oo.    .oooo888   .oooo.
        //  888          `P  )88b  `888P"Y88bP"Y88b  `888  `888  `888P"Y88b  d88' `888  `P  )88b
        //  888           .oP"888   888   888   888   888   888   888   888  888   888   .oP"888
        //  `88b    ooo  d8(  888   888   888   888   888   888   888   888  888   888  d8(  888
        //   `Y8bood8P'  `Y888""8o o888o o888o o888o  `V88V"V8P' o888o o888o `Y8bod88P" `Y888""8o

        Container camunda = smuzee.addContainer( CAMUNDA, "Система управления бизнес-процессами", "Java" );
        camunda.addTags( "Camunda", ORCHESTRATOR_TAG );

        Component camundaApi = camunda.addComponent( "Camunda REST", "Oбщедоступный API", "Java" );
        Component externalTaskQueue = camunda.addComponent( "Очередь внешних задач", "", "" );
        externalTaskQueue.addTags( QUEUE_TAG );
        Component userTaskQueue = camunda.addComponent( "Очередь пользовательских задач", "", "" );
        userTaskQueue.addTags( QUEUE_TAG );
        Component processEngine = camunda.addComponent( "Процессный движок", "Среда выполнения процессов, репозиторий, управление задачами", "Java" );
        Component highLevelProcesses = camunda.addComponent( "Верхнеуровневые процессы", "Реализуют прикладные задачи предметной-области", "BPMN" );
        Component lowLevelProcesses = camunda.addComponent( "Низкоуровневые процессы", "Решают задачи обработки данных и простые логические цепочки", "BPMN" );
        camundaApi.addTags( API_TAG );
        camundaApi.uses( processEngine, "Управление" );
        processEngine.uses( highLevelProcesses, "Запуск по таймеру или вручную", "", InteractionStyle.Asynchronous );
        processEngine.uses( realm, "Проверка доступа", "HTTP", InteractionStyle.Synchronous, new String[]{HTTP_REQUEST_TAG, LEAP_INTERACTION_TAG} );
        highLevelProcesses.uses( lowLevelProcesses, "Activity Call с параметром `Business key`", "" );
        highLevelProcesses.uses( userTaskQueue, "Контроль человеку", "BPMN", InteractionStyle.Asynchronous );
        lowLevelProcesses.uses( externalTaskQueue, "Контроль машине", "BPMN", InteractionStyle.Asynchronous );
        businessAnalyst.uses( highLevelProcesses, "Разрабатывает", "UI", InteractionStyle.Asynchronous, new String[]{LEAP_INTERACTION_TAG} );
        userTaskQueue.delivers( operator, "Назначает задачи", "UI", InteractionStyle.Asynchronous, new String[]{LEAP_INTERACTION_TAG} );
        systemAnalyst.uses( lowLevelProcesses, "Разрабатывает", "UI", InteractionStyle.Asynchronous, new String[]{LEAP_INTERACTION_TAG} );

        Container camundaDB = smuzee.addContainer( "Camunda DB", "Хранение процессов и их инстансов", "PostgreSQL" );
        camundaDB.addTags( DATABASE_TAG, POSTGRESQL_TAG );
        processEngine.uses( camundaDB, "", "JDBC", InteractionStyle.Synchronous );


        //    .oooooo.   ooo        ooooo oooooooooooo oooooooooo.
        //   d8P'  `Y8b  `88.       .888' `888'     `8 `888'   `Y8b
        //  888           888b     d'888   888          888     888
        //  888           8 Y88. .P  888   888oooo8     888oooo888'
        //  888           8  `888'   888   888    "     888    `88b
        //  `88b    ooo   8    Y     888   888       o  888    .88P
        //   `Y8bood8P'  o8o        o888o o888ooooood8 o888bood8P'

        Container smevAdapter = smuzee.addContainer( SMEV_ADAPTER, "Реализует интерфейсы взаимодействия" );
        smevAdapter.uses( smev, "Подключение к транспортной системе", "", InteractionStyle.Asynchronous );

        Container smevGate = prepareWorker( smuzee, SMEV_GATE.SELF, "Контроллер СМЭВ", "Spring Boot" );

        Component smevGateClient = smevGate.addComponent( "HTTP клиент", "Роутинг исходящих запросов, коннектор бд" );
        Component smevGateXml = smevGate.addComponent( "XML-мастерская", "Трансформация, валидация, генерация форм", "XQuery" );
        Component smevGateConverter = smevGate.addComponent( "Конвертер запросов", "Двусторонний маппинг и категоризация содержимого" );
        Component smevCamundaClient = smevGate.getComponentWithName( WORKER.CAMUNDA_CLIENT );
        Component smevWorker = smevGate.getComponentWithName( WORKER.WORKER_LIB );
        Component smevTopicMgr = smevGate.getComponentWithName( WORKER.TOPIC_MNG );
        smevGateConverter.uses( smevWorker, "Реализует @PUT-воркер" );
        smevGateConverter.uses( smevGateClient, "Формирует get/send" );
        smevGateConverter.uses( smevGateXml, "Обработка XML" );

        smevGateClient.uses( smevAdapter, "Get() / Send()", "HTTP REST", InteractionStyle.Asynchronous, new String[]{HTTP_REQUEST_TAG} );
        smevGateClient.uses( smevGate.getComponentWithName( WORKER.CONSUL_CLIENT), "Настройки СМЭВ" );
        smevGateClient.uses( smevGate.getComponentWithName( WORKER.LOGBACK ), "LOG" );
        smevGate.getComponentWithName( API ).uses( smevGateClient, "Роутинг к методу Find" );
        smevGate.getComponentWithName( API ).uses( smevGate.getComponentWithName( WORKER.LOGBACK ), "LOG" );

        Container smevGateDb = smuzee.addContainer( "XML БД", "Архив запросов", "eXist-db | BaseX" );
        smevGateDb.addTags( DATABASE_TAG, "eXistDB" );
        smevGateClient.uses( smevGateDb, "СМЭВ-сообщения" );
        smevGateConverter.uses( smevGateDb, "Camunda task" );


        //  ooo        ooooo  o8o                                                                        o8o
        //  `88.       .888'  `"'                                                                        `"'
        //   888b     d'888  oooo   .ooooo.  oooo d8b  .ooooo.   .oooo.o  .ooooo.  oooo d8b oooo    ooo oooo   .ooooo.   .ooooo.   .oooo.o
        //   8 Y88. .P  888  `888  d88' `"Y8 `888""8P d88' `88b d88(  "8 d88' `88b `888""8P  `88.  .8'  `888  d88' `"Y8 d88' `88b d88(  "8
        //   8  `888'   888   888  888        888     888   888 `"Y88b.  888ooo888  888       `88..8'    888  888       888ooo888 `"Y88b.
        //   8    Y     888   888  888   .o8  888     888   888 o.  )88b 888    .o  888        `888'     888  888   .o8 888    .o o.  )88b
        //  o8o        o888o o888o `Y8bod8P' d888b    `Y8bod8P' 8""888P' `Y8bod8P' d888b        `8'     o888o `Y8bod8P' `Y8bod8P' 8""888P'
        //
        Container microserviceBlueprint = prepareWorker( smuzee, WORKER.SELF, "Шаблон/каркас воркера", "Spring Boot" );
        Component service = microserviceBlueprint.addComponent( "Бизнес-слой", "Операции API и Camunda External Task", "Spring Bean" );
        Component repo = microserviceBlueprint.addComponent( "Слой хранения", "Репозиторий доступа к БД", "Spring JPA" );
        microserviceBlueprint.getComponentWithName(API ).uses( service, "" );
        service.uses( microserviceBlueprint.getComponentWithName( WORKER.WORKER_LIB ), "Реализует" );
        service.uses( repo, "" );
        service.uses( microserviceBlueprint.getComponentWithName( WORKER.LOGBACK ), "LOG" );
        repo.uses( microserviceBlueprint.getComponentWithName( WORKER.LOGBACK ), "LOG" );

        return model;
    }

    private static void addCommonMicroservices(Model model){
        SoftwareSystem smuzee=model.getSoftwareSystemWithName(SMUZEE);

        Container ticketService = prepareWorker( smuzee, "Сервис охотбилетов", "Сущности: 1. охотничьи билеты 2. заявления на выдачу 3. заявления на аннулирование", "Spring Boot" );

        Container hunterService = prepareWorker( smuzee, "Сервис Охотник", "Управляет одной сущностью", "Spring Boot" );
        Component hunterRepo = hunterService.addComponent( "Hunter manager", "Хранение, маппинг", "Spring Data JPA" );
        hunterService.getComponentWithName( API ).uses( hunterRepo, "" );
        hunterRepo.uses( hunterService.getComponentWithName( WORKER.WORKER_LIB ), "Реализует" );

        Container hunterDb = smuzee.addContainer( "Hunter database", "", "PostgreSQL" );
        hunterDb.addTags( DATABASE_TAG );

        Container mdmService = prepareWorker( smuzee, "MDM", "Сервис собственных справочников", "Spring Boot" );


        Container referenceService = prepareWorker( smuzee, "НСИ", "Сервис внешних справочников", "Spring Boot" );

    }
    public static void addBasicDocs(Model model){
        DefaultDocumentationImporter documentationImporter = new DefaultDocumentationImporter();
        DefaultImageImporter imageImporter = new DefaultImageImporter();
        SoftwareSystem smuzee=model.getSoftwareSystemWithName(SMUZEE);


        File microserviceDocs = new File( "src/main/java/ru/smuzee/core/c4/docs/microservice" );
        documentationImporter.importDocumentation( smuzee.getContainerWithName(WORKER.SELF), microserviceDocs );

        File camundaDocs = new File( "src/main/java/ru/smuzee/core/c4/docs/camunda" );
        documentationImporter.importDocumentation(  smuzee.getContainerWithName(CAMUNDA), camundaDocs );
        imageImporter.importDocumentation(  smuzee.getContainerWithName(CAMUNDA), camundaDocs );

        File smevDocs = new File( "src/main/java/ru/smuzee/core/c4/docs/smev" );
        documentationImporter.importDocumentation(  smuzee.getContainerWithName(SMEV_GATE.SELF), smevDocs );

        File logDocs = new File( "src/main/java/ru/smuzee/core/c4/docs/logging" );
        documentationImporter.importDocumentation( smuzee.getContainerWithName(FILEBEAT), logDocs );
        imageImporter.importDocumentation( smuzee.getContainerWithName(FILEBEAT), logDocs );


        File consulDocs = new File( "src/main/java/ru/smuzee/core/c4/docs/consul" );
        documentationImporter.importDocumentation( smuzee.getContainerWithName(CONSUL), consulDocs );

        File gateDocs = new File( "src/main/java/ru/smuzee/core/c4/docs/gateway" );
        documentationImporter.importDocumentation( smuzee.getContainerWithName(API_GATEWAY.SELF), gateDocs );


        File smevAdapterDocs = new File( "src/main/java/ru/smuzee/core/c4/docs/smevAdapter" );
        documentationImporter.importDocumentation( smuzee.getContainerWithName(SMEV_ADAPTER), smevAdapterDocs );
        imageImporter.importDocumentation( smuzee.getContainerWithName(SMEV_ADAPTER), smevAdapterDocs );
    }
    public static void addMicroserviceDocs(Model model){

    }


}
