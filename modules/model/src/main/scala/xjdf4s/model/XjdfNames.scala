package xjdf4s.model

import xjdf4s.core.QualifiedName
import xjdf4s.core.XjdfNamespace

private[model] object XjdfNames:
  val namespace: String = XjdfNamespace.uri
  def element(localName: String): QualifiedName = QualifiedName(namespace, localName)
end XjdfNames
