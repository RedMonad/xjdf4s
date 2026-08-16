package xjdf4s.core

enum ValidationError derives CanEqual:
  case EmptyValue(field: String)
  case EmptyCollection(field: String)
  case InvalidValue(field: String, value: String, expected: String)
end ValidationError
