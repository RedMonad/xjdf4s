Ниже — компактный раздел, который можно сразу вставить в документацию.

---

# Работа с XSD через `xsdq.py`

`xsdq.py` — инструмент для навигации по заранее сгенерированному индексу XSD.  
Он используется вместо чтения исходного XSD-файла и позволяет быстро находить элементы, типы, атрибуты, связи и подсказки для генерации Scala 3 моделей.

Инструмент работает с JSON-индексом, например `xsd-index.json`, и не обращается к XSD напрямую во время запросов.

---

## Запуск

```bash
python xsdq.py --help
```

Если индекс лежит не в стандартном месте, указывайте его явно:

```bash
python xsdq.py summary --index path/to/xsd-index.json
```

Все команды возвращают JSON.

Для экономии контекста агента рекомендуется использовать:

```bash
--compact
```

---

## Основные команды

### 1. Краткая информация об индексе

```bash
python xsdq.py summary --index xsd-index.json
```

Показывает:

- количество элементов, типов, атрибутов;
- корневые кандидаты;
- meta-информацию.

---

### 2. Поиск сущностей

```bash
python xsdq.py search "Order" --index xsd-index.json
```

Поиск по имени, ID, namespace, аннотации.

Фильтр по типу сущности:

```bash
python xsdq.py search "Order" --kind element --index xsd-index.json
```

Компактный вывод:

```bash
python xsdq.py search "Order" --kind element --compact --index xsd-index.json
```

---

### 3. Получение сущности

```bash
python xsdq.py get "element:{urn:example}Order" --index xsd-index.json
```

Возвращает:

- сам узел;
- его тип;
- исходящие связи;
- входящие использования.

Если точный ID неизвестен, сначала используйте `search`.

---

### 4. Effective children элемента или типа

```bash
python xsdq.py children "element:{urn:example}Order" --index xsd-index.json
```

С рекурсией:

```bash
python xsdq.py children "element:{urn:example}Order" --depth 2 --index xsd-index.json
```

Команда возвращает реальные дочерние поля с учетом:

- `xs:sequence`;
- `xs:choice`;
- `xs:all`;
- `xs:group`;
- `xs:extension`;
- `xs:restriction`;
- anonymous types.

---

### 5. Effective attributes

```bash
python xsdq.py attrs "complexType:{urn:example}OrderType" --index xsd-index.json
```

Возвращает атрибуты с учетом:

- `xs:attributeGroup`;
- `xs:extension`;
- `xs:restriction`;
- `use`, `default`, `fixed`.

---

### 6. Кто использует сущность

```bash
python xsdq.py used-by "complexType:{urn:example}AddressType" --index xsd-index.json
```

Полезно, чтобы понять, можно ли менять тип и где он встречается.

---

### 7. Что использует сущность

```bash
python xsdq.py uses "complexType:{urn:example}OrderType" --index xsd-index.json
```

---

### 8. Иерархия типов

```bash
python xsdq.py hierarchy "complexType:{urn:example}PaymentType" --index xsd-index.json
```

Показывает:

- базовые типы;
- наследников;
- substitution members.

---

### 9. Контекст для генерации кода

Самая полезная команда для агента:

```bash
python xsdq.py bundle "element:{urn:example}Order" \
  --depth 2 \
  --scala \
  --compact \
  --index xsd-index.json
```

Возвращает:

- сам элемент;
- связанные типы;
- дочерние элементы;
- атрибуты;
- простые типы;
- enumeration;
- Scala-подсказки.

---

### 10. Scala-подсказки для конкретного узла

```bash
python xsdq.py scala-hints "complexType:{urn:example}OrderType" --index xsd-index.json
```

С вложенными типами:

```bash
python xsdq.py scala-hints "complexType:{urn:example}OrderType" --depth 1 --index xsd-index.json
```

---

## Рекомендуемый сценарий для AI-агента

Агент должен использовать следующий порядок действий:

```bash
# 1. Понять структуру схемы
python xsdq.py summary --index xsd-index.json --compact

# 2. Найти нужный элемент или тип
python xsdq.py search "Order" --kind element --compact --index xsd-index.json

# 3. Получить карточку сущности
python xsdq.py get "element:{urn:example}Order" --compact --index xsd-index.json

# 4. Получить контекст для генерации Scala-модели
python xsdq.py bundle "element:{urn:example}Order" --depth 2 --scala --compact --index xsd-index.json
```

Если нужно проверить отдельный тип:

```bash
python xsdq.py scala-hints "complexType:{urn:example}OrderType" --depth 1 --compact --index xsd-index.json
```

---

## Правила использования в промпте агента

Можно включить в системную инструкцию:

> Для работы с XSD не читай XSD-файл напрямую.  
> Используй инструмент `xsdq.py`.  
> Индекс находится в файле `xsd-index.json`.  
> Для поиска сущностей используй `search`.  
> Для получения контекста перед генерацией Scala-модели используй `bundle --scala --compact`.  
> Если ID неоднозначен, сначала получи точный ID через `search`.

---

## Полезные флаги

| Флаг | Назначение |
|---|---|
| `--index path/to/xsd-index.json` | путь к индексу |
| `--compact` | компактный JSON, экономит контекст |
| `--kind element` | фильтр по типу сущности |
| `--depth N` | глубина рекурсии |
| `--limit N` | ограничить число результатов |
| `--scala` | добавить Scala-подсказки |
| `--brief` | краткие узлы вместо полных |

---

## Типичные ошибки

### `Index not found`

Индекс не найден. Нужно указать путь явно:

```bash
python xsdq.py summary --index path/to/xsd-index.json
```

Или перегенерировать индекс.

---

### `ambiguous reference`

Имя найдено в нескольких сущностях. Нужно использовать точный ID из результата `search`.

Пример:

```bash
python xsdq.py search "Address" --compact --index xsd-index.json
```

Затем:

```bash
python xsdq.py get "complexType:{urn:example}Address" --compact --index xsd-index.json
```

---

### `not found`

Сущность отсутствует в индексе или имя указано неточно. Проверьте:

```bash
python xsdq.py search "часть_имени" --compact --index xsd-index.json
```

---

## Короткая шпаргалка

```bash
#_summary
python xsdq.py summary --index xsd-index.json --compact

# search
python xsdq.py search "Order" --compact --index xsd-index.json

# get
python xsdq.py get "element:{urn:example}Order" --compact --index xsd-index.json

# children
python xsdq.py children "element:{urn:example}Order" --depth 2 --compact --index xsd-index.json

# attributes
python xsdq.py attrs "complexType:{urn:example}OrderType" --compact --index xsd-index.json

# usage
python xsdq.py used-by "complexType:{urn:example}AddressType" --compact --index xsd-index.json

# bundle for Scala generation
python xsdq.py bundle "element:{urn:example}Order" --depth 2 --scala --compact --index xsd-index.json
```