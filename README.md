# Example API

This project provides an example of building an API using Kotlin and the Ktlib library. It can also be used
like a template repo where you clone it, wipe out the history, change the names and start your own project.
I'll go through the details of how it's laid out and the thinking behind each part.

## Overall Architecture
The main architecture followed by the project is Robert Martin's Clean Architecture. As shown here:

![Clean Architecture](https://blog.cleancoder.com/uncle-bob/images/2012-08-13-the-clean-architecture/CleanArchitecture.jpg)

As Martin notes in his writing on this, you may have additional layers added to the application. In this project
I've added a service layer between the Use Case and Entity layers that encapsulates reusable business logic that
does not belong in the Entity layer but is used by the Use Case layer.

## Layers

### Entities

The innermost layer is the Entities layer. This layer contains the data structures that are used by the application along
with business logic that is specific to those data structures. All entities are in the `entities` package which are
located in the `src/main/kotlin/entities` directory. This application is making use of the entity concept in ktlib-core
which allows us to define the entities, their logic and the data stores associated to them with no dependency on a
database or any sore of storage mechanism.

### Services
More to come...

### Use Cases
More to come...

### Adapters
More to come...

## Running the project

This project uses gradle wrapper and docker for running Postgres.

To run the Postgres DB, from a terminal, run:

```
docker-compose up
```  

To setup the DB or run migrations run:

```
gradlew migrate
```

To run tests you can run:

```
gradlew test
```

To create a migration run one of these:

```
gradlew createSqlMigration
gradlew createKotlinMigration
```

You can pass parameters to the migration by using the `-P` flag:

```
gradlew createSqlMigration -Pname=my_migration
```

The available options are:

* name - the name use on the file created
* repeatable - set to `true` will create a repeatable
  migration [Click here for more info](https://flywaydb.org/documentation/tutorials/repeatable)
* undo - makes an undo migration, the value passed to this option should be the number of the migration you are undoing
* baseline - set to `true` will create a baseline
  migration [Click here for more info](https://flywaydb.org/documentation/concepts/baselinemigrations)