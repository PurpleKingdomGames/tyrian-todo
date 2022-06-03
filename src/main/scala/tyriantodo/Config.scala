package tyriantodo

final case class Config(localStorageKey: String, persistData: Boolean)

object Config:

  def initial(persistData: Boolean): Config =
    Config("tyrian-todos", persistData)
