package tyriantodo

import cats.effect.IO
import tyrian.*
import tyrian.cmds.Dom
import tyrian.cmds.LocalStorage
import tyrian.cmds.Logger

object Update:

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Log(msg) =>
      (model, Logger.info(msg))

    case Msg.Load(data) =>
      (Model.fromSaveData(data), Cmd.None)

    case Msg.Save =>
      val cmd: Cmd[IO, Msg] =
        if model.config.persistData then
          LocalStorage.setItem(
            model.config.localStorageKey,
            model.serialise,
            _ => Msg.Log(s"Saved ${model.todos.length} todos")
          )
        else Cmd.None

      (model, cmd)

    case Msg.NoOp =>
      (model, Cmd.None)

    case Msg.ChangeFilter(filter) =>
      (model.withFilter(filter), Cmd.None)

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
