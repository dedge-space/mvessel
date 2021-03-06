/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2012 47 Degrees, LLC http://47deg.com hello@47deg.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 */

package com.fortysevendeg.mvessel.util

import java.sql.{Connection, SQLException, Statement}

import scala.util.{Failure, Success, Try}

object DatabaseUtils {

  object WrapSQLException {

    def apply[D, T](option: Option[D], message: String)(f: D => T) = option match {
      case Some(db) =>
        Try(f(db)) match {
          case Success(r) => r
          case Failure(e) => throw new SQLException(e)
        }
      case None =>
        throw new SQLException(message)
    }

  }

  object WithStatement {

    def apply[T](f: Statement => T)(implicit connection: Connection): T = {
      val statement = connection.createStatement()
      Try(f(statement)) match {
        case Success(t) =>
          closeStatement(statement)
          t
        case Failure(e) =>
          closeStatement(statement)
          throw e
      }
    }

    private[this] def closeStatement(statement: Statement) {
      Try(statement.close())
    }

  }

}
