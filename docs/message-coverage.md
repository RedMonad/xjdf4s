# XJMF message coverage

Generated from the XSD element list obtained with `xsdq.py list --kind element`.

| Message | Status |
|---|---|
| `CommandForceGang` | typed |
| `CommandModifyQueueEntry` | typed |
| `CommandPipeControl` | typed |
| `CommandRequestQueueEntry` | typed |
| `CommandResource` | typed |
| `CommandResubmitQueueEntry` | typed |
| `CommandReturnQueueEntry` | typed |
| `CommandShutDown` | typed |
| `CommandStopPersistentChannel` | typed |
| `CommandSubmitQueueEntry` | typed |
| `CommandWakeUp` | typed |
| `QueryGangStatus` | typed |
| `QueryKnownDevices` | typed |
| `QueryKnownMessages` | typed |
| `QueryKnownSubscriptions` | typed |
| `QueryNotification` | typed |
| `QueryQueueStatus` | typed |
| `QueryResource` | typed |
| `QueryStatus` | typed |
| `ResponseForceGang` | typed |
| `ResponseGangStatus` | typed |
| `ResponseKnownDevices` | typed |
| `ResponseKnownMessages` | typed |
| `ResponseKnownSubscriptions` | typed |
| `ResponseModifyQueueEntry` | typed |
| `ResponseNotification` | typed |
| `ResponsePipeControl` | typed |
| `ResponseQueueStatus` | typed |
| `ResponseRequestQueueEntry` | typed |
| `ResponseResource` | typed |
| `ResponseResubmitQueueEntry` | typed |
| `ResponseReturnQueueEntry` | typed |
| `ResponseShutDown` | typed |
| `ResponseStatus` | typed |
| `ResponseStopPersistentChannel` | typed |
| `ResponseSubmitQueueEntry` | typed |
| `ResponseWakeUp` | typed |
| `SignalGangStatus` | typed |
| `SignalKnownDevices` | typed |
| `SignalKnownSubscriptions` | typed |
| `SignalNotification` | typed |
| `SignalQueueStatus` | typed |
| `SignalResource` | typed |
| `SignalStatus` | typed |

Current coverage: **44/44** concrete XJMF messages.

## Notes

- "typed" means **entity-name coverage**: all 44 concrete XJMF messages are Scala ADTs inside the closed
  `StandardMessage` union, while the message traits stay open for foreign-namespace extensions.
- `ChannelMode` is the normative Table A.10 pair `FireAndForget | Reliable`. `Subscription/@ChannelMode` is an
  **ordered list** (most preferred first), `Signal/@ChannelMode` is a single value.
- The `Query` trait no longer forces `@Languages`: it is carried only where the concrete table defines it
  (QueryNotification, QueryKnownDevices, QueryResource, QueryStatus). QueryGangStatus, QueryQueueStatus,
  QueryKnownMessages and QueryKnownSubscriptions do not expose it, following their tables (7.13/7.40/7.22).
- XJDF 2.2 field coverage: `ResourceQuParams/@Types` (normative element name restored), `SignalResource/@ReplaceAfter`
  and `@ReplaceBefore` with window validation, `SubscriptionInfo/@Languages` and NMTOKEN `@ChannelID`.
- `Subscription/@Languages` remains available for deprecated backward compatibility (Table 7.5).
- The JSON `$schema`/`@Name` members and the JSON exactly-one-message restriction are codec-layer policy.
