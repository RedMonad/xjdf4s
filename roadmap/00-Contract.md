# 00 — Контракт документа

## Роль
`roadmap/` — операционный план рефакторинга. Каждая фаза — отдельный файл,
самодостаточный для работы AI-агента в одной сессии. При расхождении плана и
реальности агент:
1. проверяет нормативный текст XJDF 2.2 в `reference/xjdf/*`;
2. проверяет актуальный код и воспроизводимый тест;
3. обновляет файл фазы, а не ведёт параллельный план.

## Приоритет источников истины
При расхождении сведений действует строгий порядок:
1. нормативный текст XJDF 2.2 (`reference/xjdf/*`, главы 1–9, Appendix A–H);
2. release notes XJDF 2.1/2.2 (`Appendix H`);
3. `schema.xsd` — структурный oracle для имён, кардинальностей, XSD-типов;
4. нормативные примеры XJDF (блоки `Example N.M`);
5. воспроизводимые тесты;
6. актуальный исходный код;
7. этот план.

Если prose и XSD расходятся — выбор фиксируется явно (цитаты + фикстура),
приоритет за текстом. Молчаливый выбор запрещён.

## Статусы
| Маркер | Значение |
| --- | --- |
| `[ ]` | не начато / не подтверждено |
| `[~]` | частично, критерии приёмки не пройдены |
| `[x]` | подтверждено тестами и прогоном |
| `BLOCKED` | есть внешняя зависимость или решение владельца |

Пункт нельзя отметить `[x]` только потому, что «код выглядит правильным».

## Шкала приоритетов
| Приоритет | Смысл |
| --- | --- |
| P0 | Ломает корректность ядра или примеры спецификации |
| P1 | Нарушение конформности XJDF 2.2: типы, кардинальности, токены, валидация |
| P2 | Архитектура, алгебры, безопасность публичного API, мёртвый код |
| P3 | Тестовая инфраструктура, developer experience |
| P4 | Гигиена репозитория, лицензия |

## Ограничение верификации
Выводы аудита получены статическим анализом. Утверждения «компилируется» /
«тесты зелёные» требуют машинной верификации (прогон `sbt`).

## Конвенции
- Один PR = один пункт фазы (или тесно связанная пара).
- На каждый баг — сначала падающий регрессионный тест, потом исправление.
- Breaking change сопровождается migration note и полным списком call sites.
- Каждое SHALL — негативный тест; SHOULD/MAY не превращаются в безусловные ошибки.
- Нет скрытых исключений в safe API: бросающие методы содержат `unsafe` в имени.
- Языки: scaladoc — английский; файлы `roadmap/` — русский.

## Работа с XSD: `xsdq.py` и `xsdgen.py`

Инструменты лежат в `reference/xjdf/tool/`.

**Для работы с XSD агент НЕ читает `schema.xsd` напрямую.**
Используется `xsdq.py` — навигация по предгенерированному JSON-индексу.

### Генерация индекса (`xsdgen.py`)

```bash
python reference/xjdf/tool/xsdgen.py \
  reference/xjdf/schema.xsd \
  -o reference/xjdf/tool/xsd-index.json
```

Запускать только при изменении `schema.xsd`. В штатном режиме индекс уже
присутствует.

### Запрос к индексу (`xsdq.py`)

Все команды возвращают JSON. Для экономии контекста всегда добавлять `--compact`.

```bash
# Краткая сводка индекса
python reference/xjdf/tool/xsdq.py summary \
  --index reference/xjdf/tool/xsd-index.json --compact

# Поиск сущностей (по имени, ID, namespace, аннотации)
python reference/xjdf/tool/xsdq.py search "Certification" \
  --index reference/xjdf/tool/xsd-index.json --compact

# Карточка сущности (узел + тип + связи)
python reference/xjdf/tool/xsdq.py get "element:{http://www.CIP4.org/JDFSchema_2_0}Certification" \
  --index reference/xjdf/tool/xsd-index.json --compact

# Effective children (с учётом sequence/choice/all/group/extension/restriction)
python reference/xjdf/tool/xsdq.py children "element:{http://www.CIP4.org/JDFSchema_2_0}Certification" \
  --depth 2 --index reference/xjdf/tool/xsd-index.json --compact

# Effective attributes (с учётом attributeGroup/extension/restriction, use/default/fixed)
python reference/xjdf/tool/xsdq.py attrs "complexType:{http://www.CIP4.org/JDFSchema_2_0}CertificationType" \
  --index reference/xjdf/tool/xsd-index.json --compact

# Кто использует / что использует
python reference/xjdf/tool/xsdq.py used-by  "complexType:{...}AddressType" --index ... --compact
python reference/xjdf/tool/xsdq.py uses     "complexType:{...}OrderType"   --index ... --compact

# Иерархия типов (базовые, наследники, substitution members)
python reference/xjdf/tool/xsdq.py hierarchy "complexType:{...}PaymentType" --index ... --compact

# Контекст для генерации Scala-модели (самая полезная команда)
python reference/xjdf/tool/xsdq.py bundle "element:{...}Certification" \
  --depth 2 --scala --compact --index reference/xjdf/tool/xsd-index.json

# Scala-подсказки для конкретного узла
python reference/xjdf/tool/xsdq.py scala-hints "complexType:{...}OrderType" \
  --depth 1 --compact --index reference/xjdf/tool/xsd-index.json
```

### Рекомендуемый порядок действий

```
summary → search → get → bundle --scala --compact
```

Если ID неоднозначен — сначала `search`, затем точный ID из результата.

### Ключевые флаги

| Флаг | Назначение |
|---|---|
| `--index <path>` | путь к `xsd-index.json` |
| `--compact` | компактный JSON (всегда использовать) |
| `--kind element\|type\|attribute` | фильтр поиска |
| `--depth N` | глубина рекурсии (`children`, `bundle`, `scala-hints`) |
| `--limit N` | ограничить число результатов |
| `--scala` | добавить Scala-подсказки (`bundle`, `scala-hints`) |
| `--brief` | краткие узлы вместо полных |

### Типичные ошибки

| Ошибка | Действие |
|---|---|
| `Index not found` | Указать `--index` явно или перегенерировать через `xsdgen.py` |
| `ambiguous reference` | Имя найдено в нескольких сущностях → уточнить через `search` |
| `not found` | Сущность отсутствует в индексе или имя неточно → `search "часть_имени"` |

## Фазы
| Файл | Фаза | Статус |
| --- | --- | --- |
| `01-M1.0-Baseline.md` | Воспроизводимая сборка | `[~]` |
| `02-M1.1-Critical-Correctness.md` | Критическая корректность | `[~]` переоткрыта |
| `03-M1.2-Types-Tokens-Cardinalities.md` | Типы, токены, кардинальности | `[~]` переоткрыта |
| `04-M1.3-Root-Validator.md` | Корневой валидатор | `[~]` переоткрыта |
| `05-M1.4-Architecture-Safe-API.md` | Архитектура и safe API | `[~]` переоткрыта |
| `06-M1.5-Tests-And-Laws.md` | Тесты и законы | `[~]` переоткрыта |
| `07-M1.6-Chapter-4-8-Gaps.md` | Пробелы глав 4 и 8 | `[~]` переоткрыта |
| `08-M2-Codecs.md` | Кодеки XML/JSON | `[ ]` |
| `09-M3-Chapter-6-Catalog.md` | Каталог главы 6 | `[ ]` |
| `10-M4-XJMF-Transport.md` | XJMF и транспорт | `[ ]` |
| `11-M5-Workflow.md` | Workflow | `[ ]` |
| `12-M6-Release.md` | Публикация | `[ ]` BLOCKED (лицензия) |

Порядок фаз — порядок зависимостей, не календарь. M1 переоткрыт по результатам
консолидированного аудита (три независимых ревью): обнаружен P0-дефект BOM и
ряд P1-расхождений конформности, которые необходимо закрыть до заморозки API M2.
