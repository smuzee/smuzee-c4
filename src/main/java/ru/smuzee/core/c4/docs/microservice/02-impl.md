## Реализация Воркера

**Воркер** – в нашей системе, это вид микросервиса, который выполняет внешние задачи BPM-процессов. Совсем не
обязательно, что абсолютно все задачи у нас будут проходить через Camunda

С точки зрения программного кода, за функционал воркера отвечает соответствующая библиотека. Что создать новый воркер нужно проделать несколько простых шагов.

> [!IMPORTANT]
>
> На текущий момент исходный код библиотеки представлен в качестве прототипа, и в этом плане будет претерпевать
> существенные изменения. Концепция же останется прежней.

### 1. Создать SpringBootApplication

*POM.xml*:

```xml
<parent>
	<groupId>com.hunt</groupId>
    <artifactId>hunt-spring-cloud-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <relativePath/>
</parent>
...
<dependency>
    <groupId>com.hunt</groupId>
    <artifactId>hunt-camunda-worker</artifactId>
    <version>${project.version}</version>
</dependency>
```

Минимальный *application.properties*:

```properties
server.port=8090
spring.application.name=hunter
hunt.worker.baseurl=http://localhost:8080/engine-rest 
hunt.worker.workerId=${spring.application.name}
spring.cloud.consul.host=localhost
spring.cloud.consul.port=8500
```

### 2. Создать сущность

- Класс сущности, управляемый через **Camunda** должен реализовать интерфейс `ProcessEntity` (не переживайте, он пустой)
- В любом удобном месте кода (доступном `@ComponentScan`) реализуйте нужный вам по методу функциональный интерфейс класса `Worker<ProcessEntity>`
- Реализованную функцию выпустите бином с аннотацией `@EnableProcessEntity(YourEntity.class)`

Пример:

```java
@Bean
@Transactional
@EnableProcessEntity(Hunter.class)
public Put<Hunter> saveHunter(){
    return hunterRepository::save;
}

@Bean
@EnableProcessEntity(Hunter.class)
public Get<Hunter> findHunter(){
    return entity -> hunterRepository.findByPassportNoIgnoreCase(entity.getPassportNo()) ;
}
```

Объявленный бин создает подписку на топик, а реализованная функция вычисляется при завершении задачи



