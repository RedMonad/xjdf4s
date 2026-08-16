package xjdf4s.model

import xjdf4s.core.QualifiedName

private[model] object XjdfNames:
  val namespace: String = "http://www.CIP4.org/JDFSchema_2_0"
  def element(localName: String): QualifiedName = QualifiedName(namespace, localName)
end XjdfNames
