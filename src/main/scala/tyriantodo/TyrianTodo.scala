package tyriantodo

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object TyrianTodo extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.initial, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Increment => (model, Cmd.None)
    case Msg.Decrement => (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    import Components.*

    val appContents: List[Elem[Msg]] =
      if model.todos.isEmpty then Nil
      else
        List(
          todoMainSection(model),
          todoAppFooter(0)
        )

    div(
      todoAppSection(
        todoAppHeader :: appContents
      ),
      todoPageFooter
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

final case class Model(todos: List[TodoItem])
object Model:
  val initial: Model =
    Model(Nil)

final case class TodoItem(label: String, completed: Boolean)

enum Msg:
  case Increment, Decrement
