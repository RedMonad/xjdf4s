# SPEC-COVERAGE — Реестр покрытия спецификации XJDF 2.2 и сознательных отклонений

Настоящий документ ведёт реестр соответствия доменного ядра спецификации CIP4 XJDF 2.2 и фиксирует все сознательные архитектурные отклонения с их обоснованием и компенсацией (ROADMAP §1.2, Приложение C, ADR-0007).

## Реестр сознательных отклонений

| Отклонение | Причина | Компенсация | Статус |
| --- | --- | --- | --- |
| `PartitionKey.OptionKey` вместо `Option` | коллизия имени со `scala.Option` | `attributeName = "Option"` + тест на wire-имя | реализовано (PR-4) |
| `SeverityClass` вместо `Severity` | коллизия с `@Severity: Int [0..100]` из §5.3.4.1 | документировано в scaladoc | реализовано (PR-5) |
| `HardCoverJacket.GlueApplied` / `Unjacketed` | Scala-имена не совпадают с токенами `Glue` / `None` (Table 4.11) | явный `def token` + golden-множество токенов | реализовано (PR-5) |
| Семейство «→ `None`»: `BindingType.NoBinding` (Table A.8), `BindingOrder.Unbound` (§4.3), `Coating.Uncoated` (Table A.11), `SoftCoverScoring.Unscored` (Table 4.18), `HardCoverJacket.Unjacketed` (Table 4.11) | `None` — зарезервированное имя `scala.None` | явные `token`-маппинги + golden-тест «`→ None` token family» в `laws/EnumLaws.scala` | реализовано (PR-5) |
| `HardCoverJacket.GlueApplied` | Scala-имя не совпадает с токеном `Glue` (Table 4.11, Sheet 1); имя `Glue` уже занято смыслом «тип клея» (`GlueType`, Table A.24) | явный `def token` без fallback-ветки + golden-тест на токен `Glue` (регрессия N-08) | реализовано (PR-5) |
| `DeviceStatus.Cleanup` / `.Setup` и `Status.Cleanup` / `.Setup` — одинаковые имена в разных enum | это два разных типа спецификации (Table A.15 и Table A.46), совпадение имён нормативно | обращение только с явной квалификацией (`DeviceStatus.Setup`); член спецификации не удаляется (ADR-0007) | реализовано (PR-5) |
| `Scope.Device` совпадает по имени с ресурсом `Device` (Table 6.57) | нормативное значение Table A.36 *(New in XJDF 2.2)* | обращение с явной квалификацией `Scope.Device`; коллизии нет, типы живут в разных пакетах | реализовано (PR-5) |
| `MediaType` содержит 7 значений с пометкой Deprecated | декодер обязан читать документы, использующие их (ADR-0010: неизвестные/устаревшие данные не отбрасываются молча) | пометки только в scaladoc; аннотация `@deprecated` не ставится — она сделала бы предупреждающим сам список `all`, а сборка держится warning-free | реализовано (PR-5) |
| `NamedColor` — открытый `NmToken` + `Catalog.NamedColor`, а не закрытый тип | prose (§1.10.3.1) и `schema.xsd` (147 `xs:pattern`) указывают на закрытый список, но §A.2.30 делегирует набор внешнему каталогу `[Color Names]` (SVG 1.1) | зафиксировано в ADR-0007; 147 значений в `Catalog.NamedColor` + тест на расширяемость; лексическая проверка — в кодеках M2 | реализовано (PR-5) |
| `Sides.Unprinted` и `Scope.Device` отсутствуют в `schema.xsd` | XSD отстаёт от нормативного текста Appendix A (обе пометки *New* присутствуют в prose) | по §1.2 приоритет за текстом; зафиксировано в ADR-0007 | реализовано (PR-5) |
| `XJDF/@Name` и `@$schema` отсутствуют в домене | JSON Exception, в XML запрещены (Table 3.1, X-04) | реализуются в `codec-json` (M2); статус **codec-only** | codec-only (M2) |
| `Comment/@Text` отсутствует в домене | JSON Exception (Table 8.14) | реализуется в `codec-json` (M2); статус **codec-only** | codec-only (M2) |
| Валидация `RegExp` — только непустота | Appendix A (Table A.1): «Regular expression as defined by `[XMLSchema]`» — грамматика XSD-regex | M1.2-1: валидация непустотой; полная XSD-грамматика — на стороне кодеков M2 | реализовано (PR-4) |
| `XjdfVersion.from` принимает только `"2.2"` | Table 3.1 требует `"2.2"` для соответствующих спецификации документов, хотя Table A.52 перечисляет `2.0`/`2.1`/`2.2` | scaladoc-объяснение (M1.5-2); при поддержке 2.0/2.1 — отдельное решение | запланировано (M1.5-2) |
| `Monoid[Matrix]` вместо `Group` | вырожденная матрица необратима | `inverse: Option[Matrix]` + задокументированная причина; опциональный `InvertibleMatrix` вне M1 | реализовано |
| `Semigroup` (не `Monoid`) для `AuditPool`, `AmountPool`, `NmTokens`, `ProcessPath` | носитель `NonEmptyChain`, кардинальность `T+` запрещает пустое значение | явная запись в scaladoc и в `docs/01` | реализовано |
| Дубликат `"Product"` в `@Types` считается нарушением | §3.1.3 говорит «additional process type tokens»; трактовка «любой второй токен» | зафиксировано как интерпретация + тест (M1.3-4, N-36) | запланировано (M1.3-4) |
