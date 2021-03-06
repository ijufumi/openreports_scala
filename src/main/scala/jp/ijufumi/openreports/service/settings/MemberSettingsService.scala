package jp.ijufumi.openreports.service.settings

import java.sql.SQLException

import jp.ijufumi.openreports.model.{RMemberGroup, TGroup, TMember}
import jp.ijufumi.openreports.service.HashKey
import jp.ijufumi.openreports.service.enums.StatusCode
import jp.ijufumi.openreports.service.support.{ConnectionFactory, Hash}
import jp.ijufumi.openreports.vo.{GroupInfo, MemberInfo}
import org.joda.time.DateTime
import scalikejdbc.{DB, SQLSyntax}
import skinny.Logging

import scala.collection.mutable

class MemberSettingsService extends Logging {
  def getMembers(): Array[MemberInfo] = {
    TMember.findAll().map(m => MemberInfo(m)).toArray
  }

  def getMember(memberId: Long): Option[MemberInfo] = {
    val memOpt = TMember.findById(memberId).map(m => MemberInfo(m))
    if (memOpt.isEmpty) {
      return memOpt
    }

    val mem = memOpt.get

    val newGroups = mutable.Seq[GroupInfo]()
    // 所属しているグループを追加
    newGroups ++ mem.groups
    // 所属していないグループを追加
    newGroups ++ TGroup
      .findAll()
      .find(g => !mem.groups.exists(_.groupId == g.groupId))
      .map(g => GroupInfo(g.groupId, g.groupName, g.versions))

    Option.apply(
      new MemberInfo(
        mem.memberId,
        mem.name,
        mem.emailAddress,
        newGroups,
        mem.menus,
        mem.versions
      )
    )
  }

  def registerMember(name: String,
                     emailAddress: String,
                     password: String,
                     isAdmin: Boolean,
                     groups: Seq[String]): StatusCode.Value = {

    val db = DB(ConnectionFactory.getConnection)
    try {
      db.begin()
      val id = TMember.createWithAttributes(
        'emailAddress -> emailAddress,
        'password -> Hash.hmacSha256(HashKey, password),
        'name -> name
      )
      groups.foreach(
        s => RMemberGroup.createWithAttributes('memberId -> id, 'groupId -> s)
      )
      db.commit()
    } catch {
      case e: SQLException => {
        db.rollback()
        return StatusCode.of(e)
      }
      case _: Throwable    => {
        db.rollback()
        return StatusCode.OTHER_ERROR
      }
    }
    StatusCode.OK
  }

  def updateMember(memberId: Long,
                   name: String,
                   emailAddress: String,
                   password: String,
                   isAdmin: Boolean,
                   groups: Seq[String],
                   versions: Long): StatusCode.Value = {
    val db = DB(ConnectionFactory.getConnection)
    try {
      db.begin()
      val memberOpt = TMember.findById(memberId)
      if (memberOpt.isEmpty) {
        return StatusCode.DATA_NOT_FOUND
      }

      val updateBuilder = TMember
        .updateByIdAndVersion(memberId, versions)
        .addAttributeToBeUpdated(
          (TMember.column.field("name"), name)
        )
        .addAttributeToBeUpdated(
          (TMember.column.field("emailAddress"), emailAddress)
        )

      if (password.length != 0) {
        val hashedPassword = Hash.hmacSha256(HashKey, password)
        updateBuilder.addAttributeToBeUpdated(
          (TMember.column.field("password"), hashedPassword)
        )
      }

      updateBuilder.withAttributes('updatedAt -> DateTime.now)

      RMemberGroup.deleteBy(
        SQLSyntax.eq(RMemberGroup.column.field("memberId"), memberId)
      )

      groups.foreach(
        s =>
          RMemberGroup
            .createWithAttributes('memberId -> memberId, 'groupId -> s)
      )
      db.commit()
    } catch {
      case e: SQLException => {
        db.rollback()
        return StatusCode.of(e)
      }
      case _: Throwable    => {
        db.rollback()
        return StatusCode.OTHER_ERROR
      }
    }
    StatusCode.OK
  }
}
