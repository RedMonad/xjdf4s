# M2 — XML/JSON-кодеки

Статус: `[ ]` (предусловие: M1 закрыт)

Wire-формат нельзя стабилизировать поверх известно неверных типов и
кардинальностей. Перед M2 все P0/P1 фаз M1.1–M1.6 должны быть закрыты.

Нормативная база: §1.4, §1.4.2 «Use of JSON», §9.10, §1.3.5.1 «Order of Child
Elements», `schema.xsd`.

## Контракты
```scala
trait Encoder[Format, -A]:
  def encode(value: A): Format
trait Decoder[Format, A]:
  def decode(input: Format): ValidatedNec[DecodeIssue, A]
```
`DecodeIssue` содержит код, путь в формате, ожидаемый тип, исходный токен,
причину. Независимые семантические ошибки накапливаются; невосстановимая
синтаксическая ошибка может быть fail-fast.

## Нормализация
Законы:
```
decode(encode(a))     = Right(normalize(a))
encode(decode(bytes)) = canonicalize(bytes)
```
До заморозки API определить: значения по умолчанию; различие «отсутствует» vs
«явно задан default»; порядок атрибутов/детей; namespace-префиксы; JSON-only
дискриминаторы; канонические лексические формы; политику foreign namespaces.
Если foreign extensions должны быть lossless — ввести raw extension AST
(неизвестные данные нельзя молча отбрасывать).

## Атомарные парсеры
Тотальные парсеры для: NMTOKENS и числовых списков; XYPair/Shape/Rectangle/
Matrix; цветов; IntegerRange; XSD dateTime/duration; RegExp; PDFPath;
transfer functions. Для каждого: валидный/невалидный корпуса, whitespace,
round-trip, границы, fuzz, отсутствие необработанных исключений.

## DoD M2
- Каждый тип M1 имеет кодек либо задокументированное исключение.
- Round-trip-законы зелёные; примеры совпадают с XML/JSON golden.
- Ни decoder, ни parser не бросают исключений на произвольном входе.
- Политика foreign extensions протестирована.