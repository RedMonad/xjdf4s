package xjdf4s.messaging

import xjdf4s.core.QualifiedName

private[messaging] object MessageNames:
  private val Namespace = "http://www.CIP4.org/JDFSchema_2_0"
  def element(localName: String): QualifiedName = QualifiedName(Namespace, localName)
end MessageNames
