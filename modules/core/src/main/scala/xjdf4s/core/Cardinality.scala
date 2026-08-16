package xjdf4s.core

/** An immutable sequence whose XSD cardinality is `1..unbounded`. */
opaque type NonEmptyVector[+A] = Vector[A]

object NonEmptyVector:
  def one[A](head: A): NonEmptyVector[A] = Vector(head)

  def apply[A](head: A, tail: A*): NonEmptyVector[A] = head +: tail.toVector

  def from[A](values: IterableOnce[A]): Either[ValidationError, NonEmptyVector[A]] =
    val vector = Vector.from(values)
    Either.cond(vector.nonEmpty, vector, ValidationError.EmptyCollection("NonEmptyVector"))

  extension [A](values: NonEmptyVector[A])
    def head: A = values.head
    def tail: Vector[A] = values.tail
    def toVector: Vector[A] = values
    def size: Int = values.size
    def map[B](f: A => B): NonEmptyVector[B] = values.map(f)
    def append[B >: A](value: B): NonEmptyVector[B] = values :+ value
    def concat[B >: A](other: IterableOnce[B]): NonEmptyVector[B] = values ++ other
end NonEmptyVector
