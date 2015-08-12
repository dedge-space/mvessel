package com.fortysevendeg.mvessel

import java.sql.{SQLException, SQLFeatureNotSupportedException}
import java.util.Properties

import com.fortysevendeg.mvessel.util.ConnectionValues
import org.specs2.matcher.Scope
import org.specs2.mutable.Specification

trait DriverSpecification
  extends Specification {

  val name = "database.db"

  val validUrl = s"jdbc:sqlite:$name"

  val invalidPrefixUrl = s"jdbc:oracle:$name"

  val invalidUrl = s"urlNotValid"

  val properties = new Properties()

  trait DriverScope extends Scope

  trait WithConnectionValues extends DriverScope {

    val timeout = 1

    val retry = 5

    val connectionValues = ConnectionValues(name, Map("timeout" -> timeout.toString, "retry" -> retry.toString))

    val driver = new Driver {

      override def parseConnectionString(connectionString: String): Option[ConnectionValues] = Some(connectionValues)
    }

  }

  trait WithoutConnectionValues extends DriverScope {

    val driver = new Driver {

      override def parseConnectionString(connectionString: String): Option[ConnectionValues] = None
    }

  }

}

class DriverSpec
  extends DriverSpecification {

  "acceptsURL" should {

    "return true for a valid connection URL" in new WithConnectionValues {
      driver.acceptsURL(validUrl) must beTrue
    }

    "return false for a valid connection URL with other DB engine prefix" in new WithConnectionValues {
      driver.acceptsURL(invalidPrefixUrl) must beFalse
    }

    "return false for a invalid connection URL" in new WithConnectionValues {
      driver.acceptsURL(invalidUrl) must beFalse
    }

  }

  "jdbcCompliant" should {

    "return false" in new WithConnectionValues {
      driver.jdbcCompliant must beFalse
    }

  }

  "getPropertyInfo" should {

    "return an empty array" in new WithConnectionValues {
      driver.getPropertyInfo(validUrl, properties) must beEmpty
    }

  }

  "getMinorVersion" should {

    "return 0" in new WithConnectionValues {
      driver.getMinorVersion shouldEqual 0
    }

  }

  "getMajorVersion" should {

    "return 1" in new WithConnectionValues {
      driver.getMajorVersion shouldEqual 1
    }

  }

  "getParentLogger" should {

    "throw a SQLFeatureNotSupportedException" in new WithConnectionValues {
      driver.getParentLogger must throwA[SQLFeatureNotSupportedException]
    }

  }

  "connect" should {

    "create a Connection with the params obtained by the ConnectionStringParser" in
      new WithConnectionValues {
        driver.connect(validUrl, properties) must beLike {
          case c: Connection =>
            c.databaseName shouldEqual name
            c.timeout shouldEqual timeout
            c.retryInterval shouldEqual retry
        }
      }

    "throws a SQLException when the URL can't be parsed" in
      new WithoutConnectionValues {
        driver.connect(validUrl, properties) must throwA[SQLException]
      }
  }

}
