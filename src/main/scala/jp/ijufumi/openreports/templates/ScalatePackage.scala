package jp.ijufumi.openreports.templates

import org.fusesource.scalate.support.TemplatePackage
import org.fusesource.scalate.{ Binding, TemplateSource }

/**
 * Defines some common imports, attributes and methods across jp.ijufumi.openreports.controller.templates in package foo and below
 */
class ScalatePackage extends TemplatePackage {

  /** Returns the Scala code to add to the top of the generated template method */
  def header(source: TemplateSource, bindings: List[Binding]) =
    """
import jp.ijufumi.openreports.controller._
import jp.ijufumi.openreports.controller.model._
  """

}
