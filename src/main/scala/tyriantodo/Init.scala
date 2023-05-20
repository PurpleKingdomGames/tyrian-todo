package tyriantodo

import cats.effect.IO
import tyrian.*
import tyrian.cmds.LocalStorage

object Init:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    import LocalStorage.Result

    // Might be nicer to use json here.
    val persist = flags
      .get("persistData")
      .map {
        case "true"  => true
        case "false" => false
      }
      .getOrElse(true)

    val config: Config = Config.initial(persist)

    val toMessage: Either[Result.NotFound, Result.Found] => Msg =
      case Left(_)      => Msg.Log("No save data found")
      case Right(found) => Msg.Load(found.data)

    val loadCmd: Cmd.Batch[IO, Msg] =
      if persist then
        Cmd.Batch(
          LocalStorage.getItem[IO, Msg](config.localStorageKey, toMessage)
        )
      else Cmd.Batch()

    (Model.initial(config), loadCmd)
