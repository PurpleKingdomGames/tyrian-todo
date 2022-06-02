package tyriantodo

import tyrian.Html.*
import tyrian.*

object View:

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

object Components:

  def todoAppSection(contents: List[Elem[Msg]]): Html[Msg] =
    section(_class := "todoapp")(contents)

  def todoAppHeader(editingValue: String): Html[Msg] =
    header(_class := "header")(
      h1("todos"),
      input(
        _class      := "new-todo",
        placeholder := "What needs to be done?",
        value       := editingValue,
        autoFocus,
        onInput(s => Msg.NewEditingValue(s))
      )
    )

  // This section should be hidden by default and shown when there are todos
  def todoMainSection(model: Model): Html[Msg] =
    section(_class := "main")(
      input(
        id     := "toggle-all",
        _class := "toggle-all",
        _type  := "checkbox",
        checked(model.allComplete),
        onChange(Msg.MarkAll(model.allComplete))
      ),
      label(forId := "toggle-all")("Mark all as complete"),
      ul(_class := "todo-list")(
        model.todos
          .filter { todo =>
            model.filter match
              case ToDoFilter.All       => true
              case ToDoFilter.Active    => !todo.completed
              case ToDoFilter.Completed => todo.completed
          }
          .map { todo =>
            // List items should get the class `editing` when editing and `completed` when marked as completed
            val listClass =
              if todo.editing then List(_class := "editing")
              else if todo.completed then List(_class := "completed")
              else Nil

            val itemId =
              "item-" + todo.id

            val editingValue =
              if model.editingItemValue.isEmpty then todo.label
              else model.editingItemValue

            li(listClass)(
              div(_class := "view")(
                input(
                  _class := "toggle",
                  _type  := "checkbox",
                  checked(todo.completed),
                  onChange(Msg.ToggleCompleted(todo.id))
                ),
                label(
                  onDoubleClick(Msg.EditItem(todo.id, itemId))
                )(todo.label),
                button(_class := "destroy", onClick(Msg.RemoveItem(todo.id)))()
              ),
              input(
                id          := itemId,
                _class      := "edit",
                placeholder := "Rule the web",
                value       := editingValue,
                onInput(s => Msg.EditingItemValue(s)),
                onBlur(Msg.SubmitTodo)
              )
            )
          }
      )
    )

  // This footer should be hidden by default and shown when there are todos
  def todoAppFooter(count: Int, anyComplete: Boolean): Html[Msg] =
    val remainingText =
      if count == 1 then " item left" else " items left"

    footer(_class := "footer")(
      // This should be `0 items left` by default
      span(_class := "todo-count")(
        strong(count.toString),
        text(remainingText)
      ),
      // Remove this if you don't implement routing
      ul(_class := "filters")(
        li(a(_class := "selected", href := "#/")("All")),
        li(a(href := "#/active")("Active")),
        li(a(href := "#/completed")("Completed"))
      ),
      // Hidden if no completed items are left
      button(
        _class := "clear-completed",
        hidden(!anyComplete),
        onClick(Msg.ClearCompleted)
      )("Clear completed")
    )

  val todoPageFooter: Html[Msg] =
    footer(_class := "info")(
      p("Double-click to edit a todo"),
      p(
        text("Created by "),
        a(href := "https://twitter.com/davidjamessmith")("Dave")
      ),
      p(text("Part of "), a(href := "https://todomvc.com")("TodoMVC"))
    )
