package arckane.medivh

import scala.concurrent._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

import org.mindrot.jbcrypt.BCrypt

import arckane.users.user._
import arckane.actors._

class MedivhApi extends Controller {

  def postHash = Action(parse.json) { request =>
    (request.body.as[JsObject] \ "text").asOpt[String].map { text =>
      Ok(Json.obj(
        "hash" -> BCrypt.hashpw(text, BCrypt.gensalt())
      ))
    }.getOrElse {
      BadRequest(Json.obj(
        "error" -> "bad json object"
      ))
    }
  }

  def signin = Action.async(parse.json) { request =>
    val json = request.body.as[JsObject]
    (for {
      email <- (json \ "email").asOpt[String]
      password <- (json \ "password").asOpt[String]
    } yield authenticate(email, password).map {
      case Some((uri, name)) =>
        Ok(Json.obj(
          "success" -> true
        )).withSession(
          "user-uri" -> uri,
          "user-name" -> name
        )
      case None =>
        Ok(Json.obj(
          "success" -> false
        ))
    }).getOrElse {
      Future(BadRequest(Json.obj(
        "error" -> "bad json object"
      )))
    }
  }

  def postImportPages = Action { request =>
    Actors.james ! ImportPages
    Ok
  }
}
