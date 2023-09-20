# base-kotlin

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