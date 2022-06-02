package tyriantodo

import cats.effect.IO
import tyrian.*
import tyrian.cmds.LocalStorage

object Init:

  val init: (Model, Cmd[IO, Msg]) =
    import LocalStorage.Result

    val toMessage: Either[Result.NotFound, Result.Found] => Msg =
      case Left(_)      => Msg.Log("No save data found")
      case Right(found) => Msg.Load(found.data)

    val cmds: Cmd[IO, Msg] =
      Cmd.Batch(
        Navigation.getLocationHash {
          case Navigation.Result.CurrentHash(hash) =>
            Msg.ChangeFilter(ToDoFilter.fromString(hash))

          case _ =>
            Msg.NoOp
        },
        LocalStorage.getItem(Config.LocalStorageKey, toMessage)
      )

    (Model.initial, cmds)
