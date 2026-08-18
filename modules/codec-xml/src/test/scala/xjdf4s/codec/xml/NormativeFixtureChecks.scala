package xjdf4s.codec.xml

import xjdf4s.codec.xml.domain.*
import xjdf4s.core.*
import xjdf4s.messaging.*
import xjdf4s.model.*
import xjdf4s.model.resources.*

/** Normative fixtures from the XJDF 2.2 reference: Example 7.5 (QueryResource), Example 8.5 (MediaLayers XML) and
 *  Example 7.8 (SignalResource about consumed resources).
 */
object NormativeFixtureChecks:
  val example75QueryResource: Unit =
    val xml =
      """<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
        |  <Header DeviceID="TestSender" ID="l_000002" Time="2019-03-26T14:07:48.454+00:00"/>
        |  <QueryResource>
        |    <Header DeviceID="TestSender" ID="Q1" Time="2019-03-26T14:07:48.455+00:00"/>
        |    <ResourceQuParams ResourceDetails="Full" ResourceName="Media" Scope="Allowed"/>
        |  </QueryResource>
        |</XJMF>""".stripMargin
    val decoded = XmlParser.parse(xml).flatMap(XjmfCodec.decoder.decode).toOption.get
    assert(decoded.header.deviceId.value == "TestSender")
    val query = decoded.messages.toVector.head match
      case message: QueryResource => message
      case other => assert(false, s"expected QueryResource, got ${other.getClass.getName}")
    assert(query.header.id.exists(_.value == "Q1"))
    assert(query.params.scope == Scope.Allowed)
    assert(query.params.resourceName.exists(_.value == "Media"))
    assert(query.params.details.contains(ResourceDetails.Full))

  val example85MediaLayers: Unit =
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
    val decoded = XmlParser.parse(xml).flatMap(ResourceSetCodec.decoder.decode).toOption.get
    val media = decoded.resources.head.specificResource.get.asInstanceOf[Media]
    assert(media.mediaType == MediaType.SelfAdhesive)
    assert(media.mediaUnit.contains(MediaUnit.Roll))
    assert(media.thickness.contains(900.0f))
    val layers = media.mediaLayers.get.layers
    assert(layers.size == 3)
    assert(layers.head.isInstanceOf[MediaLayer.MediaLayer])
    assert(layers(1).isInstanceOf[MediaLayer.GlueLayer])
    assert(layers(2).isInstanceOf[MediaLayer.MediaLayer])
    val glue = layers(1).asInstanceOf[MediaLayer.GlueLayer].value
    assert(glue.areaGlue.contains(true))
    assert(glue.glueType.contains(GlueType.Removable))
    assert(media.mediaLayers.get.validate.isEmpty)

  val example78SignalResource: Unit =
    val xml =
      """<XJMF xmlns="http://www.CIP4.org/JDFSchema_2_0">
        |  <Header DeviceID="DeviceID" ID="l_000002" Time="2019-03-26T14:07:48.698+00:00"/>
        |  <SignalResource>
        |    <Header DeviceID="DeviceID" ID="S1" Time="2019-03-26T14:07:48.698+00:00" refID="Sub1"/>
        |    <ResourceInfo JobID="Job1" JobPartID="Printing" Scope="Job">
        |      <ResourceSet Name="Media" Usage="Input">
        |        <Resource ExternalID="MIS-ID">
        |          <AmountPool>
        |            <PartAmount Amount="4500" Waste="66">
        |              <Part LotID="Lot1"/>
        |            </PartAmount>
        |            <PartAmount Amount="2200" Waste="22">
        |              <Part LotID="Lot2"/>
        |            </PartAmount>
        |          </AmountPool>
        |          <Part SheetName="S1"/>
        |        </Resource>
        |      </ResourceSet>
        |    </ResourceInfo>
        |  </SignalResource>
        |</XJMF>""".stripMargin
    val decoded = XmlParser.parse(xml).flatMap(XjmfCodec.decoder.decode).toOption.get
    val signal = decoded.messages.toVector.head match
      case message: SignalResource => message
      case other => assert(false, s"expected SignalResource, got ${other.getClass.getName}")
    assert(signal.header.refId.exists(_.value == "Sub1"))
    assert(signal.resourceInfo.size == 1)
    val resourceSet = signal.resourceInfo.head.resourceSet
    assert(resourceSet.name.value == "Media")
    val resource = resourceSet.resources.head
    assert(resource.externalId.exists(_.value == "MIS-ID"))
    val pool = resource.amountPool.get
    assert(pool.amounts.toVector.map(_.amount.get) == Vector(4500.0f, 2200.0f))
    assert(pool.amounts.toVector.map(_.waste.get) == Vector(66.0f, 22.0f))
    assert(pool.amounts.toVector.head.parts.head.lotId.exists(_.value == "Lot1"))
    assert(resource.parts.head.sheetName.exists(_.value == "S1"))
end NormativeFixtureChecks
