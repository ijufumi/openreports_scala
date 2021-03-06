package jp.ijufumi.openreports.controller.settings

import jp.ijufumi.openreports.controller.common.{ApplicationController, FileUploadController, I18nFeature}
import jp.ijufumi.openreports.service.enums.StatusCode
import jp.ijufumi.openreports.service.settings.ReportTemplateSettingsService

class ReportTemplateSettingsController extends FileUploadController {

  override val activeMenu = "settings/report-template"
  override val requiredMemberInfo = true

  val path = RootPath + "/report-template"
  val viewPath = RootPath + "/report-template"

  def index = {
    val templates = new ReportTemplateSettingsService().getReportTemplates
    set("templates", templates)
    render(viewPath + "/index")
  }

  def showUpload = {
    render(viewPath + "/upload")
  }

  def upload = {
    fileParams.get("uploadFile") match {
      case Some(file) =>
        val statusCode = new ReportTemplateSettingsService().uploadFile(file)
        statusCode match {
          case StatusCode.OK => redirect(url(Controllers.reportTemplateSettings.indexUrl))
        }
        set("customErrorMessages", Seq(i18n.get("error.fileUploadError")))
      case None =>
        logger.info("file not found")
        set("customErrorMessages", Seq(i18n.get("warning.requiredUploadFile")))
    }

    redirect(url(Controllers.reportTemplateSettings.showUploadUrl))
  }

  def showHistory = {
    params.getAs[Long]("id").map { id =>
      val histories = new ReportTemplateSettingsService().getHistories(id)
      set("histories", histories)
      render(viewPath + "/history")
    } getOrElse haltWithBody(404)
  }
}
