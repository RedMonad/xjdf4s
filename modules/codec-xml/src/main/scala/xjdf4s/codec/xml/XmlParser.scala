package xjdf4s.codec.xml

import xjdf4s.core.{QualifiedName, XjdfNamespace}

/**
 * Minimal, dependency-free XML parser for XJDF/XJMF documents:
 *
 *  - `<?xml ...?>` prolog, comments and processing instructions are recognized and skipped;
 *  - CDATA sections are preserved as text;
 *  - named (`&amp;`, `&lt;`, ...) and numeric (`&#38;`, `&#x26;`) entity references are decoded;
 *  - namespace declarations (`xmlns`, `xmlns:prefix`) are resolved into `QualifiedName` and are not kept as attributes;
 *  - whitespace-only text nodes are dropped (canonical form);
 *  - when no default namespace is declared, the XJDF namespace is assumed (pragmatic convention for XJDF tooling);
 *  - DOCTYPE declarations are rejected.
 */
object XmlParser:

  private final class ParseAbort(val line: Int, val column: Int, val message: String) extends RuntimeException

  def parse(input: String): Either[XmlError, Xml.Element] =
    try Right(new Parser(input).parseDocument())
    catch
      case abort: ParseAbort => Left(XmlError.Parse(abort.line, abort.column, abort.message))

  private final class Parser(input: String):

    private var position = 0

    def parseDocument(): Xml.Element =
      skipMisc()
      if startsWith("<?xml") then skipUntil("?>")
      skipMisc()
      if startsWith("<!DOCTYPE") then abort("DOCTYPE declarations are not supported")
      val root = readElement(Map.empty)
      skipMisc()
      if position < input.length then abort("unexpected content after the root element")
      root

    private def abort(message: String): Nothing =
      val prefix = input.substring(0, math.min(position, input.length))
      val line = prefix.count(_ == '\n') + 1
      val column = position - prefix.lastIndexOf('\n')
      throw new ParseAbort(line, column, message)

    private def peek: Char =
      if position >= input.length then '\u0000' else input.charAt(position)

    private def startsWith(token: String): Boolean =
      input.startsWith(token, position)

    private def consume(token: String): Unit =
      if !startsWith(token) then abort(s"expected '$token'")
      position += token.length

    private def skipUntil(token: String): Unit =
      val end = input.indexOf(token, position)
      if end < 0 then abort(s"expected '$token'")
      position = end + token.length

    private def skipMisc(): Unit =
      var continue = true
      while continue do
        skipWhitespace()
        if startsWith("<!--") then skipUntil("-->")
        else if startsWith("<?") then skipUntil("?>")
        else continue = false

    private def skipWhitespace(): Unit =
      while position < input.length && input.charAt(position).isWhitespace do position += 1

    private def readQualifiedName(): (String, String) =
      val raw = readName()
      raw.split(":", 2) match
        case Array(prefix, local) => (prefix, local)
        case _                    => ("", raw)

    private def readName(): String =
      val start = position
      if position < input.length &&
          (input.charAt(position).isLetter || input.charAt(position) == '_' || input.charAt(position) == ':')
      then position += 1
      while position < input.length && (input.charAt(position).isLetterOrDigit || "._:-".contains(input.charAt(position))) do
        position += 1
      if position == start then abort("expected a name")
      input.substring(start, position)

    private def readElement(scopes: Map[String, String]): Xml.Element =
      consume("<")
      val (prefix, localName) = readQualifiedName()
      var currentScopes = scopes
      val attributes = Vector.newBuilder[(QualifiedName, String)]
      var selfClosing = false
      var done = false
      while !done do
        skipWhitespace()
        if startsWith("/>") then
          consume("/>")
          selfClosing = true
          done = true
        else if peek == '>' then
          position += 1
          done = true
        else
          val (attributePrefix, attributeLocal) = readQualifiedName()
          skipWhitespace()
          consume("=")
          skipWhitespace()
          val value = readQuotedValue()
          if attributePrefix.isEmpty && attributeLocal == "xmlns" then
            currentScopes = currentScopes.updated("", value)
          else if attributePrefix == "xmlns" then
            currentScopes = currentScopes.updated(attributeLocal, value)
          else
            val namespace =
              if attributePrefix.isEmpty then currentScopes.getOrElse("", "")
              else currentScopes.getOrElse(attributePrefix, "")
            attributes += (
              (
                QualifiedName(namespace, attributeLocal, Option.when(attributePrefix.nonEmpty)(attributePrefix)),
                value,
              )
            )
      val namespace =
        if prefix.isEmpty then currentScopes.getOrElse("", XjdfNamespace.uri)
        else currentScopes.getOrElse(prefix, "")
      val name = QualifiedName(namespace, localName, Option.when(prefix.nonEmpty)(prefix))
      if selfClosing then Xml.Element(name, attributes.result(), Vector.empty)
      else
        val children = readContent(currentScopes)
        consume("</")
        val (_, endLocal) = readQualifiedName()
        skipWhitespace()
        if peek == '>' then position += 1 else abort("expected '>'")
        if endLocal != localName then abort(s"mismatched closing tag: expected '</$localName>' but found '</$endLocal>'")
        Xml.Element(name, attributes.result(), children)

    private def readContent(scopes: Map[String, String]): Vector[Xml] =
      val children = Vector.newBuilder[Xml]
      var done = false
      while !done do
        if position >= input.length then abort("unexpected end of input inside an element")
        if startsWith("</") then done = true
        else if startsWith("<!--") then skipUntil("-->")
        else if startsWith("<![CDATA[") then children += Xml.Text(readCdata())
        else if startsWith("<?") then skipUntil("?>")
        else if peek == '<' then children += readElement(scopes)
        else
          val text = readText()
          if text.trim.nonEmpty then children += Xml.Text(text)
      children.result()

    private def readText(): String =
      val builder = new StringBuilder
      var done = false
      while !done do
        if position >= input.length then abort("unexpected end of input inside text content")
        val current = input.charAt(position)
        if current == '<' then done = true
        else if current == '&' then builder.append(readEntityReference())
        else
          builder.append(current)
          position += 1
      builder.result()

    private def readCdata(): String =
      consume("<![CDATA[")
      val end = input.indexOf("]]>", position)
      if end < 0 then abort("unterminated CDATA section")
      val value = input.substring(position, end)
      position = end + 3
      value

    private def readQuotedValue(): String =
      val quote = peek
      if quote != '"' && quote != '\'' then abort("expected a quoted attribute value")
      position += 1
      val builder = new StringBuilder
      var done = false
      while !done do
        if position >= input.length then abort("unterminated attribute value")
        val current = input.charAt(position)
        if current == quote then
          position += 1
          done = true
        else if current == '&' then builder.append(readEntityReference())
        else
          builder.append(current)
          position += 1
      builder.result()

    private val NamedEntities: Map[String, Char] =
      Map("amp" -> '&', "lt" -> '<', "gt" -> '>', "quot" -> '"', "apos" -> '\'')

    private def readEntityReference(): String =
      consume("&")
      val end = input.indexOf(';', position)
      if end < 0 then abort("unterminated entity reference")
      val body = input.substring(position, end)
      position = end + 1
      if body.startsWith("#x") || body.startsWith("#X") then
        try Integer.parseInt(body.substring(2), 16).toChar.toString
        catch case _: NumberFormatException => abort(s"invalid numeric entity reference '&$body;'")
      else if body.startsWith("#") then
        try body.substring(1).toInt.toChar.toString
        catch case _: NumberFormatException => abort(s"invalid numeric entity reference '&$body;'")
      else NamedEntities.getOrElse(body, abort(s"unknown entity reference '&$body;'")).toString
  end Parser
end XmlParser
