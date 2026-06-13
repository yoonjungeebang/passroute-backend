package db.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V2__AddCsTopicToInterviewQuestion extends BaseJavaMigration {

  private static final String TABLE_NAME = "interview_questions";
  private static final String COLUMN_NAME = "cs_topic";

  @Override
  public void migrate(Context context) throws Exception {
    Connection connection = context.getConnection();
    if (columnExists(connection)) {
      return;
    }

    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(
          "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " VARCHAR(30) NULL");
    }
  }

  private boolean columnExists(Connection connection) throws Exception {
    DatabaseMetaData metadata = connection.getMetaData();
    String catalog = connection.getCatalog();
    String metadataTableName = metadata.storesUpperCaseIdentifiers()
        ? TABLE_NAME.toUpperCase(Locale.ROOT)
        : TABLE_NAME;
    try (ResultSet columns = metadata.getColumns(catalog, null, metadataTableName, null)) {
      while (columns.next()) {
        String existingColumn = columns.getString("COLUMN_NAME");
        if (COLUMN_NAME.equals(existingColumn.toLowerCase(Locale.ROOT))) {
          return true;
        }
      }
    }
    return false;
  }
}
