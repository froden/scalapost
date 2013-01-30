package no.froden.scalapost

import org.scalatest.FunSuite

class XmlTypesTest extends FunSuite{

  test("Links should extract list of tuples with rel and uri") {
    <links><link rel="create" uri="example.com" /><link rel="delete" uri="test2" /></links> match {
      case Links((rel, uri), rest @ _*) => {
        assert(rel === "create")
        assert(uri === "example.com")
        assert(rest === Seq(("delete", "test2")))
      }
    }
  }
}
