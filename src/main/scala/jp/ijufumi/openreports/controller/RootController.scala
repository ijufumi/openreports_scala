package jp.ijufumi.openreports.controller

import jp.ijufumi.openreports.controller.common.ApplicationController
import jp.ijufumi.openreports.model.TMember
import skinny._
import skinny.validator.{ required, _ }

class RootController extends ApplicationController {

  val path = publicPath
  val viewPath = publicPath + "/root"

  def requestParams = Params(params)

  def validateParams = validation(
    requestParams,
    paramKey("userName") is required,
    paramKey("password") is required
  )

  def toTop = redirect(publicPath)

  def index = {
    //render("/root/index")
    val memberInfo: Option[Any] = skinnySession.getAttribute("memberInfo")
    if (memberInfo.isDefined) {
      redirect(privatePath + "/home")
    } else {
      render(viewPath + "/index")
    }
  }

  def signUp = {
    render(viewPath + "/sign-up")
  }

  def login = {
    val userName = requestParams.getAs("userName").getOrElse("")
    val password = requestParams.getAs("password").getOrElse("")
    if (validateParams.validate) {
      val members: Seq[TMember] = TMember.where('emailAddress -> userName, 'password -> password).apply();

      if (members.isEmpty) {
        logger.info("invalid id or password : [" + userName + "][" + password + "]")
        set("userName", requestParams.getAs("userName").getOrElse(""))
        set("customErrorMessages", Seq(i18n.get("warning.loginFailure")))
        render(viewPath + "/index")
      } else {
        logger.info("groups:%s".format(members(0).groups))
        skinnySession.setAttribute("memberInfo", userName);
        redirect(privatePath + "/home")
      }
    } else {
      logger.info("invalid id or password : [" + userName + "][" + password + "]")
      set("customErrorMessages", Seq(i18n.get("warning.loginFailure")))
      render(viewPath + "/index")
    }
  }
}
