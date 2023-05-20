package tyriantodo

enum Msg:

  // List management
  case NewEditingValue(value: String)
  case SubmitTodo
  case ToggleCompleted(id: Int)
  case RemoveItem(id: Int)
  case EditItem(id: Int, elementId: String)
  case StopEditing(id: Int)
  case StopEditingAll
  case EditingItemValue(value: String)
  case MarkAll(asComplete: Boolean)
  case ClearCompleted

  // Persistance
  case Load(data: String)
  case Save

  // Routing
  case ChangeFilter(to: ToDoFilter)
  case FollowLink(externalHref: String)

  // Utility
  case Log(message: String)
  case NoOp
