package xjdf4s.model.resources

import xjdf4s.core.*
import xjdf4s.model.*

enum JdfVersion(val lexical: String):
  case V1_1 extends JdfVersion("1.1")
  case V1_2 extends JdfVersion("1.2")
  case V1_3 extends JdfVersion("1.3")
  case V1_4 extends JdfVersion("1.4")
  case V1_5 extends JdfVersion("1.5")
  case V1_6 extends JdfVersion("1.6")
  case V1_7 extends JdfVersion("1.7")
  case V2_0 extends JdfVersion("2.0")
  case V2_1 extends JdfVersion("2.1")
  case V2_2 extends JdfVersion("2.2")
end JdfVersion

enum DevicePackaging derives CanEqual:
  case XML, Zip
end DevicePackaging

final case class DeviceIcon(
    bitDepth: Int,
    size: XYPair,
    usage: Vector[DeviceStatus] = Vector.empty,
    fileSpec: Option[FileSpec] = None,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class IconList(
    icons: NonEmptyVector[DeviceIcon],
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class DeviceModule(
    moduleId: Nmtoken,
    descriptiveName: Option[String] = None,
    manufacturer: Option[String] = None,
    manufacturerUrl: Option[UriRef] = None,
    moduleTypes: Vector[Nmtoken] = Vector.empty,
    revision: Option[String] = None,
    serialNumber: Option[String] = None,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends XjdfNode,
      Extensible

final case class DeviceSchemas(
    current: Option[FileSpec] = None,
    global: Option[FileSpec] = None,
)

final case class Device(
    deviceId: Nmtoken,
    costCenterId: Option[Nmtoken] = None,
    descriptiveName: Option[String] = None,
    deviceClasses: Vector[Nmtoken] = Vector.empty,
    deviceType: Option[String] = None,
    icsVersions: Vector[Nmtoken] = Vector.empty,
    jdfVersions: Vector[JdfVersion] = Vector.empty,
    knownLocalizations: Vector[LanguageTag] = Vector.empty,
    manufacturer: Option[String] = None,
    manufacturerUrl: Option[UriRef] = None,
    maxRunSpeed: Option[Float] = None,
    packaging: Vector[DevicePackaging] = Vector.empty,
    presentationUrl: Option[UriRef] = None,
    restApiBaseUrl: Option[UriRef] = None,
    revision: Option[String] = None,
    serialNumber: Option[String] = None,
    urlSchemes: Vector[Nmtoken] = Vector.empty,
    xjmfUrl: Option[UriRef] = None,
    schemas: DeviceSchemas = DeviceSchemas(),
    iconList: Option[IconList] = None,
    identificationFields: Vector[IdentificationField] = Vector.empty,
    modules: Vector[DeviceModule] = Vector.empty,
    extensions: Extensions = Extensions.empty,
) extends SpecificResource:
  val elementName: QualifiedName = XjdfNames.element("Device")
