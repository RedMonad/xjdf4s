# xjdf4s

Доменная модель [XJDF 2.2](https://www.cip4.org) (Exchange Job Definition
Format, CIP4) на Scala 3.

XJDF — формат обмена данными для полиграфических рабочих процессов: описание
продукта (`ProductList`), производственных инструкций (`ResourceSet`) и записей
исполнения (`AuditPool`) в виде одной транзакции между Controller и Device.

> **Статус: активный рефакторинг / pre-alpha.**
> Ядро проходит стабилизацию по результатам независимого аудита конформности.
> Публичный API нестабилен, кодеки XML/JSON в разработке. Использование в
> production не рекомендуется.

## Быстрый старт

Требуется JDK 21 и sbt 2.x.

```bash
sbt compile          # собрать
sbt test             # законы и conformance-примеры
sbt examples/run     # демо: примеры спецификации, BOM, change order
```

## Модули

| Модуль | Артефакт | Содержимое |
| --- | --- | --- |
| `modules/core` | `xjdf4s-core` | примитивы, модель, ресурсы, интенты, DSL, валидатор |
| `modules/laws` | `xjdf4s-laws` | законы структур (munit + ScalaCheck) и conformance-сьюты |
| `modules/examples` | `xjdf4s-examples` | демо примеров спецификации |

## План работ

Операционный план рефакторинга — в каталоге [`roadmap/`](roadmap/00-Contract.md).
Каждая фаза — отдельный файл; текущая фаза M1 переоткрыта по результатам аудита.

Нормативная база — `reference/xjdf/*` (главы 1–9, Appendix A–H, `schema.xsd`).