package org.fusesource.scalate.japi;

import scala.jdk.javaapi.CollectionConverters;
import scala.collection.mutable.Buffer;
import java.util.List;

final class Converter {
  private Converter() {
  }

  static <A> Buffer<A> asScalaBuffer(List<A> list) {
    return CollectionConverters.asScala(list);
  }

  static <A, B> scala.collection.mutable.Map<A, B> mapAsScalaMap(java.util.Map<A, B> map) {
    return CollectionConverters.asScala(map);
  }
}
