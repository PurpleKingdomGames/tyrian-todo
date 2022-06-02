package tyriantodo

import cats.effect.IO
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.scalajs.dom
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Dom
import tyrian.cmds.LocalStorage
import tyrian.cmds.Logger

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object TyrianTodo extends TyrianApp[Msg, Model]:

  val localStorageKey: String = "tyrian-todos"

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    val toMessage: Either[
      LocalStorage.Result.NotFound,
      LocalStorage.Result.Found
    ] => Msg =
      case Left(_)      => Msg.Log("No save data found")
      case Right(found) => Msg.Load(found.data)

    val cmds: Cmd[IO, Msg] =
      Cmd.Batch(
        Navigation.getLocationHash {
          case Navigation.Result.CurrentHash(hash) =>
            Msg.ChangeFilter(ModelFilter.fromString(hash))
          case _ => Msg.NoOp
        },
        LocalStorage.getItem(localStorageKey, toMessage)
      )

    (Model.initial, cmds)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Log(msg) =>
      (model, Logger.info(msg))

    case Msg.Load(data) =>
      (Model.fromSaveData(data), Cmd.None)

    case Msg.Save =>
      (
        model,
        LocalStorage.setItem(
          localStorageKey,
          model.serialise,
          _ => Msg.Log(s"Saved ${model.todos.length} todos")
        )
      )

    case Msg.NoOp =>
      (model, Cmd.None)

    case Msg.ChangeFilter(filter) =>
      (model.copy(filter = filter), Cmd.None)

    case Msg.NewEditingValue(newValue) =>
      (model.copy(editingValue = newValue), Cmd.None)

    case Msg.EditingItemValue(newValue) =>
      (model.copy(editingItemValue = newValue), Cmd.None)

    case Msg.SubmitTodo if model.currentlyEditing =>
      val updated =
        model.copy(
          todos = model.todos.map { todo =>
            if todo.editing then
              if model.editingItemValue.nonEmpty then
                todo.stopEditing.copy(label = model.editingItemValue.trim)
              else todo.stopEditing
            else todo
          }
        )

      (updated, Cmd.Emit(Msg.Save))

    case Msg.SubmitTodo if model.editingValue.isEmpty =>
      (model, Cmd.None)

    case Msg.SubmitTodo =>
      val updated =
        model.copy(
          editingValue = "",
          editingItemValue = "",
          todos = model.todos :+ TodoItem(
            model.idCount,
            model.editingValue.trim,
            false,
            false
          ),
          idCount = model.idCount + 1
        )

      (updated, Cmd.Emit(Msg.Save))

    case Msg.ToggleCompleted(id) =>
      val updated =
        model.copy(
          todos =
            model.todos.map(todo => if todo.id == id then todo.toggle else todo)
        )

      (updated, Cmd.Emit(Msg.Save))

    case Msg.RemoveItem(id) =>
      val updated =
        model.copy(
          todos = model.todos.filterNot(_.id == id)
        )

      (updated, Cmd.Emit(Msg.Save))

    case Msg.EditItem(id, elementId) =>
      val updated =
        model.copy(
          todos = model.todos.map(todo =>
            if todo.id == id then todo.startEditing else todo.stopEditing
          )
        )

      (updated, Dom.focus[IO, Msg](elementId)(_ => Msg.NoOp))

    case Msg.StopEditing(id) =>
      val updated =
        model.copy(
          todos = model.todos.map(todo =>
            if todo.id == id then todo.stopEditing else todo
          ),
          editingItemValue = ""
        )

      (updated, Cmd.None)

    case Msg.StopEditingAll =>
      val cmd: Cmd[IO, Msg] =
        Cmd.Batch(
          model.todos.map(todo =>
            if todo.editing then Cmd.Emit(Msg.StopEditing(todo.id))
            else Cmd.None
          )
        )

      (model, cmd)

    case Msg.MarkAll(asComplete) =>
      val updated =
        model.copy(
          todos = model.todos.map(todo =>
            if asComplete then todo.markAsComplete else todo.markAsNotComplete
          )
        )

      (updated, Cmd.Emit(Msg.Save))

    case Msg.ClearCompleted =>
      val updated =
        model.copy(
          todos = model.todos.filterNot(_.completed)
        )

      (updated, Cmd.Emit(Msg.Save))

  def view(model: Model): Html[Msg] =
    import Components.*

    val appContents: List[Elem[Msg]] =
      if model.todos.isEmpty then Nil
      else
        List(
          todoMainSection(model),
          todoAppFooter(
            model.todos.filterNot(_.completed).length,
            model.anyComplete
          )
        )

    div(
      todoAppSection(
        todoAppHeader(model.editingValue) :: appContents
      ),
      todoPageFooter
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.Batch(
      Sub.fromEvent[IO, dom.KeyboardEvent, Msg](
        "keyup",
        dom.window
      ) { e =>
        if e.keyCode == 13 then Some(Msg.SubmitTodo) else None
      },
      Sub.fromEvent[IO, dom.KeyboardEvent, Msg](
        "keyup",
        dom.window
      ) { e =>
        if e.keyCode == 27 then Some(Msg.StopEditingAll) else None
      },
      Navigation.onLocationHashChange(hashChange =>
        Msg.ChangeFilter(ModelFilter.fromString(hashChange.newFragment))
      )
    )

final case class Model(
    editingValue: String,
    editingItemValue: String,
    todos: List[TodoItem],
    idCount: Int,
    filter: ModelFilter
):
  def currentlyEditing: Boolean =
    todos.exists(_.editing)

  def allComplete: Boolean =
    todos.forall(_.completed)

  def anyComplete: Boolean =
    todos.exists(_.completed)

  def serialise: String =
    todos.asJson.noSpaces

object Model:
  val initial: Model =
    Model("", "", Nil, 0, ModelFilter.All)

  def fromSaveData(data: String): Model =
    val l =
      decode[List[TodoItem]](data).toOption.getOrElse(Nil)
      .zipWithIndex
      .map { case (todo, i) =>
        todo.copy(id = i)
      }

    Model(
      "",
      "",
      l,
      l.length,
      ModelFilter.All
    )

enum ModelFilter:
  case All, Active, Completed

object ModelFilter:
  def fromString(hash: String): ModelFilter =
    hash match
      case "#"           => ModelFilter.All
      case "#/active"    => ModelFilter.Active
      case "#/completed" => ModelFilter.Completed
      case "#!/"         => ModelFilter.Completed
      case _             => ModelFilter.All

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
  case ChangeFilter(to: ModelFilter)
