package ru.smuzee.core.c4;


import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.api.StructurizrClientException;
import com.structurizr.configuration.WorkspaceScope;
import com.structurizr.model.*;
import com.structurizr.util.ImageUtils;
import com.structurizr.view.*;

import java.io.File;
import java.io.IOException;

import static ru.smuzee.core.c4.BatchProcessing.modelBatching;
import static ru.smuzee.core.c4.BatchProcessing.systemBatching;
import static ru.smuzee.core.c4.CoreModelFactory.prepareFull;
import static ru.smuzee.core.c4.Styler.beautify;
import static ru.smuzee.core.c4.Styler.brandBook;
import static ru.smuzee.core.c4.SmuzeeTags.*;
import static ru.smuzee.core.c4.Names.*;



public class CoreComposer {
    private static final long WORKSPACE_ID = 1;
    private static final String WORKSPACE_URL = "http://structurizr.moarse.ru/api";
    private static final String API_KEY = "0eb102fa-d547-4faf-ad41-b52d0b30361a";
    private static final String API_SECRET = "f3077957-6843-44e9-aa86-9838452321b4";

    public static void main(String[] args) throws StructurizrClientException, IOException {
        Workspace workspace = new Workspace( "SMuZee core components", "" );
        workspace.getConfiguration().setScope( WorkspaceScope.SoftwareSystem );

        Model model = prepareFull(workspace);

        model.setImpliedRelationshipsStrategy( new CreateImpliedRelationshipsUnlessSameRelationshipExistsStrategy() );

        SoftwareSystem smuzee=model.getSoftwareSystemWithName(SMUZEE);
        Container apiGateway = smuzee.getContainerWithName(API_GATEWAY.SELF);
        Container microserviceBlueprint = smuzee.getContainerWithName(WORKER.SELF);
        Container filebeat = smuzee.getContainerWithName(FILEBEAT);
        Container consul = smuzee.getContainerWithName(CONSUL);
        Container smevGate = smuzee.getContainerWithName(SMEV_GATE.SELF);
        Container camunda = smuzee.getContainerWithName(CAMUNDA);
        Container smevAdapter = smuzee.getContainerWithName(SMEV_ADAPTER);
        Container keycloak = smuzee.getContainerWithName(KEYCLOAK);
        Container logstash = smuzee.getContainerWithName(LOGSTASH);


        //Batch-процессинг
        systemBatching( smuzee );
        modelBatching( model );


        //  oooooo     oooo ooooo oooooooooooo oooooo   oooooo     oooo  .oooooo..o
        //   `888.     .8'  `888' `888'     `8  `888.    `888.     .8'  d8P'    `Y8
        //    `888.   .8'    888   888           `888.   .8888.   .8'   Y88bo.
        //     `888. .8'     888   888oooo8       `888  .8'`888. .8'     `"Y8888o.
        //      `888.8'      888   888    "        `888.8'  `888.8'          `"Y88b
        //       `888'       888   888       o      `888'    `888'      oo     .d8P
        //        `8'       o888o o888ooooood8       `8'      `8'       8""88888P'

        ViewSet views = workspace.getViews();
        //Стили
        Branding branding = brandBook( views );
        Styles styles = beautify( views );

        ContainerView huntBackend = views.createContainerView( smuzee, "huntBackend", "Backend системы" );
        huntBackend.addProperty( "structurizr.groups", "false" );
        huntBackend.setTitle( "Архитектура Backend" );
        huntBackend.addDefaultElements();

        huntBackend.remove( microserviceBlueprint );
        huntBackend.removeElementsWithTag( DATABASE_TAG );
        huntBackend.removeElementsWithTag( Tags.PERSON );
        model.getRelationships().stream()
                .filter( r -> r.getSource().equals( filebeat ) && !r.getDestination().equals( logstash ) )
                .forEach( huntBackend::remove );
        model.getRelationships().stream()
                .filter( r -> r.getSource().hasTag( WORKER_TAG ) && r.getDestination().equals( consul ) )
                .forEach( huntBackend::remove );
        model.getRelationships().stream()
                .filter( r -> r.getSource().equals( apiGateway ) && r.getDestination().hasTag( WORKER_TAG ) )
                .forEach( huntBackend::remove );


        ComponentView microserviceOverview = views.createComponentView( microserviceBlueprint, "archetype", "Окружение и структура микросервиса" );
        microserviceOverview.setTitle( "Шаблон микросервиса системы" );
        microserviceOverview.addDefaultElements();
        model.getRelationships().stream()
                .filter( r -> r.getSource().equals( apiGateway ) && !r.getDestination().getParent().equals( microserviceBlueprint ) )
                .forEach( microserviceOverview::remove );


        ComponentView smevGateView = views.createComponentView( smevGate, "smevGate", "Структура обработчика запросов из/в СМЭВ" );
        smevGateView.setTitle( "Компоненты контроллерa СМЭВ" );
        smevGateView.addDefaultElements();
        smevGateView.remove( filebeat );
        smevGateView.remove( apiGateway );

        /*
        DynamicView dynamicSmevView = views.createDynamicView( smevGate, "smevFlow", "Поток сообщений СМЭВ" );
        dynamicSmevView.add(smevTopicMgr, "Подписка на Topic" ,smevCamundaClient);
        dynamicSmevView.add(smevCamundaClient,"fetchAndLock()", externalTaskQueue );
        dynamicSmevView.add(externalTaskQueue, "externalTask",smevCamundaClient );
        dynamicSmevView.add(smevCamundaClient , "invoke",smevTopicMgr );
        dynamicSmevView.add(smevTopicMgr, "beanDefinitionPostProcessor",smevWorker);
        dynamicSmevView.add(smevWorker ,"functional interface implementation",smevGateConverter );
        dynamicSmevView.add(smevGateConverter,"jsonToXML" ,smevGateXml );
        dynamicSmevView.add(smevGateXml, "XML",smevGateConverter );
        dynamicSmevView.add(smevGateConverter, "store externalTask<Json>",smevGateDb );
        dynamicSmevView.add(smevGateConverter, "store request<Xml>",smevGateDb );
        dynamicSmevView.add(smevGateConverter,"",smevGateClient );
        dynamicSmevView.add(smevGateClient,"Get() || Send(response_date)", smevAdapter );
        dynamicSmevView.add(smevAdapter,"response(request_date) || OK" ,smevGateClient );
        dynamicSmevView.add(smevGateClient,"store response<Xml>" ,smevGateDb );
        dynamicSmevView.add(smevGateClient, "return" , smevGateConverter );
        dynamicSmevView.add(smevGateConverter,"XmlToJson" ,smevGateXml );
        dynamicSmevView.add(smevGateXml, "Json",smevGateConverter );
        dynamicSmevView.add(smevGateConverter,"complete task" ,smevGateDb );
        dynamicSmevView.add(smevGateConverter, "return", smevWorker );
        dynamicSmevView.add(smevWorker,"compose().andThen()", smevCamundaClient);
        dynamicSmevView.add(smevCamundaClient, "complete()",externalTaskQueue );
*/
        ComponentView camundaView = views.createComponentView( camunda, "camView", "Оркестрация микросервисов" );
        camundaView.addDefaultElements();
        camundaView.remove( filebeat );
        camundaView.remove( microserviceBlueprint );
        camundaView.remove( apiGateway );


        ComponentView gatewayView = views.createComponentView( apiGateway, "apiGateway", "Оркестрация API" );
        gatewayView.setTitle( "Оркестрация API Gateway" );
        gatewayView.addDefaultElements();

        gatewayView.remove( microserviceBlueprint );
        gatewayView.remove( filebeat );
        gatewayView.remove( apiGateway.getComponentWithName( "Logback" ) );
        model.getRelationships().stream()
                .filter( r -> r.getSource().hasTag( WORKER_TAG ) && r.getDestination().equals( consul ) )
                .forEach( gatewayView::remove );
        model.getRelationships().stream()
                .filter( r -> r.getSource().getParent() != null )
                .filter( r -> !r.getSource().getParent().equals( apiGateway ) && r.getDestination().equals( keycloak ) )
                .forEach( gatewayView::remove );

        ImageView smevSeqView = views.createImageView( smevAdapter, "smevSeqView" );
        smevSeqView.setDescription( "Схема процесса асинхронного взаимодействия ИС УВ со СМЭВ 3 через интеграционный интерфейс web-сервис ИУА" );
        smevSeqView.setTitle( "Sequence диаграмма СМЭВ-Адаптер-Система" );
        smevSeqView.setContent( "src/main/java/ru/smuzee/core/c4/img/iua-seq.jpeg" );

        /*
        ImageView bpmLevelsView = views.createImageView( highLevelProcesses, "bpmLevelsView" );
        bpmLevelsView.setDescription( "Взаимодействие разных видов бизнес-процессов на примере процедуры получения охотбилета" );
        bpmLevelsView.setTitle( "Ансамбль процессов" );
        bpmLevelsView.setContent( "./src/main/resources/img/hunting-ticket-bpm.png" );
        */

        //Закодировать кaртинки
        for (ImageView iv : views.getImageViews()) {
            if (iv.getContent() != null) {
                File img = new File( iv.getContent() );
                try {
                    iv.setContent( ImageUtils.getImageAsDataUri( img ) );
                    iv.setContentType( ImageUtils.getContentType( img ) );
                } catch (IOException ignored) {

                }
            }
        }
       /* DefaultDocumentationImporter documentationImporter = new DefaultDocumentationImporter();
        DefaultImageImporter imageImporter = new DefaultImageImporter();

        File allDocs = new File( "src/main/java/ru/smuzee/core/c4/docs/workspace" );
        documentationImporter.importDocumentation( workspace, allDocs );
        imageImporter.importDocumentation( workspace, allDocs );*/

        StructurizrClient structurizrClient = new StructurizrClient( WORKSPACE_URL, API_KEY, API_SECRET );
        structurizrClient.setWorkspaceArchiveLocation( new File( "./archive" ) );
        structurizrClient.putWorkspace( WORKSPACE_ID, workspace );
    }
}
