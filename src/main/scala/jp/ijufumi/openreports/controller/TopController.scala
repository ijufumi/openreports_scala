package jp.ijufumi.openreports.controller

import jp.ijufumi.openreports.controller.common.ApplicationController
import jp.ijufumi.openreports.service.TopService
import skinny._
import skinny.validator.{required, _}

class TopController extends ApplicationController {

  override val activeMenu = "top"
  val path = PublicPath
  val viewPath = PublicPath + "/top"

  def toTop = redirect(url(Controllers.top.indexUrl))

  def index = {
    //render("/top/index")
    val memberInfo: Option[Any] = skinnySession.getAttribute("memberInfo")
    if (memberInfo.isDefined) {
      redirect(url(Controllers.home.indexUrl))
    } else {
      render(viewPath + "/index")
    }
  }

  def signUp = {
    render(viewPath + "/sign-up")
  }

  def login = {
    val loginId = requestParams.getAs("loginId").getOrElse("")
    val password = requestParams.getAs("password").getOrElse("")
    if (validateParams.validate) {
      val memberInfoOpt = new TopService().login(loginId, password)

      if (memberInfoOpt.isEmpty) {
        set("loginId", requestParams.getAs("loginId").getOrElse(""))
        set("customErrorMessages", Seq(i18n.get("warning.loginFailure")))
        render(viewPath + "/index")
      } else {
        skinnySession.setAttribute("memberInfo", memberInfoOpt.get);
        redirect(url(Controllers.home.indexUrl))
      }
    } else {
      logger.info(
        "invalid id or password : [" + loginId + "][" + password + "]"
      )
      render(viewPath + "/index")
    }
  }

  def requestParams = Params(params)

  def validateParams = validation(
    requestParams,
    paramKey("loginId") is required,
    paramKey("password") is required
  )
}
