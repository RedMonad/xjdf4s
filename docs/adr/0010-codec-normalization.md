# ADR-0010 — Нормализация кодеков и сохранение расширений

- **Статус:** принято (направление); детали фиксируются до заморозки API M2
- **Дата:** 2026-08-15
- **Задача:** M2.2 (дедлайн по ROADMAP §6 — «до заморозки API M2»)
- **Связанные ADR:** ADR-0007 (JSON Exceptions вне домена), ADR-0008 (payload)

---

## Context

Wire-формат (XML/JSON) отличается от доменной модели: JSON Exceptions
(`XJDF/@Name`, `@$schema`, `Comment/@Text`, `@Types`-массив, `AuditPool`-массив
с `Name`), порядок элементов, namespace-префиксы, лексические формы значений.
Без явной политики декодеры либо теряют данные, либо делают round-trip
недетерминированным.

## Decision

Round-trip формулируется как

```
decode(encode(a))     = Right(normalize(a))
encode(decode(bytes)) = canonicalize(bytes)
```

До заморозки API M2 определяются:

- значения по умолчанию;
- различие «отсутствует» vs «явно задан default»;
- порядок атрибутов и дочерних элементов;
- namespace-префиксы;
- JSON-only дискриминаторы;
- канонические лексические формы;
- политика foreign namespaces.

Если foreign extensions должны быть lossless, вводится raw extension AST —
неизвестные данные нельзя молча отбрасывать (устаревшие токены `MediaType` с
пометкой Deprecated — частный случай того же правила: декодер обязан их
читать, см. реестр отклонений).

## Alternatives

| Вариант | Суть | Оценка |
| --- | --- | --- |
| Lossy-декодеры («неизвестное выбросить») | Просто | Нарушает §1.10.3.2 (открытые списки) и политику расширений |
| Нормализация ad hoc по месту | Каждый кодек сам решает | Два кодека разойдутся в канонических формах |
| **Явные `normalize`/`canonicalize`** | Принято | Один контракт для XML и JSON; тестируемо property-тестами |

## Consequences

- Round-trip-тесты сравнивают нормализованную модель, а не сырые байты
  (ROADMAP §12.2).
- JSON Exceptions реализуются в `codec-json` (M2) и не протекают в домен
  (ADR-0007): `XJDF/@Name`, `@$schema`, `Comment/@Text`, `@Types`-массив,
  `AuditPool`-массив.
- Golden-фикстуры M2 — канонические XML/JSON; временные `Show`-golden
  примеров (M1.5-3) заменяются ими.

## Normative references

- Table 3.1 (JSON Exceptions `@Name`/`@$schema`), Table 8.14 (`Comment/@Text`)
  — `reference/xjdf/3 – Structure.md`, `reference/xjdf/8 – Subelements.md`
- §1.4, §1.4.2 (кодирования XML/JSON) — `reference/xjdf/1 – Introduction.md`
- ROADMAP §6 (ADR-0010), §12.2, Приложение C (реестр отклонений)

## Migration impact

Нет до M2: доменная модель не меняется. При заморозке API M2 фиксируются
все пункты списка выше; отклонения от них — обновление этого ADR.

**Срок пересмотра:** M2.2 (заморозка контрактов кодеков).
