package tyriantodo

import cats.effect.IO
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.window
import tyrian.*

object Subscriptions:

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.Batch(
      Sub.fromEvent[IO, KeyboardEvent, Msg]("keyup", window) { e =>
        e.keyCode match
          case 13 =>
            // Enter key
            Some(Msg.SubmitTodo)

          case 27 =>
            // Escape key
            Some(Msg.StopEditingAll)

          case _ =>
            None
      },
      Navigation.onLocationHashChange(hashChange =>
        Msg.ChangeFilter(ToDoFilter.fromString(hashChange.newFragment))
      )
    )
