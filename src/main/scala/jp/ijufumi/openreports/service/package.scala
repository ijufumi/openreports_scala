package jp.ijufumi.openreports

package object service {
  val TemplatePath = sys.env.getOrElse("TEMPLATE_PATH", "report")
  val OutputFilePath = sys.env.getOrElse("OUTPUT_FILE_PATH", "report/output")
  val Hashedkey = "test"
}
