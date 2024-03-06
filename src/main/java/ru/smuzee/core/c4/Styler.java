package ru.smuzee.core.c4;

import com.structurizr.model.Tags;
import com.structurizr.util.ImageUtils;
import com.structurizr.view.*;

import java.io.File;
import java.io.IOException;

import static ru.smuzee.core.c4.SmuzeeTags.*;

public class Styler {
    static final String GREY = "#8D98A7";
    static final String BLUE3 = "#003DA7";
    static final String BLUE2 = "#3B69B7";
    static final String BLUE1 = "#94AAD1";
    static final String ORANGE3 = "#F7642C";
    static final String ORANGE2 = "#F77E25";
    static final String ORANGE1 = "#F8981D";
    static final String ORANGE0 = "#F8C684";
    static final String CAMUNDA = "#FC5D0D";
    static final String SPRING = "#6EB23F";

    public static Branding brandBook(ViewSet views) throws IOException {
        Branding branding = views.getConfiguration().getBranding();
        File logo = new File("src/main/java/ru/smuzee/core/c4/img/smuzee.png");
        //File logo = new File( "src/main/java/ru/smuzee/core/c4/icons/fox-horizontal-logo.png" );
        branding.setLogo( ImageUtils.getImageAsDataUri( logo ) );
        branding.setFont( new Font( "Bodoni", "https://db.onlinewebfonts.com/c/8682b980e7f3168a4720fc220dc7c896?family=PT+Bodoni+Cyrillic" ) );
        return branding;
    }

    public static Styles beautify(ViewSet views) {

        Styles styles = views.getConfiguration().getStyles();
        //Связи
        styles.addRelationshipStyle( Tags.RELATIONSHIP )
                .thickness( 3 )
                .style( LineStyle.Solid )
                .routing( Routing.Direct )
                .fontSize( 20 );
        styles.addRelationshipStyle( HTTP_REQUEST_TAG )
                .color( BLUE3 );
        styles.addRelationshipStyle( LEAP_INTERACTION_TAG )
                .thickness( 2 )
                .style( LineStyle.Dashed );
        styles.addRelationshipStyle( Tags.ASYNCHRONOUS )
                .style( LineStyle.Dashed );
        styles.addRelationshipStyle( SAFE_REQUEST_TAG )
                .thickness( 1 )
                .routing( Routing.Curved )
                .color( ORANGE1 );
        styles.addRelationshipStyle( SERVICE_REGISTRATION_TAG )
                .thickness( 2 )
                .style( LineStyle.Dotted )
                .color( BLUE2 );

        styles.addElementStyle( Tags.ELEMENT ).color( "white" ).strokeWidth( 8 );
        styles.addElementStyle( "Group" ).fontSize( 45 ).color( ORANGE2 );
        styles.addElementStyle( "Boundary" ).fontSize( 60 ).strokeWidth( 3 ).metadata( false );

        //Элементы первого уровня
        styles.addElementStyle( Tags.PERSON ).shape( Shape.Person ).metadata( false )
                .width( 350 )
                .background( "white" )
                .strokeWidth( 5 )
                .stroke( ORANGE2 )
                .fontSize( 17 )
                .color( BLUE3 );
        styles.addElementStyle( "Operator" ).icon( "src/main/java/ru/smuzee/core/c4/icons/oper.png" );
        styles.addElementStyle( "Analyst" ).icon( "src/main/java/ru/smuzee/core/c4/icons/anal.png" );
        styles.addElementStyle( "Administrator" ).icon( "src/main/java/ru/smuzee/core/c4/icons/devops.png" );
        styles.addElementStyle( "Security" ).icon( "src/main/java/ru/smuzee/core/c4/icons/seq.png" );


        styles.addElementStyle( Tags.SOFTWARE_SYSTEM ).shape( Shape.RoundedBox )
                .width( 450 )
                .background( "white" )
                .strokeWidth( 12 )
                .stroke( BLUE3 )
                .fontSize( 28 )
                .color( BLUE3 );
        styles.addElementStyle( "Hunt" ).icon( "src/main/java/ru/smuzee/core/c4/icons/hunt-logo.png" )
                .strokeWidth( 14 )
                .stroke( ORANGE3 );
        styles.addElementStyle( "SMEV" ).icon( "src/main/java/ru/smuzee/core/c4/icons/smev.png" );
        styles.addElementStyle( "Kibana" ).icon( "src/main/java/ru/smuzee/core/c4/icons/kibana.png" );
        styles.addElementStyle( "Consul" ).icon( "src/main/java/ru/smuzee/core/c4/icons/consul.png" );
        styles.addElementStyle( "Keycloak" ).icon( "src/main/java/ru/smuzee/core/c4/icons/keycloak.png" );

        //Элементы второго уровня
        styles.addElementStyle( Tags.CONTAINER ).shape( Shape.RoundedBox )
                .width( 400 )
                .height( 250 )
                .background( BLUE2 )
                .strokeWidth( 8 )
                //.stroke(auto)
                .fontSize( 24 )
                .color( "white" )
                .icon( null );
        styles.addElementStyle( WEB_BROWSER_TAG ).shape( Shape.WebBrowser );
        styles.addElementStyle( DATABASE_TAG ).shape( Shape.Cylinder )
                .width( 300 )
                .height( 300 )
                .background( GREY );
        styles.addElementStyle( ORCHESTRATOR_TAG ).shape( Shape.RoundedBox )
                .width( 1000 )
                .height( 200 );
        styles.addElementStyle( "Redis" ).icon( "src/main/java/ru/smuzee/core/c4/icons/redis.png" )
                .background( "#DC382C" );
        styles.addElementStyle( POSTGRESQL_TAG ).icon( "src/main/java/ru/smuzee/core/c4/icons/postgresql.png" )
                .background( "#336791" );
        styles.addElementStyle( "eXistDB" ).icon( "src/main/java/ru/smuzee/core/c4/icons/existdb.png" )
                .background( "#4c4b4d" );
        styles.addElementStyle( "Camunda" )
                .background( CAMUNDA );
        styles.addElementStyle( SPRING_CLOUD_TAG )
                .background( SPRING );
        styles.addElementStyle( MICROSERVICE_TAG ).shape( Shape.Hexagon )
                .width( 350 );

        //Элементы третьего уровня
        styles.addElementStyle( Tags.COMPONENT ).shape( Shape.Box ).metadata( false )
                .width( 400 )
                .height( 150 )
                .background( BLUE1 )
                .strokeWidth( 4 )
                //.stroke(auto)
                .fontSize( 16 )
                .color( "white" );
        styles.addElementStyle( QUEUE_TAG ).shape( Shape.Pipe );
        styles.addElementStyle( TINY_TAG )
                .width( 320 )
                .height( 100 )
                .strokeWidth( 1 );
        styles.addElementStyle( API_TAG ).shape( Shape.Circle )
                .width( 200 );
        //Закодировать иконки
        for (ElementStyle es : styles.getElements()) {
            if (es.getIcon() != null) {
                File icon = new File( es.getIcon() );
                try {
                    es.setIcon( ImageUtils.getImageAsDataUri( icon ) );
                } catch (IOException ignored) {

                }
            }
        }
        return styles;
    }
}
