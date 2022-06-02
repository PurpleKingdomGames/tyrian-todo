package tyriantodo

enum Msg:
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
  case NoOp
  case Load(data: String)
  case Save
  case Log(message: String)
  case ChangeFilter(to: ToDoFilter)
