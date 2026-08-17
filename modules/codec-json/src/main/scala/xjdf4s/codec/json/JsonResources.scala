package xjdf4s.codec.json

import io.circe.{Decoder, HCursor, Json}
import io.circe.syntax.*

import xjdf4s.model.*
import xjdf4s.model.resources.*

/**
 * Dispatch for the specific-resource member of `Resource` (JSON slice). The member name is the resource element
 * name; the covered set mirrors the XML property-test surface.
 */
object JsonResources:

  val coveredNames: Vector[String] = Vector("Media", "Color", "Component", "Tool", "RunList", "RegisterMark")

  def nameOf(resource: SpecificResource): String =
    resource match
      case _: Media         => "Media"
      case _: Color         => "Color"
      case _: Component     => "Component"
      case _: Tool          => "Tool"
      case _: RunList       => "RunList"
      case _: RegisterMark  => "RegisterMark"
      case other => throw new UnsupportedOperationException(s"no JSON codec for ${other.getClass.getName} in this slice")

  def encode(resource: SpecificResource): Json =
    resource match
      case media: Media         => media.asJson
      case color: Color         => color.asJson
      case component: Component => component.asJson
      case tool: Tool           => tool.asJson
      case runList: RunList     => runList.asJson
      case registerMark: RegisterMark => registerMark.asJson
      case other => throw new UnsupportedOperationException(s"no JSON codec for ${other.getClass.getName} in this slice")

  def decodeSpecific(cursor: HCursor): Decoder.Result[Option[SpecificResource]] =
    coveredNames.foldLeft[Decoder.Result[Option[SpecificResource]]](Right(None)) { (acc, name) =>
      acc match
        case found @ Right(Some(_)) => found
        case Right(None) =>
          cursor.downField(name).focus match
            case Some(json) => decodeOne(name, json).map(value => Some(value))
            case None       => Right(None)
        case failure => failure
    }

  private def decodeOne(name: String, json: Json): Decoder.Result[SpecificResource] =
    name match
      case "Media"        => json.as[Media].map(identity)
      case "Color"        => json.as[Color].map(identity)
      case "Component"    => json.as[Component].map(identity)
      case "Tool"         => json.as[Tool].map(identity)
      case "RunList"      => json.as[RunList].map(identity)
      case "RegisterMark" => json.as[RegisterMark].map(identity)
      case other          => JsonCodec.fail(json.hcursor, s"resource '$other' is not covered by the JSON slice")
end JsonResources
