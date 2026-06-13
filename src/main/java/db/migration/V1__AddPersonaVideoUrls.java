package db.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V1__AddPersonaVideoUrls extends BaseJavaMigration {

  private static final String TABLE_NAME = "ai_personas";

  @Override
  public void migrate(Context context) throws Exception {
    Connection connection = context.getConnection();
    addColumnIfMissing(connection, "speaking_video_url");
    addColumnIfMissing(connection, "silence_video_url");
  }

  private void addColumnIfMissing(Connection connection, String columnName) throws Exception {
    if (columnExists(connection, columnName)) {
      return;
    }

    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(
          "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " VARCHAR(1024) NULL");
    }
  }

  private boolean columnExists(Connection connection, String columnName) throws Exception {
    DatabaseMetaData metadata = connection.getMetaData();
    String catalog = connection.getCatalog();
    String metadataTableName = metadata.storesUpperCaseIdentifiers()
        ? TABLE_NAME.toUpperCase(Locale.ROOT)
        : TABLE_NAME;
    try (ResultSet columns = metadata.getColumns(catalog, null, metadataTableName, null)) {
      while (columns.next()) {
        String existingColumn = columns.getString("COLUMN_NAME");
        if (columnName.equals(existingColumn.toLowerCase(Locale.ROOT))) {
          return true;
        }
      }
    }
    return false;
  }
}
