# M4 — XJMF (глава 7) и транспорт

Статус: `[ ]`

## Чистая messaging-модель
Отдельный `modules/messaging`: `XJMF`, `Header` с корректными message/sender-
скоупами идентификаторов, четыре семейства `Query`/`Command`/`Response`/`Signal`
как enum-иерархия, type-safe payload поддержанных сообщений главы 7, escape
hatch для расширений. `core` не зависит от `messaging`.

## Выравнивание сообщений и аудитов
Продолжить Table 3.2 «Alignment of Audits and Messages»: `Signal → Audit`
(реализовано через `Alignment`), добавить `CommandReturnQueueEntry →
AuditProcessRun` тем же приёмом, с законом на каждый case. Свёртка потока
сигналов в хронологический `AuditPool` с явной политикой дубликатов и
out-of-order. Утверждения о естественности — только для реально заданных
functor mappings.

## Кодеки XJMF
Расширить модули кодеков либо добавить sibling-модули; не смешивать XJMF и
XJDF root-дискриминаторы. Golden-фикстуры — из главы 7.

## Эффектный транспорт
`transport-http` реализует REST §9.10.3/§9.10.4: граница `Kleisli`/tagless-final;
Submit/Return QueueEntry, KnownDevices; политика timeout/retry/idempotency;
in-memory интерпретатор для тестов. Здесь же проверяется ChangeOrder-документ
(`CommandResubmitQueueEntry`, §1.6.5) и появляется честная демонстрация
intersection types.

## DoD M4
- Обмены главы 7 декодируются, валидируются и кодируются обратно.
- message-ID и document-ID скоупы не смешиваются.
- Транспорт тестируем без сети.