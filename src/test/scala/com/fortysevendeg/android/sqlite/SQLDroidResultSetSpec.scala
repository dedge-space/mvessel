package com.fortysevendeg.android.sqlite

import java.sql.{ResultSetMetaData, ResultSet, SQLException}

import android.database.{MatrixCursor, Cursor}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.util.Random

trait SQLDroidResultSetSpecification
  extends Specification
  with Mockito {

  trait WithCursorMocked
    extends Scope {

    val cursor = mock[Cursor]

    val sqlDroid = new SQLDroidResultSet(cursor, new TestLogWrapper)

  }

  trait WithMatrixCursor
    extends Scope {

    val columnNames = Array("column1", "column2", "column3", "column4", "column5")

    val columnTypes = Array(Cursor.FIELD_TYPE_STRING, Cursor.FIELD_TYPE_INTEGER, Cursor.FIELD_TYPE_FLOAT, Cursor.FIELD_TYPE_BLOB, Cursor.FIELD_TYPE_NULL)

    val cursor = new MatrixCursor(columnNames, columnTypes)

    val sqlDroid = new SQLDroidResultSet(cursor, new TestLogWrapper)

  }

  trait WithData {

    self: WithMatrixCursor =>

    val numRows = 10

    1 to numRows foreach { _ =>
      cursor.addRow(Array(
        Random.nextString(10),
        new java.lang.Integer(Random.nextInt(10)),
        new java.lang.Float(Random.nextFloat() * Random.nextInt(10)),
        Random.nextString(10),
        javaNull))
    }
  }

}

class SQLDroidResultSetSpec
  extends SQLDroidResultSetSpecification {

  "isBeforeFirst" should {

    "return true if the cursor is before to the first" in new WithMatrixCursor with WithData {
      sqlDroid.isBeforeFirst must beTrue
    }

    "return false if the cursor is in the first position" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      sqlDroid.isBeforeFirst must beFalse
    }

    "return false if the cursor is after last" in new WithMatrixCursor with WithData {
      cursor.moveToLast()
      cursor.moveToNext()
      sqlDroid.isBeforeFirst must beFalse
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.isBeforeFirst must throwA[SQLException]
    }
  }

  "isAfterLast" should {

    "return false if the cursor is before to the first" in new WithMatrixCursor with WithData {
      sqlDroid.isAfterLast must beFalse
    }

    "return false if the cursor is in the first position" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      sqlDroid.isAfterLast must beFalse
    }

    "return true if the cursor is after last" in new WithMatrixCursor with WithData {
      cursor.moveToLast()
      cursor.moveToNext()
      sqlDroid.isAfterLast must beTrue
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.isAfterLast must throwA[SQLException]
    }

  }

  "isFirst" should {

    "return false if the cursor is before to the first" in new WithMatrixCursor with WithData {
      sqlDroid.isFirst must beFalse
    }

    "return true if the cursor is in the first position" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      sqlDroid.isFirst must beTrue
    }

    "return true if the cursor is in the second position" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      cursor.moveToNext()
      sqlDroid.isFirst must beFalse
    }

    "return false if the cursor is after last" in new WithMatrixCursor with WithData {
      cursor.moveToLast()
      cursor.moveToNext()
      sqlDroid.isFirst must beFalse
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.isFirst must throwA[SQLException]
    }
  }

  "isLast" should {

    "return false if the cursor is before to the first" in new WithMatrixCursor with WithData {
      sqlDroid.isLast must beFalse
    }

    "return false if the cursor is in the first position" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      sqlDroid.isLast must beFalse
    }

    "return true if the cursor is in the last position" in new WithMatrixCursor with WithData {
      cursor.moveToLast()
      sqlDroid.isLast must beTrue
    }

    "return false if the cursor is after last" in new WithMatrixCursor with WithData {
      cursor.moveToLast()
      cursor.moveToNext()
      sqlDroid.isLast must beFalse
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.isLast must throwA[SQLException]
    }
  }

  "absolute" should {

    "return false for an empty cursor" in new WithMatrixCursor {
      sqlDroid.absolute(1) must beFalse
    }

    "return true and move before first when passing 0" in new WithMatrixCursor with WithData {
      sqlDroid.absolute(0) must beTrue
      cursor.isBeforeFirst must beTrue
    }

    "return false when move to a position greater than number of rows" in new WithMatrixCursor with WithData {
      sqlDroid.absolute(numRows + 1) must beFalse
    }

    "return true and move to the first position when passing 1" in new WithMatrixCursor with WithData {
      sqlDroid.absolute(1) must beTrue
      cursor.isFirst must beTrue
    }

    "return false when move to a negative position whose absolute value is greater than number of rows" in new WithMatrixCursor with WithData {
      sqlDroid.absolute(-numRows - 1) must beFalse
    }

    "return true and move to the last position when passing -1" in new WithMatrixCursor with WithData {
      sqlDroid.absolute(-1) must beTrue
      cursor.isLast must beTrue
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.absolute(0) must throwA[SQLException]
    }

  }

  "relative" should {

    "return false for an empty cursor" in new WithMatrixCursor {
      sqlDroid.relative(1) must beFalse
    }

    "return true and move before first when passing -1" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      sqlDroid.relative(-1) must beTrue
      cursor.isBeforeFirst must beTrue
    }

    "return false when move to a position greater than number of rows" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      sqlDroid.relative(numRows + 1) must beFalse
    }

    "return true and move to the first position when passing 1" in new WithMatrixCursor with WithData {
      sqlDroid.relative(1) must beTrue
      cursor.isFirst must beTrue
    }

    "return false when move to a negative position whose absolute value is greater than number of rows" in new WithMatrixCursor with WithData {
      sqlDroid.relative(-numRows - 1) must beFalse
    }

    "return true and move to the previous position when passing -1" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      cursor.moveToNext()
      val position = cursor.getPosition
      cursor.moveToNext()
      sqlDroid.relative(-1) must beTrue
      cursor.getPosition shouldEqual position
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.relative(0) must throwA[SQLException]
    }

  }

  "beforeFirst" should {

    "change the position to -1 with an empty cursor" in new WithMatrixCursor {
      sqlDroid.beforeFirst()
      cursor.getPosition shouldEqual -1
    }

    "change the position to -1 if the cursor is before to the first" in new WithMatrixCursor with WithData {
      sqlDroid.beforeFirst()
      cursor.getPosition shouldEqual -1
    }

    "change the position to -1 if the cursor is in the first position" in new WithMatrixCursor {
      cursor.moveToNext()
      sqlDroid.beforeFirst()
      cursor.getPosition shouldEqual -1
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.beforeFirst() must throwA[SQLException]
    }

  }

  "afterLast" should {

    "don't change the position with an empty cursor" in new WithMatrixCursor {
      val position = cursor.getPosition
      sqlDroid.afterLast()
      cursor.getPosition shouldEqual position
    }

    "change the position to num rows with a cursor with data" in new WithMatrixCursor with WithData {
      sqlDroid.afterLast()
      cursor.getPosition shouldEqual numRows
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.afterLast() must throwA[SQLException]
    }

  }

  "next" should {

    "return true and increment position by one with a cursor with data" in new WithMatrixCursor with WithData {
      val position = cursor.getPosition
      sqlDroid.next() must beTrue
      cursor.getPosition shouldEqual (position + 1)
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.next() must throwA[SQLException]
    }

  }

  "previous" should {

    "return true and decrement position by one with a cursor with data" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      val position = cursor.getPosition
      sqlDroid.previous() must beTrue
      cursor.getPosition shouldEqual (position - 1)
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.previous() must throwA[SQLException]
    }

  }

  "last" should {

    "return true and don't change the position with an empty cursor" in new WithMatrixCursor {
      val position = cursor.getPosition
      sqlDroid.last() must beFalse
      cursor.getPosition shouldEqual position
    }

    "change the position to num rows minus 1 with a cursor with data" in new WithMatrixCursor with WithData {
      sqlDroid.last()
      cursor.getPosition shouldEqual numRows - 1
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.last() must throwA[SQLException]
    }

  }

  "first" should {

    "return true and don't change the position with an empty cursor" in new WithMatrixCursor {
      val position = cursor.getPosition
      sqlDroid.first() must beFalse
      cursor.getPosition shouldEqual position
    }

    "change the position to 0 with a cursor with data" in new WithMatrixCursor with WithData {
      sqlDroid.first()
      cursor.getPosition shouldEqual 0
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.first() must throwA[SQLException]
    }

  }

  "getRow" should {

    "return position plus 1" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      val position = cursor.getPosition
      sqlDroid.getRow shouldEqual position + 1
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.getRow must throwA[SQLException]
    }

  }

  "findColumn" should {

    "return the column position for an existing one" in new WithMatrixCursor {
      sqlDroid.findColumn(columnNames(1)) shouldEqual 1
    }

    "throws SQLException if the cursor is closed" in new WithMatrixCursor with WithData {
      cursor.close()
      sqlDroid.findColumn(columnNames(1)) must throwA[SQLException]
    }

  }

  "close" should {

    "call close on cursor" in new WithCursorMocked {
      sqlDroid.close()
      there was one(cursor).close()
    }

  }

  "isClosed" should {

    "return true if close method in cursor return true" in new WithCursorMocked {
      cursor.isClosed returns true
      sqlDroid.isClosed must beTrue
    }

    "return false if close method in cursor return false" in new WithCursorMocked {
      cursor.isClosed returns false
      sqlDroid.isClosed must beFalse
    }

  }

  "wasNull" should {

    "return true if the last fetch value was null" in new WithMatrixCursor {
      cursor.addRow(Array(
        javaNull,
        new java.lang.Integer(Random.nextInt(10)),
        new java.lang.Float(Random.nextFloat() * Random.nextInt(10)),
        Random.nextString(10),
        javaNull))
      cursor.moveToNext()
      sqlDroid.getString(1)
      sqlDroid.wasNull() must beTrue
    }

    "return false if the last fetch value wasn't null" in new WithMatrixCursor with WithData {
      cursor.moveToNext()
      sqlDroid.getString(1)
      sqlDroid.wasNull() must beFalse
    }

  }

  "getType" should {

    "return ResultSet.TYPE_SCROLL_SENSITIVE" in new WithCursorMocked {
      sqlDroid.getType shouldEqual ResultSet.TYPE_SCROLL_SENSITIVE
    }

  }

  "getMetaData" should {

    "return a ResultSetMetaData" in new WithCursorMocked {
      sqlDroid.getMetaData must beAnInstanceOf[ResultSetMetaData]
    }

  }

}
