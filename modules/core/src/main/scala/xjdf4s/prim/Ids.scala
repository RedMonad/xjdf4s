package xjdf4s
package prim

import cats.Show
import cats.data.NonEmptyChain
import cats.kernel.Eq

/**
 * XJDF data type `ID` (§2.2.3): unique identifier within the scope of a single
 * XJDF instance. Lexically an ID is like an NMTOKEN; *uniqueness*, however, is
 * not a property of the value itself — it is a property of the whole ticket and
 * is enforced by `XJDF.validate` (like a universal property of the document).
 */
opaque type Id = String

object Id:

  def from(raw: String): Option[Id] =
    Option(raw).filter(s => s.nonEmpty && !s.exists(_.isWhitespace))

  def unsafe(raw: String): Id =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid ID: '$raw'"))

  extension (id: Id) def value: String = id

  given Show[Id] = Show.show(identity)

  given Eq[Id] = Eq.fromUniversalEquals

end Id

/** XJDF data type `IDREF`: a reference to an `ID` within the same document. */
opaque type IdRef = String

object IdRef:

  def from(raw: String): Option[IdRef] =
    Option(raw).filter(s => s.nonEmpty && !s.exists(_.isWhitespace))

  def unsafe(raw: String): IdRef =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid IDREF: '$raw'"))

  extension (ref: IdRef) def value: String = ref

  given Show[IdRef] = Show.show(identity)

  given Eq[IdRef] = Eq.fromUniversalEquals

end IdRef

/** XJDF data type `IDREFS`: a whitespace-separated list of IDREF values. */
opaque type IdRefs = NonEmptyChain[IdRef]

object IdRefs:

  def of(head: IdRef, tail: IdRef*): IdRefs =
    NonEmptyChain(head, tail*)

  def from(chain: NonEmptyChain[IdRef]): IdRefs = chain

  extension (refs: IdRefs)
    /** The underlying non-empty chain (representation). */
    def toNonEmptyChain: NonEmptyChain[IdRef] = refs
    def toList: List[IdRef]                   = refs.toNonEmptyChain.toChain.toList
    def contains(ref: IdRef): Boolean         = refs.toNonEmptyChain.exists(_ == ref)

  given Show[IdRefs] = Show.show(refs => refs.toNonEmptyChain.toChain.toList.mkString(" "))

  given Eq[IdRefs] = Eq.fromUniversalEquals

end IdRefs

/**
 * `XJDF/@JobID`: job identification used by the application that created the
 * XJDF job (§2.2.2, Table 3.1). Maintained across XJDF instances — unlike
 * `Id`, which is scoped to one document.
 */
opaque type JobId = String

object JobId:

  def from(raw: String): Option[JobId] =
    Option(raw).filter(s => s.nonEmpty && !s.exists(_.isWhitespace))

  def unsafe(raw: String): JobId =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid JobID: '$raw'"))

  extension (id: JobId) def value: String = id

  given Show[JobId] = Show.show(identity)

  given Eq[JobId] = Eq.fromUniversalEquals

end JobId

/**
 * `XJDF/@JobPartID`: identifies one or more worksteps of the same type that can
 * be described as one XJDF (Table 3.1). Internal to the creating system.
 */
opaque type JobPartId = String

object JobPartId:

  def from(raw: String): Option[JobPartId] =
    Option(raw).filter(s => s.nonEmpty && !s.exists(_.isWhitespace))

  def unsafe(raw: String): JobPartId =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid JobPartID: '$raw'"))

  extension (id: JobPartId) def value: String = id

  given Show[JobPartId] = Show.show(identity)

  given Eq[JobPartId] = Eq.fromUniversalEquals

end JobPartId

/** `XJDF/@ProjectID`: project context that this XJDF belongs to (Table 3.1). */
opaque type ProjectId = String

object ProjectId:

  def from(raw: String): Option[ProjectId] =
    Option(raw).filter(s => s.nonEmpty && !s.exists(_.isWhitespace))

  def unsafe(raw: String): ProjectId =
    from(raw).getOrElse(throw new IllegalArgumentException(s"Not a valid ProjectID: '$raw'"))

  extension (id: ProjectId) def value: String = id

  given Show[ProjectId] = Show.show(identity)

  given Eq[ProjectId] = Eq.fromUniversalEquals

end ProjectId
