package tyriantodo

import cats.effect.IO
import org.scalajs.dom
import tyrian.*

object Subscriptions:

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
        Msg.ChangeFilter(ToDoFilter.fromString(hashChange.newFragment))
      )
    )
