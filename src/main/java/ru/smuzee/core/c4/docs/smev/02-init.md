## Постановка

### Аналитическая часть

#### Форматы данных

В первую очередь нужно сформировать все виды запросов/сообщений, которые нам потребуются (см. [Границы MVP](https://structurizr.kbinform.ru/workspace/2/documentation#%D1%84%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%BE%D0%BD%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B5-%D1%82%D1%80%D0%B5%D0%B1%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F)).

|      | put(Json var)<br />Переменная из ExternalTask | MessageQuery<br />Сообщение в адаптер | QueryResult<br />Ответ адаптера | complete(Json var)<br />Ответ Камунде |
|------|-----------------------------------------------|---------------------------------------|---------------------------------|---------------------------------------|
| Get  | —                                             | —                                     | `request_business_data`         | ???                                   |
| Send | ???                                           | `response_business_data`              | OK?                             | OK?                                   |

Каждая ячейка *содержит* схему (JSON Schema, WSDL) и максимальный пример. Подобного рода инфа нужна для каждого типа запроса.

#### Уточнить workflow и структуру приложения

![](embed:smevFlow)
Исходя из большего понимания процессов предметной области, в том числе в части подготовки покетов обмена, просьба критически отнестись к текущей схеме и при наличии таковых внести корректировки

### Разработка

На текущий момент работаем в рамках репозитория [прототипа](https://gitlab.com/kbinform/hunt-prototype)

1. Создать новый модуль в проекте
2. Унаследовать от POM:

```xml
<parent>
	<groupId>com.hunt</groupId>
	<artifactId>hunt-spring-cloud-parent</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<relativePath/>
</parent>
```

3. Добавить зависимость

```xml
<dependency>
	<groupId>com.hunt</groupId>
	<artifactId>hunt-camunda-worker</artifactId>
	<version>${project.version}</version>
</dependency>
```

4. Реализовать воркер (в том объеме воркфлоу, который будет на тот момент ясен). Допинфа о [воркерах](https://structurizr.kbinform.ru/workspace/2/documentation/Hunt/Microservice%20Archetype#%D1%80%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F-%D0%B2%D0%BE%D1%80%D0%BA%D0%B5%D1%80%D0%B0)

#### Стоит учесть

- С адаптером работаем по REST
- Логирование пока, еще код очень сырой реализовывать не надо, а вот [Consul](https://structurizr.kbinform.ru/workspace/2/documentation/Hunt/Consul) – очень даже.
- API на этапе MVP этому сервису вряд ли понадобится

### Исследование

- Сейчас в модели база данных не определена. Предлагаются [eXist-DB](https://exist-db.org/) и [BaseX](https://basex.org/). С учетом наших задач, объема и потока данных, надо прикинуть оптимальный вариант (может и PostgreSQL сгодится)
  
- XML-мастерскую сначала стоит прикинуть в целом на горизонт (общий объем задач) в части архитектуры классов. Будет хорошо если найдутся в старом коде кейсы показательные, обработки XML

