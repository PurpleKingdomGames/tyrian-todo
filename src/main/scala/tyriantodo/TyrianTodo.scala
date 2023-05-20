package tyriantodo

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object TyrianTodo extends TyrianApp[Msg, Model]:

  def router: Location => Msg =
    case Location.Internal(LocationDetails(Some(hash), _, _, _, _, _, _)) =>
      Msg.ChangeFilter(ToDoFilter.fromString(hash))

    case Location.Internal(_) =>
      Msg.NoOp

    case loc: Location.External =>
      Msg.FollowLink(loc.href)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    Init.init(flags)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    Update.update(model)

  def view(model: Model): Html[Msg] =
    View.view(model)

  def subscriptions(model: Model): Sub[IO, Msg] =
    Subscriptions.subscriptions(model)
