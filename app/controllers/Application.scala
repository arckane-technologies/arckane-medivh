package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

class Application extends Controller {

  def getSession (request: Request[AnyContent]): Option[String] = for {
    name <- request.session.get("user-name")
    uri <- request.session.get("user-uri")
  } yield Json.obj(
    "user-name" -> name,
    "user-uri" -> uri
  ).toString

  def guest: String = Json.obj("guest" -> true).toString

  def isGuest (request: Request[AnyContent]): Boolean = request.session.get("user-name") match {
    case Some(name) => false
    case None => true
  }

  def index = Action { request =>
    getSession(request) match {
      case Some(session) => Ok(views.html.index(session))
      case None => Redirect("/signin")
    }
  }

  def signin = Action { request =>
    if (isGuest(request)) Ok(views.html.signin())
    else Redirect("/")
  }

  def signout = Action {
    Redirect("/").withNewSession
  }
}
