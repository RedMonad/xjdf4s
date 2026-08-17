package xjdf4s.codec.json

import io.circe.parser.*

import xjdf4s.codec.json.given
import xjdf4s.codec.xml.*
import xjdf4s.codec.xml.domain.*
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/** Normative JSON fixtures (Examples 3.1, 8.5, 7.1) decoded into the same ADT values as their XML counterparts. */
object NormativeJsonFixtureChecks:

  val example31Root: Unit =
    val json = """{
      |  "JobID": "J1",
      |  "Name": "XJDF",
      |  "Types": [ "Product" ],
      |  "Version": "2.2"
      |}""".stripMargin
    val decoded = parse(json).flatMap(_.as[XJDF]).toOption.get
    assert(decoded.jobId.value == "J1")
    assert(decoded.types.toVector.map(_.value) == Vector("Product"))
    assert(decoded.version.contains(Version.V2_2))

  val example85MediaLayers: Unit =
    val json =
      """{
        |  "ResourceSet": {
        |    "Name": "Media",
        |    "Resource": [{
        |      "Media": {
        |        "Dimension": [1190.5511811, 0],
        |        "MediaLayers": [{
        |          "MediaType": "Paper",
        |          "Name": "Media",
        |          "Weight": 90
        |        }, {
        |          "AreaGlue": true,
        |          "GlueType": "Removable",
        |          "Name": "Glue"
        |        }, {
        |          "MediaType": "Paper",
        |          "Name": "Media",
        |          "Weight": 60
        |        }],
        |        "MediaType": "SelfAdhesive",
        |        "MediaUnit": "Roll",
        |        "Thickness": 900
        |      }
        |    }],
        |    "Usage": "Input"
        |  }
        |}""".stripMargin
    // the normative JSON fragment carries ResourceSet as a MEMBER of an XJDF object, not as the root
    val viaJson = parse(json).flatMap(_.hcursor.downField("ResourceSet").as[ResourceSet]).toOption.get

    val xml =
      """<ResourceSet Name="Media" Usage="Input">
        |  <Resource>
        |    <Media Dimension="1190.5511811 0" MediaType="SelfAdhesive" MediaUnit="Roll" Thickness="900">
        |      <MediaLayers>
        |        <Media MediaType="Paper" Weight="90"/>
        |        <Glue AreaGlue="true" GlueType="Removable"/>
        |        <Media MediaType="Paper" Weight="60"/>
        |      </MediaLayers>
        |    </Media>
        |  </Resource>
        |</ResourceSet>""".stripMargin
    val viaXml = XmlParser.parse(xml).flatMap(ResourceSetCodec.decoder.decode).toOption.get

    assert(
      viaJson == viaXml,
      s"JSON/XML mismatch:\nJSON: $viaJson\nXML:  $viaXml",
    )
    val layers = viaJson.resources.head.specificResource.get.asInstanceOf[Media].mediaLayers.get.layers
    assert(layers.size == 3)
    assert(layers(1).asInstanceOf[MediaLayer.GlueLayer].value.glueType.contains(GlueType.Removable))

  val example71Xjmf: Unit =
    val json = """{
      |  "Header": {
      |    "DeviceID": "CIP4_JDF_Writer_Java",
      |    "ID": "l_230910_094905994_000000",
      |    "Time": "2023-09-10T09:49:05+02:00"
      |  },
      |  "Name": "XJMF",
      |  "SignalNotification": {
      |    "Header": {
      |      "DeviceID": "CIP4_JDF_Writer_Java",
      |      "ID": "l_230910_094906015_000001",
      |      "Time": "2023-09-10T09:49:06+02:00"
      |    },
      |    "Notification": {
      |      "Class": "Event"
      |    }
      |  },
      |  "Version": "2.2"
      |}""".stripMargin
    val decoded = parse(json).flatMap(_.as[XJMF]).toOption.get
    assert(decoded.version.contains(Version.V2_2))
    assert(decoded.header.deviceId.value == "CIP4_JDF_Writer_Java")
    decoded.messages.toVector.head match
      case signal: SignalNotification => assert(signal.notification.severity == Severity.Event)
      case other => assert(false, s"expected SignalNotification, got ${other.getClass.getName}")
end NormativeJsonFixtureChecks
