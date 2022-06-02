package tyriantodo

import cats.effect.IO
import org.scalajs.dom
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object TyrianTodo extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.initial, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NewEditingValue(newValue) =>
      (model.copy(editingValue = newValue), Cmd.None)

    case Msg.SubmitNewTodo if model.editingValue.isEmpty =>
      (model, Cmd.None)

    case Msg.SubmitNewTodo =>
      val updatedModel =
        model.copy(
          editingValue = "",
          todos = model.todos :+ TodoItem(model.editingValue.trim, false)
        )

      (updatedModel, Cmd.None)

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
    Sub.fromEvent[IO, dom.KeyboardEvent, Msg.SubmitNewTodo.type](
      "keyup",
      dom.window
    ) { event =>
      if event.keyCode == 13 then Some(Msg.SubmitNewTodo) else None
    }

final case class Model(editingValue: String, todos: List[TodoItem])
object Model:
  val initial: Model =
    Model("", Nil)

final case class TodoItem(label: String, completed: Boolean)

enum Msg:
  case NewEditingValue(value: String)
  case SubmitNewTodo
