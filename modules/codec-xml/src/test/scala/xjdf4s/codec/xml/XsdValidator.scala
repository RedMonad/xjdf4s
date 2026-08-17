package xjdf4s.codec.xml

import java.io.StringReader

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal
import org.xml.sax.{ErrorHandler, SAXParseException}

/**
 * Validates emitted XML against the checked-in XJDF schema (`src/test/resources/xjdf.xsd`, a copy of the XSD
 * shipped with the specification). The JDK JAXP validator is used, so no additional test dependency is required.
 */
object XsdValidator:

  lazy val schema: Schema =
    val resource = Option(getClass.getResourceAsStream("/xjdf.xsd"))
      .getOrElse(throw new IllegalStateException("xjdf.xsd not found on the test classpath"))
    SchemaFactory
      .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
      .newSchema(new StreamSource(resource))

  def validate(xml: String): Either[String, Unit] =
    val problems = new ArrayBuffer[String]
    val validator = schema.newValidator()
    validator.setErrorHandler(new ErrorHandler:
      def warning(exception: SAXParseException): Unit = problems += s"warning: ${exception.getMessage}"
      def error(exception: SAXParseException): Unit = problems += s"error: ${exception.getMessage}"
      def fatalError(exception: SAXParseException): Unit = problems += s"fatal: ${exception.getMessage}"
    )
    try
      validator.validate(new StreamSource(new StringReader(xml)))
      if problems.isEmpty then Right(()) else Left(problems.mkString("; "))
    catch
      case NonFatal(exception) => Left(s"${exception.getClass.getSimpleName}: ${exception.getMessage}")
end XsdValidator
