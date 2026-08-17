package xjdf4s.messaging

/** Complete unions of the 44 XJMF 2.2 message elements. Traits remain open for foreign-namespace extensions. */
type StandardCommand =
  CommandForceGang | CommandModifyQueueEntry | CommandPipeControl | CommandRequestQueueEntry | CommandResource |
    CommandResubmitQueueEntry | CommandReturnQueueEntry | CommandShutDown | CommandStopPersistentChannel |
    CommandSubmitQueueEntry | CommandWakeUp

type StandardQuery =
  QueryGangStatus | QueryKnownDevices | QueryKnownMessages | QueryKnownSubscriptions | QueryNotification |
    QueryQueueStatus | QueryResource | QueryStatus

type StandardResponse =
  ResponseForceGang | ResponseGangStatus | ResponseKnownDevices | ResponseKnownMessages |
    ResponseKnownSubscriptions | ResponseModifyQueueEntry | ResponseNotification | ResponsePipeControl |
    ResponseQueueStatus | ResponseRequestQueueEntry | ResponseResource | ResponseResubmitQueueEntry |
    ResponseReturnQueueEntry | ResponseShutDown | ResponseStatus | ResponseStopPersistentChannel |
    ResponseSubmitQueueEntry | ResponseWakeUp

type StandardSignal =
  SignalGangStatus | SignalKnownDevices | SignalKnownSubscriptions | SignalNotification | SignalQueueStatus |
    SignalResource | SignalStatus

type StandardMessage = StandardCommand | StandardQuery | StandardResponse | StandardSignal
