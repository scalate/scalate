package org.fusesource.scalate.japi;

import scala.collection.JavaConverters;
import scala.collection.mutable.Buffer;
import java.util.List;

final class Converter {
  private Converter() {
  }

  static <A> Buffer<A> asScalaBuffer(List<A> list) {
    return JavaConverters.asScalaBuffer(list);
  }

  static <A, B> scala.collection.mutable.Map<A, B> mapAsScalaMap(java.util.Map<A, B> map) {
    return JavaConverters.mapAsScalaMap(map);
  }
}
