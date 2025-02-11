package com.github.fsanaulla.chronicler.urlhttp

import com.github.fsanaulla.chronicler.core.enums.Privileges
import com.github.fsanaulla.chronicler.core.model.{UserInfo, UserPrivilegesInfo}
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import com.github.fsanaulla.chronicler.urlhttp.management.{InfluxMng, UrlManagementClient}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, TryValues, BeforeAndAfterAll}
import com.github.fsanaulla.chronicler.testing.BaseSpec

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 10.08.17
  */
class UserManagementSpec
    extends BaseSpec
    with ScalaFutures
    with EitherValues
    with TryValues
    with DockerizedInfluxDB
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    influx.close()
    super.afterAll()
  }

  val userDB    = "db"
  val userName  = "Martin"
  val userPass  = "pass"
  val userNPass = "new_pass"

  val admin     = "Admin"
  val adminPass = "admin_pass"

  lazy val influx: UrlManagementClient =
    InfluxMng(host, port, Some(credentials))

  "User Management API" - {
    "should" - {
      "create user" in {
        influx.createDatabase(userDB).success.value.value shouldEqual 200

        influx.createUser(userName, userPass).success.value.value shouldEqual 200
        influx.showUsers.success.value.value
          .contains(UserInfo(userName, isAdmin = false)) shouldEqual true
      }

      "create admin" in {
        influx.createAdmin(admin, adminPass).success.value.value shouldEqual 200
        influx.showUsers.success.value.value
          .contains(UserInfo(admin, isAdmin = true)) shouldEqual true
      }

      "show user privileges" in {
        influx.showUserPrivileges(admin).success.value.value shouldEqual Nil
      }

      "set user password" in {
        influx.setUserPassword(userName, userNPass).success.value.value shouldEqual 200
      }

      "set privileges" in {
        influx.setPrivileges(userName, userDB, Privileges.READ).success.value.value shouldEqual 200
        influx
          .setPrivileges("unknown", userDB, Privileges.READ)
          .success
          .value
          .left
          .value
          .getMessage shouldEqual "user not found"

        influx.showUserPrivileges(userName).success.value.value shouldEqual Array(
          UserPrivilegesInfo(userDB, Privileges.READ)
        )
      }

      "revoke privileges" in {
        influx
          .revokePrivileges(userName, userDB, Privileges.READ)
          .success
          .value
          .value shouldEqual 200
        influx.showUserPrivileges(userName).success.value.value shouldEqual Array(
          UserPrivilegesInfo(userDB, Privileges.NO_PRIVILEGES)
        )
      }

      "disable admin" in {
        influx.disableAdmin(admin).success.value.value shouldEqual 200
        influx.showUsers.success.value.value
          .contains(UserInfo(admin, isAdmin = false)) shouldEqual true
      }

      "make admin" in {
        influx.makeAdmin(admin).success.value.value shouldEqual 200
        influx.showUsers.success.value.value
          .contains(UserInfo(admin, isAdmin = true)) shouldEqual true
      }

      "drop users" in {
        influx.dropUser(userName).success.value.value shouldEqual 200
        influx.dropUser(admin).success.value.value shouldEqual 200
      }
    }
  }
}
