# ROADMAP — xjdf4s

> XJDF 2.2 domain model для Scala 3.8.4 / sbt 2.0.2 / cats 2.13.0.
> Единственный источник истины — документация `./reference/*`; любое решение
> сверяется со спецификацией (главы, таблицы, разделы указаны в scaladoc и в
> этом документе). Предположения без ссылки на `./reference/*` — баги.

---

## 0. Видение

**Цель.** Дать декларативное, типобезопасное и *законосообразное* описание
XJDF: тикет, который невозможно построить невалидным молча; алгебраические
структуры, которые можно проверять свойствами; категориальный взгляд, в
котором тикет — морфизм, `ProductList` — начальная алгебра, `AuditPool` —
свободный моноид, а change order — действие моноида эндоморфизмов
(детали: `docs/01-category-theory-view.md`).

**Не-цели (сейчас).** Сериализация/десериализация в XML/JSON, XJMF-сообщения
(глава 7), транспорт — это отдельные модули-милстоуны; ядро остаётся
«чистым» доменом.

**Определение готовности этапа (общее).**
- собирается: `sbt compile`, `sbt test`, `sbt examples/run` на sbt 2.0.2 /
  Scala 3.8.4 без предупреждений `-Wunused:all -Wvalue-discard -Wnonunit-statement`;
- каждая новая структура покрыта свойством-законом или тестом-примером из
  спецификации;
- scaladoc каждого нового типа ссылается на таблицу/раздел `./reference/xjdf/*`.

---

## 1. Текущее состояние — Милстоун M0 «Каркас домена» (выполнен в этом PR)

Реализовано и покрыто тестами:

| Область | Что есть | Ссылка на спецификацию |
|---|---|---|
| Примитивы | NmToken(s), Id/IdRef(s), JobId/JobPartId/ProjectId, XjdfString, LanguageTag, Url, XPath, IcsVersion, XjdfVersion, XYPair, Shape, Rectangle, Matrix, Points/Microns/Grammage, Amount, Coverage, UnitInterval, Severity (формулы §5.3.4.1), IntegerRange (счёт §1.10.2), LabColor/CMYKColor/RGBColor, FloatList/IntegerList, AmountRange, Timestamp/TimeSpan/TimeRange | Appendix A; §1.10.2; §2.6.5 |
| Перечисления | 40+ закрытых enum с токенами из таблиц | Appendix A.2 |
| Partition | PartitionKey (27 ключей), ValueOf (match type), Part (Semigroup overlay, `matches`/`mergeWith`), PartBuilder | Table 6.4; §6.1.3.2–3 |
| Amounts | AmountPool/PartAmount/PartWaste | Tables 6.2–6.5 |
| Resource | ProcessType/ProcessPath/ProcessIndex, ResourceSet (+key, select), Resource | §3.4, §5.2, Table 6.1 |
| Resources | Media, Component, RunList(+ByteMap), NodeInfo, Contact(+Address/Person/Company/ComChannel), DeliveryParams(+DropItem), CuttingParams(+CutBlock), FoldingParams(+Fold/Perforate), Layout, Color, Preview, Device | Chapter 6 |
| Product | Product, ProductList, ProductTree/Fix/Bom (катаморфизм), Intent/IntentName | §3.3, Chapter 4 |
| Intents | BindingIntent (+7 деталей, union `BindingDetails`), MediaIntent, LayoutIntent, ColorIntent(+SurfaceColor), ProductionIntent, FoldingIntent, VariableIntent, AssemblingIntent(+4 вида вложений) | Chapter 4 |
| Audit | Audit (5 видов), AuditPool (Semigroup), Header/Notification/ProcessRun/ResourceInfo/DeviceInfo/Event/Milestone, Signal/Pulse, `Alignment` (FunctionK, Table 3.2) | §3.2; Tables 3.3–3.9, 7.3, 7.53, 7.67, 8.49 |
| Ticket | XJDF, WorkstepKey (named tuple), ChangeOrder (intersection), `TicketValidator` (12 проверок) | §3.1; §2.2.2–3 |
| Change orders | Patch (моноид Endo, действие на тикеты), mergeResourceSets (Ior) | §1.3.2 |
| ID | IdSource (State), IdAllocator (context function) | §2.2.3 |
| DSL | `dsl.*`: ticket/resourceSet/product/intent/audit-конструкторы → ValidatedNec | — |
| Примеры | Example 3.1, 3.4, 3.6, 5.2, 3.3 + brochure job + change order | Chapter 3/5 |
| Законы | Semigroup (Part/AmountPool/AuditPool), Semilattice (AmountRange), Monoid (TimeSpan/Matrix/Patch-поведенчески), естественность `Alignment.snapshot`, семантика выборки §6.1.3.2, валидация тикетов | laws-модуль |

**Пробелы M0, которые надо закрыть следующей итерацией (M1):** см. ниже.

---

## 2. Милстоуны

### M1 — Закрыть пробелы каркаса (ближайший)

- [ ] **Feedback-итерация сборки.** Прогнать CI-сборку (sbt 2.0.2, Scala 3.8.4)
      по результатам ревью этого PR; зафиксировать все замечания компилятора
      в этом пункте и закрыть их.
- [ ] **Полный Part.** Добить недостающие поля, если ревью спецификации
      (Table 6.4) покажет расхождения (проверка против `schema.xsd`).
- [ ] **Оставшиеся интенты главы 4:** ContentCheckIntent (+PreflightItem/
      ProofItem/FileSpec), EmbossingIntent (+EmbossingItem), HoleMakingIntent
      (+HolePattern, Appendix F), LaminatingIntent, ShapeCuttingIntent
      (+ShapeCut, CutBox/CutPath — PDFPath).
- [ ] **Недостающие общие подэлементы главы 8:** Crease (8.17), IdentificationField
      (8.26), Glue (8.24), HolePattern (8.25), Certification (8.7), MISDetails (8.30).
- [ ] **NodeInfo/GangSource** (табл. 6.59, 8.27) — планирование Gang-работ.
- [ ] **Закон §3.1.3.1:** NamedFeatures (GeneralID[@Datatype="NamedFeature"])
      и приоритет явных Traits над подразумеваемыми.
- [ ] **DoD:** `sbt test` зелёный; каждый новый тип — с scaladoc-ссылкой и
      хотя бы одним тестом; таблицы сверены с `./reference/xjdf/*`.

### M2 — Кодеки: XML и JSON (начать сразу после стабилизации M1)

Спецификация: §1.4 (два кодирования), §1.4.2 и §9.10 (JSON, REST),
«JSON Exception»-заметки по всем таблицам; схема — `./reference/xjdf/schema.xsd`.

- [ ] Модуль `codec-core`: `Encoder[A]`/`Decoder[A]` как typeclasses с законами
      (round-trip `decode ∘ encode = id` — тесты на всех типах M1).
- [ ] Модуль `codec-xml`: scala-xml, namespace `http://www.CIP4.org/JDFSchema_2_0`,
      порядок элементов по спецификации (лексическая сортировка, §1.3.5.1;
      исключение — Specific Resource последним, Table 6.1), foreign namespaces (§3.5).
- [ ] Модуль `codec-json`: маппинг §1.4.2; JSON-исключения: `$schema`, `Name`,
      `AuditPool` как массив с `Name` (§3.2), `Comment/@Text`, `Types` массивом.
- [ ] Парсеры атомарных типов (XYPair, matrix, rectangle, dateTime/duration по
      xsd, LabColor, PDFPath, TransferFunction) — на cats-parse, с
      property-тестами round-trip.
- [ ] **DoD:** каждый пример из `modules/examples` кодируется в XML и JSON и
      round-trip-ится без потерь; diff против ожидаемых литералов из спецификации.

### M3 — Полный каталог ресурсов главы 6

- [ ] Механически перенести оставшиеся ~130 ресурсов (таблицы главы 6) в
      `resources/` по шаблону M0 (case class + Option/Chain + scaladoc-таблица).
      План работ: алфавитными партиями, каждая партия — отдельный PR с тестом
      «строится и валидируется».
- [ ] Для каждого ресурса — `Intent Pairing` из шапки раздела (сопряжение,
      `docs/01-category-theory-view.md` §7) и привязка к процессам главы 5
      (входные/выходные ресурсы) — как **типовой реестр**
      `Process/…InputResources` для будущей проверки «процесс получает
      разрешённые ресурсы».
- [ ] **DoD:** счётчик покрытия ресурсов (цель 100% на конец этапа) в README;
      генератор-отчёт «таблица → тип» для сверки со спецификацией.

### M4 — XJMF (глава 7) как отдельный пакет `messaging`

- [ ] `XJMF`, `Header` (переиспользовать), 4 семейства: Query/Command/Response/
      Signal — как enum-иерархия, «type-safe message elements» (§1.5.6.2).
- [ ] Выравнивание Table 3.2 продолжить: Signal→Audit уже есть; добавить
      CommandReturnQueueEntry→AuditProcessRun.
- [ ] Клеящий слой к REST §9.10.3 (SubmitQueueEntry/ReturnQueueEntry/
      KnownDevices…), HTTP — эффектный (`Kleisli[F, *]`), транспорт изолирован.
- [ ] **DoD:** XJMF-обмены из примеров главы 7 компилируются и валидируются;
      сигнальный поток сворачивается в AuditPool (Alignment, законы из M0).

### M5 — «Живой» workflow и категориальные демонстрации

- [ ] Процессные сети за пределами одного тикета (Controller-композиция,
      §2.4, §9.3): типы «конвейер тикетов» с проверкой стыковки
      output→input ResourceSet (категориальная композиция морфизмов).
- [ ] `PipeControl`/`Dependent` — overlap-обработка (§3.4.1, §7.11).
- [ ] Writer-семантика аудитов на потоке сигналов (`fs2`/`WriterT`-демо) —
      обоснование в `docs/03-cats-mapping.md`.
- [ ] Стек-безопасные свёртки BOM (`Eval`-cata) для глубоких деревьев.
- [ ] **DoD:** пример «end-to-end»: MIS строит тикет → Device исполняет →
      аудиты возвращаются → change order корректирует → повторный прогон.

### M6 — Публикация и качество

- [ ] Публикация артефактов (Sonatype): `xjdf4s-core`, `xjdf4s-laws`,
      `xjdf4s-codec-*`; версионирование semver, MiMa-совместимость ядра.
- [ ] Scaladoc-сайт; примеры как type-checked docs.
- [ ] Бенчмарки (валидация, кодеки) — JMH.
- [ ] Дорожная проверка «тикет из реального мира»: несколько публичных XJDF
      из CIP4-репозитория валидируются/сериализуются без ошибок.

---

## 3. Риски и открытые вопросы

1. **Сборка вслепую.** В окружении автора нет JVM; первый запуск `sbt` —
   на стороне ревью. Mitigation: консервативный диалект (стабильные фичи
   3.7+), минимум зависимостей (cats-core, munit, munit-scalacheck), никаких
   плагинов; все версии зафиксированы в `build.sbt` и проверены по реестрам.
2. **Named tuples на 3.8.** Фича стабильна с 3.7 (справочник
   `./reference/scala/docs/reference/other-new-features/named-tuples.md`);
   используется только как представление opaque-типов — при необходимости
   легко заменить на class без изменения публичного API.
3. **GADT-уточнение в `Part.get`/`PartBuilder.withKey`** (match type +
   singleton-ветки) — стандартный паттерн Scala 3, но при проблемах
   компиляции заменяется на `Part.valueOf`/перегрузки без потери типобезопасности
   в остальном домене.
4. **XSD как вторая истина.** Текст спецификации и `schema.xsd` могут
   расходиться; для кодеков (M2) источник истины — текст, XSD — тест-оракул.
5. **Размер каталога.** Глава 6 — сотни таблиц; перенос механический и
   объёмный (M3), но не рискованный: шаблон M0 отлажен на 12 ресурсах.

---

## 4. Конвенции вклада

- Один PR = один пункт милстоуна; в описании — ссылки на разделы
  `./reference/xjdf/*`, затронутые изменениями.
- Коммит-сообщения: `M<n>: краткое описание` (например, `M2: json codec for
  AuditPool with JSON Exception`).
- Любой новый инстанс cats — с property-тестом в `modules/laws`.
- Документация: scaladoc на английском (для типа — ссылка на таблицу),
  `docs/*` и ROADMAP — на русском.
- Флаг `-Wunused:all` и прочие — обязательны; предупреждения не допускаются
  в master.

## 5. Ссылки

- Спецификация: `./reference/xjdf/0 – Table of Contents.md` … `Appendix H`,
  `schema.xsd`.
- Категориальная база: `./reference/category-theory/*` (части 1–3).
- Возможности языка: `./reference/scala/docs/reference/*`,
  `./reference/scala/spec/*`.
- cats: `./reference/cats/docs/*`.
- sbt: `./reference/sbt/docs/*`.
- Дизайн-заметки: `docs/01-category-theory-view.md`,
  `docs/02-scala3-features.md`, `docs/03-cats-mapping.md`, `docs/04-architecture.md`.
