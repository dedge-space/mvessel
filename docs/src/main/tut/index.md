---
layout: home
technologies:
 - first: ["Scala", "We love <u>Scala</u> and we thought that the driver should use all the power of this language."]
 - second: ["Android", "Mainly intended for <u>Android</u>, it has been designed to support other underlying SQLite systems."]
 - third: ["Database", "Brings the capacity of all Scala and Java <u>Database</u> libraries to the Android platform."]
---

## Create a SQLiteOpenHelper
  Android comes with a helper class to manage the creation and version management of our database. This class is SQLiteOpenHelper. We can use this helper class to let Android platform create the database file for us and later access to that file through our JDBC driver or favorite ORM.

```scala
class ContactsOpenHelper(context: Context)
      extends SQLiteOpenHelper(context, "database.db", javaNull, 1) {
        def onCreate(db: SQLiteDatabase) {
          // Use this method to initialize your database
        }
        def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
          // Use this method to upgrade your database
        }
}
```

## Use java.sql classes
  If you want to work directly with the java.sql classes you could create a method in our SQLiteOpenHelper that returns the java.sql.Connection.

```scala
def openConnection() = {
      // 1. This will trigger the `onCreate` and `onUpgrade` methods.
      val database: SQLiteDatabase = getReadableDatabase()
      // 2. Register the driver, making available for `java.sql`
      com.fortysevendeg.mvessel.AndroidDriver.register()
      // 3. Open a connection using the path provided by the database
      DriverManager.getConnection("jdbc:sqlite:" + database.getPath)
}
```

## Use the Database class
  You can directly create a connection with the Database class provided in the library. Again in your SQLiteOpenHelper:

```scala
def openDatabase() = {
      // 1. This will trigger the `onCreate` and `onUpgrade` methods.
      val database: SQLiteDatabase = getReadableDatabase()
      // 2. Open the database using the path provided by the database
      new Database[AndroidCursor](
          databaseFactory = new AndroidDatabaseFactory,
          name = database.getPath,
          timeout = 50,
          retry = 0,
          flags = 0)
}
```