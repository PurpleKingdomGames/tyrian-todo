package tyriantodo

import cats.effect.IO
import org.scalajs.dom
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Dom

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object TyrianTodo extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.initial, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NoOp =>
      (model, Cmd.None)

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

      (updated, Cmd.None)

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

      (updated, Cmd.None)

    case Msg.ToggleCompleted(id) =>
      val updated =
        model.copy(
          todos =
            model.todos.map(todo => if todo.id == id then todo.toggle else todo)
        )

      (updated, Cmd.None)

    case Msg.RemoveItem(id) =>
      val updated =
        model.copy(
          todos = model.todos.filterNot(_.id == id)
        )

      (updated, Cmd.None)

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

  def view(model: Model): Html[Msg] =
    import Components.*

    val appContents: List[Elem[Msg]] =
      if model.todos.isEmpty then Nil
      else
        List(
          todoMainSection(model),
          todoAppFooter(model.todos.filterNot(_.completed).length)
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
      }
    )

final case class Model(
    editingValue: String,
    editingItemValue: String,
    todos: List[TodoItem],
    idCount: Int
):
  def currentlyEditing: Boolean =
    todos.exists(_.editing)

object Model:
  val initial: Model =
    Model("", "", Nil, 0)

final case class TodoItem(
    id: Int,
    label: String,
    completed: Boolean,
    editing: Boolean
):
  def toggle: TodoItem =
    this.copy(completed = !completed)

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
  case NoOp
