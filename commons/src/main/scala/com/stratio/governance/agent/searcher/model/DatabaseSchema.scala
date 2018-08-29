package com.stratio.governance.agent.searcher.model

import java.sql.ResultSet

import com.stratio.governance.agent.searcher.model.es.{DatabaseSchemaES, EntityRowES}
import scalikejdbc.DB
import scalikejdbc._
import scalikejdbc.streams._

import scala.concurrent.ExecutionContext

case class DatabaseSchema(id: Int,
                          datastore_engine_id: Int,
                          agent_version: String,
                          server_version: String,
                          created_at: String,
                          updated_at: String,
                          name: String,
                          display_name: Option[String],
                          description: Option[String],
                          metadata_path: String,
                          modification_time: Option[Long],
                          access_time: Option[Long],
                          operation_command_type: String,
                          location_uri: Option[String])
    extends EntityRow

object DatabaseSchema {

  val entity = "database"

  def entityFromResultSet(databaseSchema: DatabaseSchema, resultSet: ResultSet): scala.Seq[EntityRowES] = {

    val databaseSchemaList: List[(DatabaseSchema, KeyValuePair)] = getResult(resultSet, databaseSchema)

    Seq(DatabaseSchemaES.fromDatabaseSchemaList(databaseSchemaList))
  }

  @scala.annotation.tailrec
  def getResult(resultSet: ResultSet,
                databaseSchema: DatabaseSchema,
                list: List[(DatabaseSchema, KeyValuePair)] = Nil): List[(DatabaseSchema, KeyValuePair)] = {
    if (resultSet.next()) {
      val keyValuePair = KeyValuePair.apply(
        resultSet.getInt(1),
        resultSet.getInt(2),
        resultSet.getInt(3),
        resultSet.getInt(4),
        resultSet.getString(5),
        resultSet.getString(6),
        resultSet.getString(7),
        resultSet.getString(8),
        resultSet.getString(9),
        resultSet.getString(10),
        resultSet.getString(11),
        resultSet.getString(12),
        Some(resultSet.getLong(13)),
        Some(resultSet.getLong(14))
      )

      getResult(resultSet, databaseSchema, (databaseSchema, keyValuePair) :: list)
    } else {
      (databaseSchema, null) :: list
    }
  }

  def getOneResult(resultSet: WrappedResultSet): DatabaseSchema = {
    DatabaseSchema.apply(
      resultSet.int(1),
      resultSet.int(2),
      resultSet.string(3),
      resultSet.string(4),
      resultSet.string(5),
      resultSet.string(6),
      resultSet.string(7),
      resultSet.stringOpt(8),
      resultSet.stringOpt(9),
      resultSet.string(10),
      resultSet.longOpt(11),
      resultSet.longOpt(12),
      resultSet.string(13),
      resultSet.stringOpt(14)
    )
  }

  def publisher(implicit executionContext: ExecutionContext): DatabasePublisher[DatabaseSchema] = DB.readOnlyStream {
    sql"SELECT * FROM dg_metadata.database_schema"
      .map(r => DatabaseSchema.getOneResult(r))
      .iterator
      .withDBSessionForceAdjuster(session => {
        session.connection.setAutoCommit(false)
        session.fetchSize(10)
      })
  }

}
