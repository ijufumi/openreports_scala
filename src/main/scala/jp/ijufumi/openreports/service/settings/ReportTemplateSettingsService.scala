package jp.ijufumi.openreports.service.settings

import java.nio.file.FileSystems

import jp.ijufumi.openreports.model.{TReportTemplate, TReportTemplateHistory}
import jp.ijufumi.openreports.service.support.ConnectionFactory
import jp.ijufumi.openreports.vo.{ReportTemplateHistoryInfo, ReportTemplateInfo}
import jp.ijufumi.openreports.service.{PrefixClassPath, TemplatePath}
import org.joda.time.DateTime
import scalikejdbc.{DB, SQLExecution}
import skinny.Logging
import skinny.micro.multipart.FileItem

class ReportTemplateSettingsService extends Logging {
  def getReportTemplates: Seq[ReportTemplateInfo] = {
    TReportTemplate
      .findAll()
      .map(r => ReportTemplateInfo(r.templateId, r.fileName))
  }

  def getHistories(templateId:Long): Seq[ReportTemplateHistoryInfo] = {
    TReportTemplateHistory.where('templateId -> templateId).apply()
      .sortBy(_.historyId)
      .map(r => ReportTemplateHistoryInfo(r.historyId, r.templateId, r.fileName, r.createdAt))
  }

  def uploadFile(file: FileItem): Unit = {
    val templates = TReportTemplate
      .where('fileName -> file.name)
      .apply()

    val filePath =
      "%s_%s".format(DateTime.now().toString("yyyyMMddHHmmss"), file.name)

    val db = DB(ConnectionFactory.getConnection)
    try {
      db.begin()
      if (templates.isEmpty) {
        TReportTemplate.createWithAttributes('fileName -> file.name,
          'filePath -> filePath)
      } else {
        val template = templates.head

        TReportTemplate
          .updateByIdAndVersion(template.templateId, template.versions)
          .withAttributes(
            'fileName -> file.name,
            'filePath -> filePath,
            'updatedAt -> DateTime.now(),
            'versions -> (template.versions + 1)
          )

        TReportTemplateHistory
          .createWithAttributes(
            'templateId -> template.templateId,
            'fileName -> template.fileName,
            'filePath -> template.filePath,
            'createdAt -> template.createdAt,
            'updatedAt -> template.updatedAt,
            'versions -> template.versions
          )
      }

    } catch {
      case e:Throwable => {
        db.rollback()
        throw e
      }
    }

    val fullPath = FileSystems.getDefault.getPath(TemplatePath, filePath)
    var fullPathString = fullPath.toString
    if (fullPathString.startsWith(PrefixClassPath)) {
      fullPathString = getClass.getClassLoader.getResource(
          fullPathString.substring(PrefixClassPath.length)
        ).getPath
    }
    // TODO: Add error handling.
    file.write(fullPathString)
  }
}
