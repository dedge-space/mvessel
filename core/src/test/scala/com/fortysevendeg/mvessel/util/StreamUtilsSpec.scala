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

import java.io.{ByteArrayInputStream, Closeable, StringReader}

import com.fortysevendeg.mvessel.util.StreamUtils._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class StreamUtilsSpecification
  extends Specification
  with Mockito {

  val string = "Sample String"

  class InputStreamScope
    extends Scope {

    val bytes = string.getBytes

    val inputStream = new ByteArrayInputStream(bytes)

    val emptyInputStream = new ByteArrayInputStream(Array.empty)

  }

  class ReaderScope
    extends Scope {

    val reader = new StringReader(string)

    val emptyReader = new StringReader("")

  }

  class CloseableScope
    extends Scope {

    val closeable = mock[Closeable]

  }

}

class StreamUtilsSpec
  extends StreamUtilsSpecification {

  "inputStreamToByteArray" should {

    "return the byte array when passing a InputStream" in
      new InputStreamScope {
        inputStreamToByteArray(inputStream) shouldEqual bytes
      }

    "return the byte array when passing a InputStream and a max length greater than the byte array length" in
      new InputStreamScope {
        inputStreamToByteArray(inputStream, Int.MaxValue) shouldEqual bytes
      }

    "return the byte array sliced when passing a InputStream and a max length" in
      new InputStreamScope {
        val maxLength = 2
        inputStreamToByteArray(inputStream, maxLength) shouldEqual bytes.slice(0, maxLength)
      }

    "return an empty byte array when passing an empty InputStream" in
      new InputStreamScope {
        inputStreamToByteArray(emptyInputStream, Int.MaxValue) shouldEqual Array.empty
      }

  }

  "readerToString" should {

    "return the string when passing a Reader" in
      new ReaderScope {
        readerToString(reader) shouldEqual string
      }

    "return the string when passing a Reader and a max length greater than the string" in
      new ReaderScope {
        readerToString(reader, Int.MaxValue) shouldEqual string
      }

    "return the string sliced when passing a Reader and a max length" in
      new ReaderScope {
        val maxLength = 2
        readerToString(reader, maxLength) shouldEqual string.substring(0, maxLength)
      }

    "return an empty byte array when passing an empty Reader" in
      new ReaderScope {
        readerToString(emptyReader) shouldEqual ""
      }
  }

  "withResource" should {

    "call close on resource when the function executes normally" in
      new CloseableScope {
        withResource(closeable)(_ => ())
        there was one(closeable).close
      }

    "call close on resource when the function throws an exception" in
      new CloseableScope {
        val msg = "Error"
        def f(c: Closeable): String = throw new RuntimeException(msg)
        withResource(closeable)(f) must throwA[RuntimeException](msg)
        there was one(closeable).close
      }

  }

}
