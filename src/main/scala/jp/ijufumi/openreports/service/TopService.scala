package jp.ijufumi.openreports.service

import jp.ijufumi.openreports.model.{TGroup, TMember}
import jp.ijufumi.openreports.vo.MemberInfo
import skinny.LoggerProvider

import scala.collection.mutable

class TopService extends LoggerProvider {
  def login(emailAddress: String, password: String): Option[MemberInfo] = {
    var member: MemberInfo = null
    val members: Seq[TMember] = TMember.where('emailAddress -> emailAddress, 'password -> password).apply();

    if (members.isEmpty) {
      logger.info("invalid id or password : [%s][%s]".format(emailAddress, password))
    } else {
      var menus = mutable.Set[Long]()
      val m = members.head
      for (g <- m.groups) {
        val group = TGroup.findById(g.groupId)
        menus ++ group.get.functions.map(_.functionId).toSet
      }
      member = MemberInfo(m.memberId, m.name, menus.toSet)
      logger.info("memberInfo:%s".format(member))
    }
    Option(member)
  }
}