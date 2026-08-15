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
| Дубликат `"Product"` в `@Types` считается нарушением | §3.1.3 говорит «additional process type tokens»; трактовка «любой второй токен» | зафиксировано как интерпретация + негативный тест (N-36, `XJDF-TYPES-PRODUCT-DUPLICATE`) | реализовано (PR-8, M1.3-4) |

## Decision records (короткие)

### DR-N36 — дубликат `"Product"` в `@Types` (строгая политика)

**Норма.** §3.1.3: «`@Types` of process XJDF SHALL NOT contain the token `"Product"` if any additional process type tokens are present».

**Вопрос.** Запрещает ли норма только смешение `"Product"` с процессными токенами (`"Product Cutting"`), или и чистый дубликат (`"Product Product"`)? Слово «additional» допускает оба толкования.

**Решение (PR-8, M1.3-4).** Принята строгая политика: дубликат `"Product"` отклоняется отдельным кодом `XJDF-TYPES-PRODUCT-DUPLICATE` (`IssueCode.ProductTokenDuplicate`), а смешение с процессными токенами — кодом `XJDF-TYPES-PRODUCT-MIXED` (`IssueCode.ProductTokenMixed`). Обоснование: `@Types` — упорядоченный список процессов (§5.2, `ProcessPath`); идентификатор процесса `"Product"` не несёт процессной семантики при повторении, и его дублирование указывает на ошибку отправителя. Это интерпретация, а не дословная норма, поэтому: (1) выделен отдельный `IssueCode`, (2) добавлен негативный тест `N-36: duplicate "Product" token in @Types is rejected`, (3) запись остаётся в реестре отклонений.

### DR-DomainRule — форма локальных законов (ADR-0003, M1.3-3)

**Контекст.** Ряд локальных инвариантов (`Intent.isLawful`, `BindingIntent.isLawful`, `VariableIntent.isLawful`, `PartWaste.isLawful`, `Disposition.isLawful`, `Product.hasLawfulAmounts`, `Notification.hasLawfulMilestone/hasUniqueCommentLanguages`, `ResourceSet.hasLawfulChildren/hasLawfulStatuses`) был реализован как `Boolean`-предикаты; часть из них не была подключена к корневому валидатору (N-18).

**Решение (PR-8).** Все локальные законы приведены к контракту ADR-0003 `trait DomainRule[-A]: def check(value: A, at: XPath): Chain[Issue]` и явно вызываются из `TicketValidator.checkLocalLaws`. Каждый закон возвращает структурированный `Issue` со стабильным `IssueCode`, severity и XPath. `Boolean`-предикаты сохранены как производные аксессоры там, где их использует DSL (`Intent.isLawful`) или тесты, но они больше не являются первичной формой закона. Глобальные правила (ID/IDREF, §3.4, BOM, хронология) остаются в `TicketValidator`; решение владельца — рефакторить все предикаты сразу (не оставлять legacy).

**Прим.:** `Disposition.law` определён в `TicketValidator.dispositionLaw`, а не в `prim.Disposition`, чтобы `prim` не зависел от слоя валидации; разрыв цикла зависимостей выполнен в M1.4-1 (PR-9) — правило остаётся на месте, хук в обходе ресурсов подключается при реализации FileSpec-несущих ресурсов (M1.6/M3).

### DR-M1.4-1 — разрыв цикла зависимостей валидации (ADR-0002, N-21)

**Норма.** ADR-0002: фундамент валидации — файл с Fan-Out 0; `Ticket.scala` не зависит от реализации `Patch`; корневой валидатор агрегирует правила; повторный анализ зависимостей — 0 циклов.

**Решение (PR-9).** `model/ValidationTypes.scala` создан и содержит `Issue`, `IssueCode`, `SeverityClass`, `XPath`, `trait DomainRule[-A]`, `type ValidationResult[A] = ValidatedNec[Issue, A]`, `ValidationReport`; импортирует только `prim.*` и cats. По решению владельца список ADR-0002 выполнен буквально: `IssueCode`, `SeverityClass`, `XPath` перенесены из `prim` (`Tokens.scala`, `Enums.scala`) в слой валидации. `Validation.scala` переименован в `TicketValidator.scala`. Для нуля циклов из `Ticket.scala` убраны `XJDF.validate`, `XJDF.validateReport` (стали extension-методами в `TicketValidator.scala`) и `XJDF.withPatch` (extension в `Patch.scala`).

**Migration impact.** Типы `XPath`, `SeverityClass`, `IssueCode` сменили пакет `xjdf4s.prim` → `xjdf4s.model`; потребители, импортировавшие их через `prim.*` без `xjdf4s.model.*`, обновлены: `intents/Binding.scala`, `intents/FoldingVariable.scala`, `laws/EnumLaws.scala`. Методы `XJDF.validate`/`validateReport`/`withPatch` — теперь extension-методы; call sites с точечным импортом обязаны добавить их в импорт (в репозитории такой один — `examples/SpecExamplesSuite.scala`, добавлен `validate`). Все прочие call sites используют `import xjdf4s.model.*` и не менялись: `dsl/XjdfDsl.scala`, `examples/SpecExamples.scala`, `laws/{TicketLaws,BomLaws,PatchLaws,AlignmentLaws}.scala`.

**Верификация.** Анализатор файловых зависимостей (top-level-символы, package-aware резолвинг, комментарии/строки исключены): до PR-9 — 1 цикл (SCC с `Validation/Product/Ticket/Patch`), после — 0 циклов. Межмодульный граф не изменился (`examples → core`, `laws → core`). Прогон владельца — Приложение D ROADMAP.

**Компиляция (первый прогон владельца).** Единственная ошибка — E008 в теле `withPatch`: extension-синтаксис `patch.applyTo(ticket)` не резолвится для opaque `Patch` из тела top-level extension-блока в файле-определителе. Исправлено статической формой `Patch.applyTo(patch)(ticket)`; повторный прогон ожидается.

