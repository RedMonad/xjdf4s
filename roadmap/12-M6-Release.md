# M6 — Публикация и эксплуатационная готовность

Статус: `[ ]` BLOCKED (лицензия)

## Артефакты
`xjdf4s-core`, `xjdf4s-codec-core`, `xjdf4s-codec-xml`, `xjdf4s-codec-json`,
`xjdf4s-messaging`, опционально `xjdf4s-workflow-fs2`, `xjdf4s-laws` как testkit.
Обязательны: LICENSE, утверждённая владельцем; developers/SCM metadata; подпись;
настроенный Maven Central workflow. Секреты не хранятся в Git.

## Совместимость
До `1.0.0` breaking changes перечисляются в release notes; после фиксации
публичной поверхности — MiMa или эквивалент для Scala 3; версия спецификации
XJDF не смешивается с semver библиотеки; deprecated API живёт минимум
объявленный minor-цикл.

## Документация (pre-publishing)
Scaladoc-сайт; type-checked tutorials; migration guide; матрица «фича XJDF 2.2 →
уровень поддержки»; cookbook. Проработка документации — отдельная задача,
выполняется перед публикацией, дублирует официальный документ XJDF.

## Корпус, производительность, безопасность
Легально используемый публичный корпус CIP4 (лицензия каждой фикстуры
проверяется); JMH-бенчмарки decode/encode/validation; fuzzing парсеров и
декодеров; review по entity expansion, oversized input, глубине рекурсии,
catastrophic regex и обработке URL.

## DoD M6
- Tagged workflow публикует подписанные артефакты; доступны source/docs jars.
- Compatibility gate зелёный; корпус и бенчмарки имеют baseline.
- Первый stable release имеет полный changelog.