# Specification Coverage Report

Generated: 2026-08-15 (PR-13)
Checker: `scripts/check-spec-coverage.sh` (запускается в CI, переиспользуется генератором M3)

Сводка покрытия **вычисляется** чекером, а не хранится приблизительным числом:
README ссылается на этот документ, числа — в выводе чекера.

**Конвенция ссылок.** Нумерация разделов и таблиц спецификации независима
(именно эта путаница породила семь ошибок N-15). В scaladoc используется
формат `§x.y / Table z`; чекер сверяет существование каждой таблицы с
заголовками `**Table N.M: …**` / `### Table N.M …` в `reference/xjdf/*`.

**Семантика колонок.**

- **Cardinality** — словарь Table 1.2: `1`, `?`, `*`, `+` (в рамках родительского элемента);
- **Validation** — `✅` если тип охвачен `DomainRule`/проверкой корневого валидатора
  (включая контейнерные законы `@Name ↔ elementName`); `❌` — нет;
- **Domain tests** — `✅` если тип прямо упражняется хотя бы одним тестом в
  `modules/laws` или conformance-сьютом примеров; `❌` — нет;
- **XML / JSON** — канонические кодеки; появляются в M2;
- **Status** — `Implemented` / `Planned` / `codec-only (M2)` / `Not modelled`;
- **Notes** — version notes (`New in XJDF 2.x`), решения и ссылки на находки.

## Resources (Chapter 6)

| Section | Table | Element/Attribute | Scala type | Cardinality | Validation | Domain tests | XML | JSON | Status | Notes |
|---------|-------|-------------------|------------|-------------|------------|--------------|-----|------|--------|-------|
| §6.1 | Table 6.1 | Resource | `Resource` | `+` | ✅ | ✅ | ❌ | ❌ | Implemented | bodyless `<Resource/>` представим (N-11); `Specific Resource?` → `Option`; `@Name ↔ payload` — `ResourceSetLaw`; New in XJDF 2.1: `@Expires` — не моделируется до M3 |
| §6.1 | Table 6.1 | Resource payload dispatch | `ResourcePayload` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum: 12 payload + `Foreign` escape hatch (ADR-0008) |
| §3.4 | Table 3.12 | ResourceSet | `ResourceSet` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | уникальность §3.4 попарно через `clashesWith` (N-16, `IssueCode.ResourceSetClash`) |
| §6.1.2 | Table 6.3 | PartAmount | `PartAmount` | `+` | ✅ | ✅ | ❌ | ❌ | Implemented | `parts: Chain[Part]` (N-10); nominal/`AmountBounds` разделены (ADR-0004) |
| §6.1.3 | Table 6.4 | Part | `Part` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | все 27 ключей; `matches` — отношение толерантности (ADR-0005); New in XJDF 2.1: `@Product` → `Part.product` |
| §6.1.4 | Table 6.5 | PartWaste | `PartWaste` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | law: задан `@ModuleIDs` или `@WasteDetails` |
| §6.1.1 | Table 6.2 | AmountPool | `AmountPool` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | `Semigroup` — кардинальность `T+` запрещает `Monoid` (ADR-0006/§6) |
| §6.14 | Table 6.27 | Color | `Color` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | New in XJDF 2.1: `@Spectrum`, `ColorMeasurementConditions` — не моделируются (M3) |
| §6.18 | Table 6.37 | Component | `Component` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | `IdentificationField*` (Table 8.31) → `Chain[IdentificationField]`, XSD `minOccurs="0" maxOccurs="unbounded"`; SHALL Table 8.31 подключён через `checkResourceLocalLaws` (M1.6-6); `references` обходит цепочку, собственных IDREF у элемента нет; `@ReaderPageCount` (Deprecated in XJDF 2.1) не моделируется — заменён `@SurfaceCount` |
| §6.19 | Table 6.38 | Contact | `Contact` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | |
| §6.19.1 | Table 6.39 | ComChannel | `ComChannel` | `*` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §6.19.2 | Table 6.40 | Company | `Company` | `?` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §6.19.4 | Table 6.42 | Person | `Person` | `?` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §8.1 | Table 8.1 | Address | `Address` | `?` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §6.25 | Table 6.53 | CuttingParams | `CuttingParams` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | |
| §8.16 | Table 8.19 | CutBlock | `CutBlock` | `*` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §6.26 | Table 6.54 | DeliveryParams | `DeliveryParams` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | |
| §6.26.1 | Table 6.55 | DropItem | `DropItem` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | полон по Table 6.55: `@TotalDimensions`, `@TotalVolume`, `@TotalWeight` (N-12) |
| §6.28 | Table 6.57 | Device | `Device` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | New in XJDF 2.1: `@MaxRunSpeed`, FileSpec CurrentSchema/Schema; New in XJDF 2.2: `@RestApiBaseURL` (JSON Exception — доменное `Url`-поле, обработка в codec M2) |
| §6.36 | Table 6.74 | FoldingParams | `FoldingParams` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | `Crease*` (Table 8.17) моделируется (M1.6-2); `Cut*` — M3 |
| §8.7 | Table 8.8 | Certification | `Certification` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | 3 атрибута, все опциональны в `schema.xsd` (`use="optional"`, подэлементов и ID/IDREF нет): `@Claim?` string → `XjdfString` (открытый `Catalog.CertificationClaim`, 7 значений + параметрический `PEFC nn%`), `@Identifier?` string, `@Organization?` NMTOKEN → открытый `Catalog.CertificationOrganization` (`CFCC`, `FSC`, `IFCC`, `PEFC`); SHALL «Each Certification SHALL specify a … certification level» (Tables 4.21/4.32/4.33/6.114) → `IssueCode.CERTIFICATION-LEVEL-MISSING`, `Certification.law` + `containerLaw` с XPath-индексацией (ADR-0012, M1.6-1); контейнеры (везде `Certification*`, XSD `minOccurs="0" maxOccurs="unbounded"`) — `ColorIntent/SurfaceColor` (4.21), `MediaIntent` (4.32), `ProductionIntent` (4.33), `Media` (6.114) — все четыре подключены; `Ink` (6.83) и `MiscConsumable` (6.117) не моделируются (M3); контейнерное «at least one of the levels SHALL be met» — требование к производству, не проверяется (реестр отклонений) |
| §8.14 | Table 8.17 | Crease | `Crease` | `*` | ❌ | ✅ | ❌ | ❌ | Implemented | structural; container-level validation; контейнеры — `FoldingParams` (`Crease*`, моделируется), `CreasingParams` (`Crease+`, M3); `@Depth` в µm → `Microns` |
| §8.22 | Table 8.27 | GangSource | `GangSource` | `*` | ❌ | ✅ | ❌ | ❌ | Implemented | `@Copies` и `@JobID` обязательны структурно; `@BinderySignatureID?` → `Option[NmToken]`; `@JobID` → `JobId`; подэлементов и ID/IDREF нет; `@JobID`/`@BinderySignatureID` — междокументные NMTOKEN-ссылки, не входят в `XJDF.references` (реестр отклонений, ADR-0006); все четыре контейнера имеют `GangSource*`: `JobPhase`, `QueueFilter`, `QueueEntry` (M4) и `NodeInfo` (M1.6-8 после `MISDetails`) |
| §8.30 | Table 8.48 | MISDetails | `MISDetails` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | 4 атрибута, все опциональны (`schema.xsd`: 2 именованных + 2 inline-enum, подэлементов и ID/IDREF нет) — пустой `<MISDetails/>` валиден, локальных SHALL нет; `@Complexity?` float → `UnitInterval` (prose-диапазон 0.0..1.0 обеспечивается фабрикой; XSD — просто `xs:float`, приоритет prose по §1.2); `@CostType?` → закрытый `CostType`; `@WorkType?` → закрытый `WorkType`; `@WorkTypeDetails?` NMTOKEN → открытый `Catalog.WorkTypeDetails` (ADR-0007); все четыре контейнера имеют `MISDetails?`: `ResourceInfo`, `PipeParams`, `JobPhase` (M4) и `NodeInfo` (M1.6-8 вместе с `GangSource*`) |
| §8.26 | Table 8.31 | IdentificationField | `IdentificationField` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | 10 атрибутов, все опциональны (`schema.xsd`: 9 именованных + 2 inline-enum — `@Encoding`, `@Purpose`; ID/IDREF нет) + 3 подэлемента; SHALL «Exactly one of `@Format`, `@Value` or the pair `@ValueFormat` and `@ValueTemplate`» → `IssueCode.IDENTIFICATION-FIELD-VALUE-SOURCE`, `IdentificationField.law` + `containerLaw` с XPath-индексацией (M1.6-6); три способа нарушения (ноль источников, более одного, половина пары) — по негативному тесту на каждый; `@BoundingBox?` → `Rectangle`, `@Orientation?` → `Matrix`, `@Format?` → `RegExp`, `@Position?` → `Face` (Table A.20), `@ValueTemplate?` NMTOKENS → `NmTokens`; открытые каталоги `Catalog.EncodingDetails` (Table 8.32, 47 значений), `Catalog.PurposeDetails`; подэлементы: `BarcodeDetails?` и `ExtraValues?` (оба `maxOccurs="1"`) моделируются, `MetadataMap*` → `Chain[MetadataMap]`; контекстные SHALL Tables 8.31/8.46 подключены к root validator (M1.6-6b/B2, ADR-0014); контейнер, подключённый к валидатору, — `Component` (`IdentificationField*`, Table 6.37); `Device`, `Media` — при их обходах; `Content/BarcodeProductionParams` и `EmbossingParams/Emboss` (`maxOccurs="1"`), `ExposedMedia`, `Ink`, `Layout/StripMark`, `MiscConsumable`, `Pallet`, `Tool`, `Module` — M3/M4 |
| §8.26.1 | Table 8.33 | BarcodeDetails | `BarcodeDetails` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | 4 атрибута, все опциональны (`schema.xsd`, подэлементов и ID/IDREF нет) — пустой `<BarcodeDetails/>` валиден, локальных SHALL нет; `@BarcodeVersion?`/`@ErrorCorrectionLevel?` NMTOKEN → открытые `Catalog.BarcodeVersion` (Tables 8.36/8.37, 29 DATAMATRIX + 40 QR) и `Catalog.ErrorCorrectionLevel` (9 PDF417 + 4 QR); `@XCells?`/`@YCells?` `xs:int` → `Option[Long]` без выдуманной положительности; единственный контейнер — `IdentificationField` (M1.6-6) |
| §8.26.2 | Table 8.34 | ExtraValues | `ExtraValues` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | 2 атрибута, оба `use="required"` → обязательные поля (`usage: NmToken`, `value: XjdfString`), неполный элемент непредставим типом, runtime-правило не нужно; подэлементов и ID/IDREF нет; `@Usage` NMTOKEN → открытый `Catalog.ExtraValuesUsage` (`CompositeCode`, `Coupon`, `Supplemental`); единственный контейнер — `IdentificationField` с `ExtraValues?` (`maxOccurs="1"` — один элемент, не список; M1.6-6) |
| §8.29 | Table 8.46 | MetadataMap | `MetadataMap` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | M1.6-6b/B2: обязательные `@Name: NmToken`, `@ValueFormat: XjdfString`, `@ValueTemplate: NmTokens`; `Expr*` → `Chain[Expr]`; ID/IDREF нет; wiring `RunList.metadataMaps` и `IdentificationField.metadataMaps`; четыре контекстных SHALL в `TicketValidator`, по негативному тесту на каждый; Examples 8.6/8.7 fixture; Table 8.31 против Example 8.7 — ADR-0014/N-55, выбран prose |
| §8.29.1 | Table 8.47 | Expr | `Expr` | `*` | ❌ | ✅ | ❌ | ❌ | Implemented | M1.6-6b/B1: обязательные `@Name: NmToken` и `@Path: XjdfXPath` обеспечены полями типа; XSD подтверждает оба `use="required"`, ID/IDREF и подэлементов нет; implied XPath `text()` — семантика вычислителя, локальных структурных SHALL нет; контекстные правила родителя `MetadataMap` и wiring — B2 |
| §8.24 | Table 8.29 | Glue | `Glue` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | SHALL: `@GluingPattern` even entries, `@MeltingTemperature` only with Hotmelt/PUR (M1.6-3, ADR-0011); `@GlueRef` → IDREF (collected); контейнеры — `BindIn`, `StickOn`, `AdhesiveNote` (глава 4), `GluingParams` etc. (M3) |
| §8.25 | Table 8.30 | HolePattern | `HolePattern` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | SHALL: `@Pattern` SHALL be supplied when `@Center`, `@Extent` or `@Shape` missing (M1.6-5); контейнеры — `HoleMakingIntent` (`+`, моделируется, M1.6-12), `HoleMakingParams` (`+`, M3), `LooseBinding` (`?`, моделируется), `Media` (`*`, M3); открытый каталог `Catalog.HolePattern` (Appendix F, 34 значения incl. None из XSD); `Catalog.HoleReinforcement` (Grommet) |
| §6.52 | Table 6.95 | Layout | `Layout` | `?` | ✅ | ❌ | ❌ | ❌ | Implemented | New in XJDF 2.1: `@Anchor`, `@SheetLay` — моделируются; `@ExpansionBox`, `Position/@PositionOrd` — не моделируются (M3) |
| §6.57 | Table 6.114 | Media | `Media` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | New in XJDF 2.1: `@BackCIE*`, `@Spectrum`, `ColorMeasurementConditions` — не моделируются (M3); `Certification*` (Table 8.8) моделируется, SHALL подключён через `checkResourceLocalLaws` (M1.6-1); `HolePattern*`, `IdentificationField*`, `MediaLayers?` — M3 |
| §6.59 | Table 6.119 | NodeInfo | `NodeInfo` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | 15 атрибутов, все опциональны, + 2 подэлемента (`schema.xsd`: 14 именованных + 1 inline-enum, ID/IDREF нет) — пустой `<NodeInfo/>` валиден, локальных SHALL нет; `GangSource*` (Table 8.27) → `Chain[GangSource]` и `MISDetails?` (Table 8.48) → `Option[MISDetails]` подключены в M1.6-8, `references` обходит обоих потомков; `@DueLevel?` — закрытый `DueLevel` вместо прежнего `Option[Long]` (N-52); `@PersonalID?` NMTOKEN указывает на `Resource/@ExternalID`, не IDREF (реестр отклонений); `@JobPriority?` остаётся `Option[Long]` — prose описывает шкалу 0..100, но не задаёт нормативный диапазон, проверка — граница кодека M2 |
| §6.66 | Table 6.134 | Preview | `Preview` | `?` | ✅ | ❌ | ❌ | ❌ | Implemented | |
| §6.73 | Table 6.148 | RunList | `RunList` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | `FileSpec?` → `Option[FileSpec]` (`minOccurs="0" maxOccurs="1"`; N-53), кардинальность закреплена regression- и XSD-oracle-тестами `RunListLaws`; New in XJDF 2.1: `@DocPages` → `RunList.docPages`; упражняется conformance-сьютом (`brochureJob`, `metadataMapJob`); `MetadataMap*` → `Chain[MetadataMap]`, разрешение переменных через Table D.1/ровно один `Expr` подключено к root validator (M1.6-6b/B2) |
| §6.73 | Table 6.148 | ByteMap | `ByteMap` | `*` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |

## Enumerations (Appendix A)

| Section | Table | Element/Attribute | Scala type | Cardinality | Validation | Domain tests | XML | JSON | Status | Notes |
|---------|-------|-------------------|------------|-------------|------------|--------------|-----|------|--------|-------|
| §A.1 | Table A.1 | XPath data type | `XjdfXPath` | 1 | ✅ | ✅ | ❌ | ❌ | Implemented | M1.6-6b/B1, ADR-0013/N-54: Appendix A задаёт `xsd:token`, тогда как `schema.xsd` ограничивает `XPath` от `xs:string`; по §1.2 выбран prose, конструктор схлопывает XML whitespace и требует непустой результат; Scala-префикс отделяет wire-выражение от validation locator `model.XPath`; полная грамматика XPath — граница M2 |
| §4.9 | Table 4.30 | LaminatingTemperature | `LaminatingTemperature` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`Hot`, `Cold`) для `LaminatingIntent/@Temperature`; golden — `EnumLaws` (M1.6-9) |
| §A.3.22 | Table A.80 | Texture catalog | `NmToken` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый `Catalog.Texture`, 12 рекомендуемых значений; `LaminatingIntent/@Texture` остаётся расширяемым NMTOKEN (M1.6-9, ADR-0007) |
| §A.2.17 | Table A.18 | EmbossDirection | `EmbossDirection` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`Both`, `Depressed`, `Flat`, `Raised`); атрибут `EmbossingItem/@Direction`; golden и машинная сверка — `EnumLaws` (M1.6-10) |
| §A.2.18 | Table A.19 | EmbossType | `EmbossType` | 1 | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`BlindEmbossing`, `Braille`, `EmbossedFinish`, `FoilEmbossing`, `FoilStamping`); атрибут `EmbossingItem/@EmbossingType` (required); golden и машинная сверка — `EnumLaws` (M1.6-10) |
| §A.2.49 | Table A.50 | WorkingDirection | `WorkingDirection` | 1 | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`Bottom`, `Top`); golden и машинная сверка — `EnumLaws`; тип XSD — `EnumTopBottom` (имя таблицы и атрибута нормативны); потребители — `Crease` (M1.6-2), `Cut` (M3) |
| §A.2.23 | Table A.24 | EnumGlue | `EnumGlue` | 1 | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (3 значения: `ColdGlue`, `Hotmelt`, `PUR`); XSD `simpleType EnumGlue`; для атрибутов «Allowed value is from: Glue» (`EdgeGlue`, `SpineGlue`); переименован из `GlueType` в M1.6-3 (ADR-0011) |
| §8.24 | Table 8.29 | GlueType | `GlueType` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (5 значений: `ColdGlue`, `Hotmelt`, `Permanent`, `PUR`, `Removable`); атрибут `Glue/@GlueType`; новый в M1.6-3 (ADR-0011, N-50) |
| §8.24 | Table 8.29 | GluingTechnique | `GluingTechnique` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (3 значения: `SideGluingBack`, `SideGluingFront`, `SpineGluing`); атрибут `Glue/@GluingTechnique`; новый в M1.6-3 |
| §8.25 | Table 8.30 | HoleCenterReference | `HoleCenterReference` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (2 значения: `RegistrationMark`, `TrailingEdge`); атрибут `HolePattern/@CenterReference`; новый в M1.6-5 |
| §8.25 | Table 8.30 | HoleReferenceEdge | `HoleReferenceEdge` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (5 значений: `Bottom`, `Left`, `Pattern`, `Right`, `Top`); атрибут `HolePattern/@ReferenceEdge`; новый в M1.6-5 |
| §8.25 | Table 8.30 | HoleShape | `HoleShape` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (3 значения: `Elliptic`, `Rectangular`, `Round`); атрибут `HolePattern/@Shape`; новый в M1.6-5 |
| §8.25 | Table 8.30 | HolePattern catalog | `NmToken` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог Appendix F (34 значения incl. `None` из XSD) `Catalog.HolePattern`; данные — NMTOKEN, allowed from Section F; новый в M1.6-5 |
| §8.25 | Table 8.30 | HoleReinforcement | `NmToken` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог `Grommet` `Catalog.HoleReinforcement`; `@Reinforcement` NMTOKEN, Values include: Grommet; новый в M1.6-5 |
| §8.7 | Table 8.8 | Certification claim catalog | `XjdfString` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог `Catalog.CertificationClaim` (ADR-0007): `FSC 100%`, `FSC Mix 70%`, `FSC Mix Credit`, `FSC Recycled 85%`, `FSC Recycled Credit`, `PEFC Certified`, `PEFC Recycled` + `pefcPercent(nn)` для `PEFC nn%`; тип данных — string, не NMTOKEN (значения содержат пробелы и `%`); новый в M1.6-1 |
| §8.7 | Table 8.8 | Certification organization catalog | `NmToken` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог `Catalog.CertificationOrganization` (ADR-0007): `CFCC`, `FSC`, `IFCC`, `PEFC` («Values include»); тест расширяемости — `CertificationLaws`; новый в M1.6-1 |
| §4.5.1 | Table 4.23 | PreflightLevel | `PreflightLevel` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`Basic`, `Extended`, `Premium`); атрибут `PreflightItem/@PreflightLevel`; golden и round-trip — `EnumLaws` (M1.6-11) |
| §4.5.2 | Table 4.24 | ProofColorType | `ProofColorType` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`Monochrome`, `BasicColor`, `MatchedColor`); атрибут `ProofItem/@ColorType`; Scala-имя с префиксом — коллизия с `ColorType` Color-ресурса (Приложение C); golden и round-trip — `EnumLaws` (M1.6-11) |
| §6.59 | Table 6.119 | DueLevel | `DueLevel` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`JobCancelled`, `Penalty`, `Trivial`); атрибут `NodeInfo/@DueLevel`; XSD объявляет энумерацию inline на `NodeInfo`, prose и XSD совпадают; до M1.6-8 атрибут был типизирован `Option[Long]`, что делало `DueLevel = 7` представимым (N-52); golden, round-trip и сверка с inline-XSD — `EnumLaws`/`NodeInfoLaws` (M1.6-8) |
| §8.26 | Table 8.31 | FieldEncoding | `FieldEncoding` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`ASCII`, `Barcode`, `Braille`, `RFID`); атрибут `IdentificationField/@Encoding`; XSD объявляет энумерацию inline на `IdentificationField`, prose и XSD совпадают; Scala-имя с префиксом `Field` — `Encoding` слишком общее для домена; golden, round-trip и сверка с inline-XSD — `EnumLaws`/`IdentificationFieldLaws` (M1.6-6) |
| §8.26 | Table 8.31 | FieldPurpose | `FieldPurpose` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`Label`, `Separation`, `Verification`); атрибут `IdentificationField/@Purpose`; XSD объявляет энумерацию inline на `IdentificationField`, prose и XSD совпадают; golden, round-trip и сверка с inline-XSD — `EnumLaws`/`IdentificationFieldLaws` (M1.6-6) |
| §8.26 | Table 8.32 | EncodingDetails catalog | `NmToken` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог `Catalog.EncodingDetails` (ADR-0007), 47 значений Table 8.32 — таблица явно объявлена образцом («Values that are not present in this list MAY be valid»); нормативная опечатка `CODABAR_Tradional` сохранена дословно и закреплена тестом; атрибут `IdentificationField/@EncodingDetails` (M1.6-6) |
| §8.26 | Table 8.31 | PurposeDetails catalog | `NmToken` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог `Catalog.PurposeDetails` (ADR-0007): `ProductIdentification` («Values include»); атрибут `IdentificationField/@PurposeDetails`; тест расширяемости — `IdentificationFieldLaws` (M1.6-6) |
| §8.26.1 | Table 8.36 | BarcodeVersion catalog | `NmToken` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог `Catalog.BarcodeVersion` (ADR-0007): 29 значений `DM_<rows>_by_<columns>` (Table 8.36, для `DATAMATRIX`/`HIBC_DATAMATRIX`) и 40 значений `QR_<n>` (Table 8.37, для `QR`), плюс параметрические конструкторы `dataMatrix(rows, columns)` и `qr(version)`; атрибут `BarcodeDetails/@BarcodeVersion` (M1.6-6) |
| §8.26.1 | Table 8.33 | ErrorCorrectionLevel catalog | `NmToken` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог `Catalog.ErrorCorrectionLevel` (ADR-0007): `PDF417_EC_0`…`PDF417_EC_8` (для `@EncodingDetails="PDF417"`) и `QR_EC_L`/`QR_EC_M`/`QR_EC_Q`/`QR_EC_H` (для `QR`); применимость значения к схеме кодирования — требование к содержимому, отдельным правилом не проверяется (реестр отклонений); атрибут `BarcodeDetails/@ErrorCorrectionLevel` (M1.6-6) |
| §8.26.2 | Table 8.34 | ExtraValues usage catalog | `NmToken` | 1 | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог `Catalog.ExtraValuesUsage` (ADR-0007): `CompositeCode`, `Coupon`, `Supplemental` («Values include»); атрибут `ExtraValues/@Usage` (required); тест расширяемости — `IdentificationFieldLaws` (M1.6-6) |
| §8.30 | Table 8.48 | CostType | `CostType` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`Chargeable`, `NonChargeable`); атрибут `MISDetails/@CostType`; XSD объявляет энумерацию inline на `MISDetails`, prose и XSD совпадают; golden, round-trip и сверка с inline-XSD — `EnumLaws`/`MISDetailsLaws` (M1.6-7) |
| §8.30 | Table 8.48 | WorkType | `WorkType` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum (`Alteration`, `Original`, `Rework`); атрибут `MISDetails/@WorkType`; XSD объявляет энумерацию inline на `MISDetails`, prose и XSD совпадают; golden, round-trip и сверка с inline-XSD — `EnumLaws`/`MISDetailsLaws` (M1.6-7) |
| §8.30 | Table 8.48 | WorkTypeDetails catalog | `NmToken` | `?` | ❌ | ✅ | ❌ | ❌ | Implemented | открытый каталог `Catalog.WorkTypeDetails` (ADR-0007): `CustomerRequest`, `EquipmentMalfunction`, `InternalChange`, `ResourceDamaged`, `UserError` («Values include»); атрибут `MISDetails/@WorkTypeDetails`; тест расширяемости — `MISDetailsLaws` (M1.6-7) |

## Intents (Chapter 4)

| Section | Table | Element/Attribute | Scala type | Cardinality | Validation | Domain tests | XML | JSON | Status | Notes |
|---------|-------|-------------------|------------|-------------|------------|--------------|-----|------|--------|-------|
| §4.1 | Table 4.1 | Intent | `Intent` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | `@Name == payload.elementName` (`Intent.nameLaw`) |
| §4.1 | Table 4.2 | Intent payload dispatch | `IntentPayload` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | закрытый enum: 12 payload + `Extension` escape hatch; `declaredIds` — документные ID внутри интентов (`ProofItem/@ID`, M1.6-11) |
| §4.2 | Table 4.3 | AssemblingIntent | `AssemblingIntent` | `?` | ✅ | ❌ | ❌ | ❌ | Implemented | |
| §4.2 | Table 4.4 | AssemblyItem | `AssemblyItem` | `*` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §4.2 | Table 4.5 | BindIn | `BindIn` | `*` | ✅ | ❌ | ❌ | ❌ | Implemented | `Glue?` → `Option[Glue]` (элемент, M1.6-3/ADR-0011); `Glue/@GlueRef` собирается; SHALL-правила Glue подключены через валидатор |
| §4.2 | Table 4.6 | BlowIn | `BlowIn` | `*` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §4.2 | Table 4.7 | StickOn | `StickOn` | `*` | ✅ | ❌ | ❌ | ❌ | Implemented | `Glue?` → `Option[Glue]` (элемент, M1.6-3/ADR-0011); `Glue/@GlueRef` собирается; SHALL-правила Glue подключены через валидатор |
| §4.3 | Table 4.8 | BindingIntent | `BindingIntent` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | law: парность details ↔ `@BindingType`; запрет `@BindingSide` при `@BindingOrder="None"` |
| §4.3 | Table 4.9 | AdhesiveNote | `AdhesiveNote` | `?` | ✅ | ❌ | ❌ | ❌ | Implemented | `Glue?` → `Option[Glue]` (элемент, M1.6-3/ADR-0011); `Glue/@GlueRef` собирается; SHALL-правила Glue подключены через валидатор |
| §4.3 | Table 4.10 | EdgeGluing | `EdgeGluing` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | |
| §4.3 | Table 4.11 | HardCoverBinding | `HardCoverBinding` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | wire-токен `Glue` (регрессия N-08) |
| §4.3 | Table 4.12 | LooseBinding | `LooseBinding` | `?` | ✅ | ❌ | ❌ | ❌ | Implemented | `HolePattern?` моделируется (M1.6-5); SHALL-правило HolePattern подключено через валидатор |
| §4.3 | Table 4.13 | CoilBinding | `CoilBinding` | `?` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §4.3 | Table 4.14 | CombBinding | `CombBinding` | `?` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §4.3 | Table 4.15 | RingBinding | `RingBinding` | `?` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §4.3 | Table 4.16 | SaddleStitching | `SaddleStitching` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | |
| §4.3 | Table 4.17 | SideStitching | `SideStitching` | `?` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §4.3 | Table 4.18 | SoftCoverBinding | `SoftCoverBinding` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | токен `None` → `Unscored` (Приложение C ROADMAP, реестр отклонений) |
| §4.3 | Table 4.19 | Tabs | `Tabs` | `?` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §4.4 | Table 4.20 | ColorIntent | `ColorIntent` | `?` | ✅ | ❌ | ❌ | ❌ | Implemented | |
| §4.4 | Table 4.21 | SurfaceColor | `SurfaceColor` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | New in XJDF 2.1: `Certification*` → `Chain[Certification]` (Table 8.8) — SHALL-правило подключено через `checkColorIntentCertifications` для обеих поверхностей (`front`/`back`, XSD `maxOccurs="2"`); прочие атрибуты — structural, container-level validation (M1.6-1) |
| §4.5 | Table 4.22 | ContentCheckIntent | `ContentCheckIntent` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | `PreflightItem*`/`ProofItem*` → `Chain` (обе кардинальности `*`, XSD `minOccurs="0"`, пустой интент валиден); IDREF отсутствуют; `ProofItem/@ID` собирается в `declaredIds` через `IntentPayload.declaredIds` (§2.2.3, Table 6.55); процесса `ContentCheck` в главе 5 нет — pairing с `Approval` (§5.3.1) и `Preflight` (§5.4.14), `ProcessType.Preflight` добавлен (M1.6-11) |
| §4.5.1 | Table 4.23 | PreflightItem | `PreflightItem` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | `@PreflightLevel?` → закрытый `PreflightLevel` (M1.6-11) |
| §4.5.2 | Table 4.24 | ProofItem | `ProofItem` | `*` | ✅ | ✅ | ❌ | ❌ | Implemented | 7 атрибутов + `FileSpec?` (вложенный элемент, XSD `maxOccurs="1"` — не IDREF): `@Amount?` integer, `@ColorType?` → закрытый `ProofColorType` (Scala-имя с префиксом: коллизия с `ColorType` Color-ресурса), `@Contract?`/`@HalfTone?` boolean, `@ID?` → документный скоуп (§2.2.3), `@PageIndex?` IntegerRange, `@ProofTarget?` URL (Deprecated 2.1, удержан для декодирования 2.0), `FileSpec` переиспользован из `model/elements` (M1.4-8); SHALL `@ID` при доставке proof — структурно через разрешение IDREF (`checkReferences`, Table 6.55); `Disposition` под `FileSpec` — подключение `dispositionLaw` (Table 8.23, M1.6-11); SHOULD/семантика `@HalfTone`, `@PageIndex`, «нет ProofItem ⇒ нет customer proofs» — не модельные правила (scaladoc) |
| §4.6 | Table 4.25 | EmbossingIntent | `EmbossingIntent` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | `EmbossingItem+` → `NonEmptyChain[EmbossingItem]` (кардинальность `+`, XSD `minOccurs="1"`, структурно); IDREF отсутствуют; SHALL `EmbossingItem/@Separation` ↔ `Color/@ColorType="DieLine"` — глобальная проверка `checkEmbossingColorTypes` (M1.6-10); `ProcessType.Embossing` (§5.6.12) |
| §4.6 | Table 4.26 | EmbossingItem | `EmbossingItem` | `+` | ✅ | ✅ | ❌ | ❌ | Implemented | required `@EmbossingType` → `EmbossType` (структурно); `@Direction?` → `EmbossDirection`; `@Face?` → `Face`; `@FoilColor?` — открытый `NamedColor` (ADR-0007); SHOULD `@FoilColorDetails` ⇒ `@FoilColor` — не ошибка без политики (ADR-0006); SHALL `@Separation`: `Color` для separation SHALL иметь `@ColorType="DieLine"` — `IssueCode.EmbossingColorNotDieLine`, интерпретация: считается `Color` с `Part/@Separation` = значению, отсутствие `@ColorType` — нарушение (M1.6-10) |
| §4.7 | Table 4.27 | FoldingIntent | `FoldingIntent` | `?` | ✅ | ❌ | ❌ | ❌ | Implemented | |
| §4.8 | Table 4.29 | HoleMakingIntent | `HoleMakingIntent` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | `HolePattern+` → `NonEmptyChain[HolePattern]` (кардинальность `+`, структурно); SHALL-правило вложенного `HolePattern` подключено через валидатор (M1.6-12) |
| §4.9 | Table 4.30 | LaminatingIntent | `LaminatingIntent` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | required `@Surface` → `NonEmptyChain[Side]` (структурно); `@Temperature?`, `@Texture?`, `@Thickness?`; IDREF отсутствуют; `ProcessType.Laminating` (M1.6-9) |
| §8.21 | Table 8.26 | Fold | `Fold` | `*` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation; общий для `FoldingIntent` и `FoldingParams` |
| §8.34 | Table 8.53 | Perforate | `Perforate` | `*` | ❌ | ❌ | ❌ | ❌ | Implemented | structural; container-level validation |
| §4.10 | Table 4.31 | LayoutIntent | `LayoutIntent` | `?` | ✅ | ❌ | ❌ | ❌ | Implemented | |
| §4.11 | Table 4.32 | MediaIntent | `MediaIntent` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | `Certification*` → `Chain[Certification]` (Table 8.8, paper certification level) — SHALL подключён через `checkIntentLocalLaws` (M1.6-1) |
| §4.12 | Table 4.33 | ProductionIntent | `ProductionIntent` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | New in XJDF 2.1: `Certification*` → `Chain[Certification]` (Table 8.8) — моделируется, SHALL подключён через `checkIntentLocalLaws` (M1.6-1) |
| §4.14 | Table 4.36 | VariableIntent | `VariableIntent` | `?` | ✅ | ✅ | ❌ | ❌ | Implemented | law: `@MinPages ≤ @AveragePages ≤ @MaxPages` |

## Deliberate Deviations

Ведётся по ROADMAP, Приложение C. Каждое сознательное отклонение имеет
владельца, обоснование, нормативный источник, тест и срок пересмотра;
статус покрытия не может быть «есть case class».

| Deviation | Reason | Compensation | Статус |
|-----------|--------|--------------|--------|
| `PartitionKey.OptionKey` вместо `Option` | коллизия имени со `scala.Option` | `attributeName = "Option"` + тест на wire-имя | реализовано (PR-4) |
| `SeverityClass` вместо `Severity` | коллизия с `@Severity: Int [0..100]` из §5.3.4.1 | документировано в scaladoc | реализовано (PR-5) |
| `HardCoverJacket.GlueApplied` / `Unjacketed` | Scala-имена не совпадают с токенами `Glue` / `None` (Table 4.11) | явный `def token` + golden-множество токенов | реализовано (PR-5) |
| Семейство «→ `None`»: `BindingType.NoBinding` (Table A.8), `BindingOrder.Unbound` (§4.3), `Coating.Uncoated` (Table A.11), `SoftCoverScoring.Unscored` (Table 4.18), `HardCoverJacket.Unjacketed` (Table 4.11) | `None` — зарезервированное имя `scala.None` | явные `token`-маппинги + golden-тест «`→ None` token family» в `laws/EnumLaws.scala`; список полон (машинная сверка M1.2-2) | реализовано (PR-5) |
| `HardCoverJacket.GlueApplied` | Scala-имя не совпадает с токеном `Glue` (Table 4.11, Sheet 1); имя `Glue` занято смыслом «тип клея» (`GlueType`, Table A.24) | явный `def token` без fallback + golden-тест на токен `Glue` (регрессия N-08) | реализовано (PR-5) |
| `DeviceStatus.Cleanup` / `.Setup` и `Status.Cleanup` / `.Setup` — одинаковые имена в разных enum | два разных типа спецификации (Table A.15 и Table A.46), совпадение нормативно | обращение только с явной квалификацией (`DeviceStatus.Setup`); член спецификации не удаляется (ADR-0007) | реализовано (PR-5) |
| `Scope.Device` совпадает по имени с ресурсом `Device` (Table 6.57) | нормативное значение Table A.36 *(New in XJDF 2.2)* | обращение с явной квалификацией `Scope.Device` | реализовано (PR-5) |
| `MediaType` содержит 7 значений с пометкой Deprecated | декодер обязан читать документы с ними (ADR-0010: устаревшие данные не отбрасываются) | пометки только в scaladoc; `@deprecated` не ставится (сборка warning-free) | реализовано (PR-5) |
| `NamedColor` — открытый `NmToken` + `Catalog.NamedColor` | prose (§1.10.3.1) и `schema.xsd` (147 `xs:pattern`) указывают на закрытый список, но §A.2.30 делегирует набор внешнему каталогу `[Color Names]` (SVG 1.1) | зафиксировано в ADR-0007 (часть 3); 147 значений + тест расширяемости; лексическая проверка — в кодеках M2 | реализовано (PR-5) |
| `Sides.Unprinted` и `Scope.Device` отсутствуют в `schema.xsd` | XSD отстаёт от prose Appendix A (обе пометки *New*) | по §1.2 приоритет за текстом; расхождение зафиксировано в ADR-0007 | реализовано (PR-5) |
| `XJDF/@Name` и `@$schema` отсутствуют в домене | JSON Exception, в XML запрещены (Table 3.1, X-04) | реализуются в `codec-json` (M2); статус **codec-only** | codec-only (M2) |
| `Comment/@Text` отсутствует в домене | JSON Exception (Table 8.14) | реализуется в `codec-json` (M2); статус **codec-only** | codec-only (M2) |
| Валидация `RegExp` — только непустота | грамматика XSD-regex несовместима с `java.util.regex`; `schema.xsd` (`regExp`) — `restriction base="xs:string"` без ограничений | валидация непустотой (M1.2-1); полная XSD-грамматика — на стороне кодеков M2 | реализовано (PR-4) |
| `XjdfVersion.from` принимает только `"2.2"` | Table 3.1 требует `"2.2"` для соответствующих спецификации документов, хотя Table A.52 перечисляет `2.0`/`2.1`/`2.2` | scaladoc-объяснение (M1.5-2, PR-13); поддержка 2.0/2.1 — отдельное решение | реализовано (PR-13) |
| `Monoid[Matrix]` вместо `Group` | вырожденная матрица необратима | `inverse: Option[Matrix]` + задокументированная причина; опциональный `InvertibleMatrix` вне M1 | реализовано (PR-12) |
| `Semigroup` (не `Monoid`) для `AuditPool`, `AmountPool`, `NmTokens`, `ProcessPath` | носитель `NonEmptyChain`, кардинальность `T+` запрещает пустое значение | явная запись в scaladoc и в `docs/01`; compile-тест | реализовано (PR-12) |
| Дубликат `"Product"` в `@Types` считается нарушением | §3.1.3 говорит «additional process type tokens»; трактовка «любой второй токен» | зафиксировано как интерпретация + негативный тест (N-36, `XJDF-TYPES-PRODUCT-DUPLICATE`) | реализовано (PR-8, M1.3-4) |
| Пустой `<Certification/>` отвергается, хотя `schema.xsd` его допускает | prose Tables 4.21/4.32/4.33/6.114: «Each Certification SHALL specify a … certification level», а XSD объявляет все три атрибута `use="optional"` — расхождение prose vs XSD, приоритет за текстом (§1.2) | ADR-0012 с цитатами; `IssueCode.CERTIFICATION-LEVEL-MISSING`; негативный тест `CertificationLaws` + позитивные на каждый из трёх атрибутов по отдельности; XSD остаётся тест-оракулом | реализовано (PR-22, M1.6-1) |
| «If more than one Certification is present, at least one of the … levels SHALL be met» не проверяется | правило о фактическом производстве, а не об инварианте документа: XJDF не выражает, какой уровень *выполнен*; кардинальность `*` уже допускает ноль элементов, поэтому «at least one present» тоже не является этим правилом | scaladoc `Certification` и `SurfaceColor`/`MediaIntent`/`ProductionIntent`/`Media`; тест `CertificationLaws`: два `Certification` в `ProductionIntent` оставляют тикет валидным; фикстура `certificationJob` | реализовано (PR-22, M1.6-1) |
| `GangSource/@JobID` и `@BinderySignatureID` не разрешаются корневым валидатором | Table 8.27 типизирует оба атрибута как NMTOKEN, а не IDREF: они указывают на отдельный исходный XJDF и его `BinderySignature`, которых нет в агрегате текущего gang-job; проверяемого предиката без внешнего реестра jobs нет | `GangSource.references = Chain.empty`; scaladoc явно отделяет междокументные идентификаторы от §2.2.3; `GangSourceLaws` проверяет типы XSD и отсутствие IDREF; разрешение переносится на интеграционный слой M4 | реализовано (PR-23, M1.6-4; ADR-0006) |
| `NodeInfo/@PersonalID` не разрешается корневым валидатором | Table 6.119 типизирует атрибут как NMTOKEN и определяет его как `Resource/@ExternalID` контакта; `@ExternalID` — не `@ID`, поэтому ссылка не входит в документный ID-скоуп §2.2.3, и предиката без разрешения Contact-ресурсов нет | `NodeInfo.references` не собирает `@PersonalID`; scaladoc отделяет его от IDREF; `NodeInfoLaws` проверяет отсутствие IDREF у ресурса и его потомков; разрешение — вместе с обходом Contact в M3/M4 | реализовано (PR-25, M1.6-8; ADR-0006) |
| `MetadataMap*` (Table 8.46) не моделировался в срезе `IdentificationField` | элемент общий для `IdentificationField` (Table 8.31) и `RunList` (Table 6.148) и тянет `Expr*`, тип `XPath` и контекстные SHALL | B1 реализовал `XjdfXPath`/`Expr`; B2 реализовал `MetadataMap`, оба wiring и четыре правила в root validator | реализовано и верифицировано владельцем (B2, 419/0) |
| Example 8.7 не включает `MetadataMap/@Name` в родительский `IdentificationField/@ValueTemplate` | буквальный пример (`job doc sheet` против `JobID DocIndex SheetIndex`) нарушает явный SHALL Table 8.31 | ADR-0014/N-55: выбран prose; негативный regression повторяет конфликт, позитивная фикстура добавляет имена mapping в родительский шаблон | реализовано и верифицировано владельцем (M1.6-6b/B2, 419/0) |
| `XjdfXPath` вместо Scala-имени `XPath` для типа данных Table A.1 | в `model.ValidationTypes` уже существует `model.XPath` — внутренний локатор ошибок; одинаковое имя при `model.*` + `prim.*` создало бы коллизии и риск смыслового смешения | wire/spec-имя остаётся `XPath`; новый номинальный тип называется `prim.XjdfXPath`, `Expr/@Path` принимает только его; `model.XPath` не меняется (ADR-0013, N-54) | реализовано и верифицировано владельцем (M1.6-6b/B1, 406/0) |
| Appendix A задаёт `XPath` как `xsd:token`, а `schema.xsd` — restriction от `xs:string` | прямое расхождение prose/XSD: разные whitespace facets; release notes не содержат разъяснения | по §1.2 выбран Table A.1; `XjdfXPath.from` применяет XML whitespace collapse, расхождение закреплено тестом-оракулом; полная грамматика XPath отложена до M2 (ADR-0013, N-54) | реализовано и верифицировано владельцем (M1.6-6b/B1, 406/0) |
| Соответствие `@BarcodeVersion`/`@ErrorCorrectionLevel` значению `@EncodingDetails` не проверяется | Tables 8.33/8.36/8.37 формулируют применимость через «Values include those from … for DATAMATRIX barcodes» и «Each value can be used only for certain values of `IdentificationField/@EncodingDetails`», без SHALL; `@EncodingDetails` — открытый каталог, поэтому полного предиката соответствия не существует | scaladoc `Catalog.BarcodeVersion` и `Catalog.ErrorCorrectionLevel` разделяет семейства значений; `IdentificationFieldLaws` фиксирует состав обоих семейств; ужесточение — только с явной политикой severity (ADR-0006) | реализовано (PR-26, M1.6-6) |
| Table 8.35 (применимость `@Height`/`@Magnification`/`@Ratio` к типам штрихкодов) не моделируется | таблица описывает атрибуты `BarcodeReproParams` — ресурса, отсутствующего в модели; к Table 8.31 она относится только через `@EncodingDetails` | строка ожидает моделирования `BarcodeReproParams` (M3); ссылки на Table 8.35 в коде нет | отложено (M3) |
| `ProofColorType` вместо `ColorType` для `ProofItem/@ColorType` | коллизия с `ColorType` Color-ресурса (Table 6.27) — два разных набора значений; XSD объявляет набор ProofItem inline | wire-токены без изменений (`Monochrome`, `BasicColor`, `MatchedColor`) + golden-тест `EnumLaws`; расхождение Scala-имени — только внутри модели | реализовано (PR-21, M1.6-11) |
| `ProofItem/@ProofTarget` моделируется несмотря на Deprecated in XJDF 2.1 | декодер обязан читать документы XJDF 2.0 (ADR-0010); `FileSpec` (New in 2.1) — замена, но не удаление | scaladoc-пометка; `@deprecated` не ставится (сборка warning-free); та же политика, что для deprecated-значений `MediaType` | реализовано (PR-21, M1.6-11) |
| Интент `ContentCheckIntent` не получает `ProcessType.ContentCheck` | в главе 5 нет процесса `ContentCheck` — интент pairing с `Approval` (§5.3.1) и `Preflight` (§5.4.14); выдумывать токен нельзя (§1.2) | `ProcessType.Preflight` добавлен (§5.4.14, Tables 5.39–5.40); фикстура использует `Types="Approval Preflight"` | реализовано (PR-21, M1.6-11) |

## Version notes

Пометки `New in XJDF 2.1/2.2` в покрытых таблицах (чекер требует их
упоминания в строках реестра):

| Table | Пометка | Статус в модели |
|-------|---------|-----------------|
| Table 6.1 | `@Expires` (2.1) | не моделируется до M3 |
| Table 6.4 | `@Product` (2.1) | моделируется (`Part.product`) |
| Table 6.27 | `@Spectrum`, `ColorMeasurementConditions` (2.1) | не моделируются (M3) |
| Table 6.57 | `@MaxRunSpeed` (2.1), FileSpec CurrentSchema/Schema (2.1), `@RestApiBaseURL` (2.2, JSON Exception) | `@MaxRunSpeed`, `@RestApiBaseURL` моделируются; FileSpec-схемы — M3 |
| Table 6.95 | `@Anchor`, `@ExpansionBox`, `@SheetLay` (2.1) | `@Anchor`, `@SheetLay` моделируются; `@ExpansionBox` — M3 |
| Table 6.114 | `@BackCIE*`, `@BackSpectrum`, `@Spectrum`, `ColorMeasurementConditions` (2.1) | не моделируются (M3) |
| Table 6.148 | `@DocPages` (2.1) | моделируется (`RunList.docPages`) |
| Table 8.19 | `@DescriptiveName`, `@ExternalID`, `@Operations` (2.1) | моделируются (`CutBlock`) |
| Table 4.24 | `@ProofTarget` (2.1, Deprecated), `FileSpec` (2.1, New) | оба моделируются: `proofTarget: Option[Url]` (удержан для декодирования XJDF 2.0, без `@deprecated`-аннотации — политика warning-free) и `fileSpec: Option[FileSpec]` (переиспользование общего элемента, M1.6-11) |
| Table 4.21 | `Certification*` (2.1, Sheet 2) | моделируется (`SurfaceColor.certifications`, M1.6-1) |
| Table 4.33 | `Certification*` (2.1) | моделируется (`ProductionIntent.certifications`, M1.6-1) |

## Decision records (короткие)

### DR-N36 — дубликат `"Product"` в `@Types` (строгая политика)

**Норма.** §3.1.3: «`@Types` of process XJDF SHALL NOT contain the token `"Product"` if any additional process type tokens are present».

**Вопрос.** Запрещает ли норма только смешение `"Product"` с процессными токенами (`"Product Cutting"`), или и чистый дубликат (`"Product Product"`)? Слово «additional» допускает оба толкования.

**Решение (PR-8, M1.3-4).** Принята строгая политика: дубликат `"Product"` отклоняется отдельным кодом `XJDF-TYPES-PRODUCT-DUPLICATE` (`IssueCode.ProductTokenDuplicate`), а смешение с процессными токенами — кодом `XJDF-TYPES-PRODUCT-MIXED` (`IssueCode.ProductTokenMixed`). Обоснование: `@Types` — упорядоченный список процессов (§5.2, `ProcessPath`); идентификатор процесса `"Product"` не несёт процессной семантики при повторении, и его дублирование указывает на ошибку отправителя. Это интерпретация, а не дословная норма, поэтому: (1) выделен отдельный `IssueCode`, (2) добавлен негативный тест `N-36: duplicate "Product" token in @Types is rejected`, (3) запись остаётся в реестре отклонений.

### DR-DomainRule — форма локальных законов (ADR-0003, M1.3-3)

**Контекст.** Ряд локальных инвариантов (`Intent.isLawful`, `BindingIntent.isLawful`, `VariableIntent.isLawful`, `PartWaste.isLawful`, `Disposition.isLawful`, `Product.hasLawfulAmounts`, `Notification.hasLawfulMilestone/hasUniqueCommentLanguages`, `ResourceSet.hasLawfulChildren/hasLawfulStatuses`) был реализован как `Boolean`-предикаты; часть из них не была подключена к корневому валидатору (N-18).

**Решение (PR-8).** Все локальные законы приведены к контракту ADR-0003 `trait DomainRule[-A]: def check(value: A, at: XPath): Chain[Issue]` и явно вызываются из `TicketValidator.checkLocalLaws`. Каждый закон возвращает структурированный `Issue` со стабильным `IssueCode`, severity и XPath. `Boolean`-предикаты сохранены как производные аксессоры там, где их использует DSL (`Intent.isLawful`) или тесты, но они больше не являются первичной формой закона. Глобальные правила (ID/IDREF, §3.4, BOM, хронология) остаются в `TicketValidator`; решение владельца — рефакторить все предикаты сразу (не оставлять legacy).

**Прим.:** после механического переноса `Disposition` в `model.elements` (M1.4-8, PR-14) правило остаётся в `TicketValidator.dispositionLaw`, чтобы не смешивать перемещение типа с изменением поведения; хук в обходе ресурсов подключается при реализации FileSpec-несущих ресурсов (M1.6/M3).

### DR-M1.5-3 — conformance-сьют примеров живёт в `laws`

**Контекст (PR-13).** Примеры спецификации (`SpecExamples`) построены в
`modules/examples` и запускались только из `main` плюс smoke-сьют в
`modules/examples/src/test`. ROADMAP M1.5-3 требует перенести их проверку в
регулярный тест-сьют `laws/SpecExamplesSuite.scala` с именованными тестами
(номер раздела/таблицы) и golden-литералами `Show`-рендеров.

**Решение.** `modules/examples` остаётся чисто демонстрационным (`Main` +
построители примеров); conformance-сьют переезжает в `modules/laws`, поэтому
`laws` объявляет зависимость от `examples` (`build.sbt`: `.dependsOn(core,
examples)`). Тестовая конфигурация (`munit`, `testFrameworks`) из `examples`
удалена как неиспользуемая. Направление зависимостей: `examples → core`,
`laws → {core, examples}` — циклов нет. Golden-тесты временные (M1.5-3):
канонические XML/JSON golden заменят их в M2.

### DR-M1.4-1 — разрыв цикла зависимостей валидации (ADR-0002, N-21)

**Норма.** ADR-0002: фундамент валидации — файл с Fan-Out 0; `Ticket.scala` не зависит от реализации `Patch`; корневой валидатор агрегирует правила; повторный анализ зависимостей — 0 циклов.

**Решение (PR-9).** `model/ValidationTypes.scala` создан и содержит `Issue`, `IssueCode`, `SeverityClass`, `XPath`, `trait DomainRule[-A]`, `type ValidationResult[A] = ValidatedNec[Issue, A]`, `ValidationReport`; импортирует только `prim.*` и cats. По решению владельца список ADR-0002 выполнен буквально: `IssueCode`, `SeverityClass`, `XPath` перенесены из `prim` (`Tokens.scala`, `Enums.scala`) в слой валидации. `Validation.scala` переименован в `TicketValidator.scala`. Для нуля циклов из `Ticket.scala` убраны `XJDF.validate`, `XJDF.validateReport` (стали extension-методами в `TicketValidator.scala`) и `XJDF.withPatch` (extension в `Patch.scala`). Повторный анализ зависимостей: 0 циклов.

## Сопровождение

- Чекер: `bash scripts/check-spec-coverage.sh` (exit 0 = OK). Запускается в CI,
  как только CI вернётся по решению владельца (M1.0-1).
- Новый ресурс/интент (шаблон вертикального среза M1.6/M3) обязан добавить
  строку реестра в том же PR — обратная проверка чекера это ловит.
- Каждое изменение публичного API с изменением spec-mapping обновляет этот
  документ и scaladoc в том же PR (§15).
- Golden-покрытие канонических XML/JSON рендеров появляется в M2 и заменит
  временные `Show`-golden conformance-сьюта примеров (M1.5-3).
