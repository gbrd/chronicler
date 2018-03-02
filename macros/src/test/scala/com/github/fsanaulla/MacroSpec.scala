package com.github.fsanaulla

import com.github.fsanaulla.core.model.InfluxWriter
import com.github.fsanaulla.macros.InfluxFormatter
import com.github.fsanaulla.macros.annotations.{field, tag}
import org.scalatest.{FlatSpec, Matchers}

// Bug not related to scala version
class MacroSpec extends FlatSpec with Matchers {

  "Macros" should "generate writer with fully annotated fields" in {
    case class Test(@tag name: String,
                    @field age: Int)

    val wr: InfluxWriter[Test] = InfluxFormatter.writer[Test]

    wr.write(Test("tName", 65)) shouldEqual "name=tName age=65"
  }

  it should "generate writer for partially annotated fields" in {
    case class Test(@tag name: String,
                    age: Int)

    val wr: InfluxWriter[Test] = InfluxFormatter.writer[Test]

    wr.write(Test("tName", 65)) shouldEqual "name=tName"

  }
}
