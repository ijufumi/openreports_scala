package jp.ijufumi.openreports.controller.settings

import jp.ijufumi.openreports.controller.common.ApplicationController
import jp.ijufumi.openreports.service.enums.StatusCode
import jp.ijufumi.openreports.service.settings.{ReportGroupSettingsService, ReportParamSettingsService, ReportSettingsService, ReportTemplateSettingsService}
import jp.ijufumi.openreports.vo.{ApiResponse, ReportInfo, ReportParamConfig}
import skinny.controller.Params
import skinny.validator.{longValue, maxLength, paramKey, required}
import org.json4s.jackson.Serialization.read
import skinny.micro.Format

class ReportSettingsController extends ApplicationController {

  override val activeMenu = "settings/report"
  override val requiredMemberInfo = true

  val path = RootPath + "/report"
  val viewPath = RootPath + "/report"

  def requestParams = Params(params)

  def validate4Register = validation(
    requestParams,
    paramKey("reportName") is required & maxLength(250),
    paramKey("description") is maxLength(250),
    paramKey("templateId") is required
  )

  def validate4Update = validation(
    requestParams,
    paramKey("reportName") is required & maxLength(250),
    paramKey("description") is maxLength(250),
    paramKey("templateId") is required,
    paramKey("versions") is required & longValue
  )

  def validate4UpdatePrams = validation(
    requestParams,
    paramKey("params") is required,
    paramKey("versions") is required & longValue
  )

  def index = {
    val reports = new ReportSettingsService().getReports
    set("reports", reports)
    render(viewPath + "/index")
  }

  def showRegister = {
    val templates = new ReportTemplateSettingsService().getReportTemplates
    val params = new ReportParamSettingsService().getParams
    val groups = new ReportGroupSettingsService().getGroups
    set("templates", templates)
    set("params", params)
    set("groups", groups)
    render(viewPath + "/register")
  }

  def doRegister = {
    implicit val response = Format.JSON
    // TODO:add validation
    val reportInfo = getBodyAs[ReportInfo].get
    val statusCode = new ReportSettingsService().registerReport(reportInfo)

    statusCode match {
      case StatusCode.OK => renderWithFormat(ApiResponse())
      case StatusCode.DUPLICATE_ERR =>
        status = 400
        renderWithFormat(ApiResponse(status = false, message = i18n.get("warning.duplicateRegister").getOrElse("")))
      case _ =>
        status = 400
        renderWithFormat(ApiResponse(status = false, message = i18n.get("werror.systemErrorr").getOrElse("")))
    }
  }

  def registerCompleted = {
    render(viewPath + "/register-completed")
  }

  def showUpdate = {
    params.getAs[Long]("id").map { id =>
      val reportOpt = new ReportSettingsService().getReport(id)
      if (reportOpt.isEmpty) {
        haltWithBody(404)
      }
      val templates = new ReportTemplateSettingsService().getReportTemplates
      val groups = new ReportGroupSettingsService().getGroups
      set("templates", templates)
      set("report", reportOpt.get)
      set("groups", groups)
      render(viewPath + "/update")
    } getOrElse haltWithBody(404)
  }

  def doUpdate = {
    implicit val response = Format.JSON
    params.getAs[Long]("id").map { id =>
      // TODO:add validation
      val reportInfo = getBodyAs[ReportInfo].get

      val statusCode = new ReportSettingsService().updateReport(
        id,
        reportInfo
      )

      logger.info("updateReport result:%s".format(statusCode))

      statusCode match {
        case StatusCode.OK => renderWithFormat(ApiResponse())
        case StatusCode.DUPLICATE_ERR =>
          status = 400
          renderWithFormat(ApiResponse(status = false, message = i18n.get("warning.duplicateRegister").getOrElse("")))
        case _ =>
          status = 400
          renderWithFormat(ApiResponse(status = false, message = i18n.get("werror.systemErrorr").getOrElse("")))
      }
    } getOrElse haltWithBody[String](404)
  }

  def updateCompleted = {
    render(viewPath + "/update-completed")
  }

  def showUpdateParams = {
    params.getAs[Long]("id").map { id =>
      val reportOpt = new ReportSettingsService().getReport(id)
      if (reportOpt.isEmpty) {
        haltWithBody(404)
      }
      set("report", reportOpt.get)
      val reportParams = new ReportParamSettingsService().getParams
      set("reportParams", reportParams)
      val paramsConfig = new ReportSettingsService().getReportParamConfig(id)
      set("paramsConfig", paramsConfig)
      render(viewPath + "/report-param")
    } getOrElse haltWithBody(404)
  }

  def doUpdateParams = {
    params.getAs[Long]("id").map { id =>
      if (validate4UpdatePrams.validate) {
        val reportParams = params.getAs[String]("params").getOrElse("")
        val versions = params.getAs[Long]("versions").getOrElse(0L)
        val reportParamList = read[List[ReportParamConfig]](reportParams)

        val statusCode = new ReportSettingsService().updateReportParam(
          id,
          reportParamList,
          versions
        )

        logger.info("updateReport result:%s".format(statusCode))

        statusCode match {
          case StatusCode.OK => redirect(url(Controllers.reportSettings.updateCompletedUrl))
          case StatusCode.DUPLICATE_ERR =>
            set("customErrorMessages", Seq(i18n.get("warning.duplicateRegister")))
          case _ =>
            set("customErrorMessages", Seq(i18n.get("error.systemError")))
        }
      } else {
        logger.info("invalid params:%s".format(params))
      }
      val reportOpt = new ReportSettingsService().getReport(id)
      if (reportOpt.isEmpty) {
        haltWithBody(404)
      }
      set("report", reportOpt.get)
      val reportParams = new ReportParamSettingsService().getParams
      set("reportParams", reportParams)
      val paramsConfig = new ReportSettingsService().getReportParamConfig(id)
      set("paramsConfig", paramsConfig)
      render(viewPath + "/report-param")
    } getOrElse haltWithBody(404)
  }
}
