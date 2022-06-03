package tyriantodo

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

final case class Model(
    config: Config,
    editingValue: String,
    editingItemValue: String,
    todos: List[TodoItem],
    idCount: Int,
    filter: ToDoFilter
):
  def currentlyEditing: Boolean =
    todos.exists(_.editing)

  def allComplete: Boolean =
    todos.forall(_.completed)

  def anyComplete: Boolean =
    todos.exists(_.completed)

  def serialise: String =
    todos.asJson.noSpaces

  def withFilter(newFilter: ToDoFilter): Model =
    this.copy(filter = newFilter)

object Model:
  def initial(config: Config): Model =
    Model(config, "", "", Nil, 0, ToDoFilter.All)

  def fromSaveData(data: String): Model =
    val l =
      decode[List[TodoItem]](data).toOption
        .getOrElse(Nil)
        .zipWithIndex
        .map { case (todo, i) =>
          todo.copy(id = i)
        }

    Model(
      Config.initial(true),
      "",
      "",
      l,
      l.length,
      ToDoFilter.All
    )

enum ToDoFilter:
  case All, Active, Completed

object ToDoFilter:
  def fromString(hash: String): ToDoFilter =
    hash match
      case "#"           => ToDoFilter.All
      case "#/active"    => ToDoFilter.Active
      case "#/completed" => ToDoFilter.Completed
      case "#!/"         => ToDoFilter.Completed
      case _             => ToDoFilter.All

final case class TodoItem(
    id: Int,
    label: String,
    completed: Boolean,
    editing: Boolean
):
  def toggle: TodoItem =
    this.copy(completed = !completed)

  def markAsComplete: TodoItem =
    this.copy(completed = true)

  def markAsNotComplete: TodoItem =
    this.copy(completed = false)

  def startEditing: TodoItem =
    this.copy(editing = true)

  def stopEditing: TodoItem =
    this.copy(editing = false)
