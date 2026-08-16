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

/** An immutable sequence whose XSD cardinality is `0..2`. */
opaque type AtMostTwo[+A] = Vector[A]

object AtMostTwo:
  def empty[A]: AtMostTwo[A] = Vector.empty
  def one[A](value: A): AtMostTwo[A] = Vector(value)
  def two[A](first: A, second: A): AtMostTwo[A] = Vector(first, second)

  def from[A](values: IterableOnce[A]): Either[ValidationError, AtMostTwo[A]] =
    val vector = Vector.from(values)
    Either.cond(
      vector.size <= 2,
      vector,
      ValidationError.InvalidValue("AtMostTwo", vector.size.toString, "between zero and two values"),
    )

  extension [A](values: AtMostTwo[A])
    def toVector: Vector[A] = values
    def size: Int = values.size
    def map[B](f: A => B): AtMostTwo[B] = values.map(f)
end AtMostTwo
