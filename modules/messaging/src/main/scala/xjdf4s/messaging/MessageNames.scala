package xjdf4s.messaging

import xjdf4s.core.QualifiedName
import xjdf4s.core.XjdfNamespace

private[messaging] object MessageNames:
  private val Namespace: String = XjdfNamespace.uri
  def element(localName: String): QualifiedName = QualifiedName(Namespace, localName)
end MessageNames
