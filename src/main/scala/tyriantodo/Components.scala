package tyriantodo

import tyrian.Html.*
import tyrian.*

object Components:

  def todoAppSection(contents: List[Elem[Msg]]): Html[Msg] =
    section(_class := "todoapp")(contents)

  def todoAppHeader(editingValue: String): Html[Msg] =
    header(_class := "header")(
      h1("todos"),
      input(
        _class      := "new-todo",
        placeholder := "What needs to be done?",
        value := editingValue,
        autoFocus,
        onInput(s => Msg.NewEditingValue(s))
      )
    )

  // This section should be hidden by default and shown when there are todos
  def todoMainSection(model: Model): Html[Msg] =
    section(_class := "main")(
      input(id := "toggle-all", _class := "toggle-all", _type := "checkbox"),
      label(forId := "toggle-all")("Mark all as complete"),
      ul(_class := "todo-list")(
        model.todos.map { todo =>
          li(
            div(_class := "view")(
              input(_class := "toggle", _type := "checkbox", checked(todo.completed)),
              label(todo.label),
              button(_class := "destroy")()
            ),
            input(_class := "edit", value := "Rule the web")
          )
        }
      )
    )

  // This footer should be hidden by default and shown when there are todos
  def todoAppFooter(count: Int): Html[Msg] =
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
      button(_class := "clear-completed")("Clear completed")
    )

  def todoPageFooter: Html[Msg] =
    footer(_class := "info")(
      p("Double-click to edit a todo"),
      p(
        text("Created by "),
        a(href := "https://twitter.com/davidjamessmith")("Dave")
      ),
      p(text("Part of "), a(href := "https://todomvc.com")("TodoMVC"))
    )
