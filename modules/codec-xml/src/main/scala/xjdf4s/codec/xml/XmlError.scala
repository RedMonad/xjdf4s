package xjdf4s.codec.xml

import cats.Show

/** Decoding/parsing failure vocabulary of the XML codec. */
enum XmlError derives CanEqual:
  case Parse(line: Int, column: Int, message: String)
  case MissingAttribute(element: String, attribute: String)
  case InvalidAttribute(element: String, attribute: String, value: String, expected: String)
  case MissingElement(parent: String, element: String)
  case UnexpectedElement(parent: String, element: String)

  /** A standard XJDF element that the current codec coverage slice does not implement yet. */
  case UnsupportedElement(element: String)

  /** A standard XJDF name where a foreign-namespace name is required. */
  case ForeignNameExpected(element: String)
end XmlError

object XmlError:
  given Show[XmlError] = Show.show {
    case XmlError.Parse(line, column, message) =>
      s"line $line, column $column: $message"
    case XmlError.MissingAttribute(element, attribute) =>
      s"<$element>: missing required attribute @$attribute"
    case XmlError.InvalidAttribute(element, attribute, value, expected) =>
      s"<$element>: invalid @$attribute '$value' — $expected"
    case XmlError.MissingElement(parent, element) =>
      s"<$parent>: missing required child <$element>"
    case XmlError.UnexpectedElement(parent, element) =>
      s"<$parent>: unexpected child <$element>"
    case XmlError.UnsupportedElement(element) =>
      s"<$element>: standard XJDF element not covered by this codec slice yet"
    case XmlError.ForeignNameExpected(element) =>
      s"<$element>: standard XJDF name where a foreign name is required"
  }
end XmlError
