package db.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class V1__AddPersonaVideoUrlsTest {

  @Test
  void addsOnlyMissingColumnsAndCanRunAgain() throws Exception {
    try (Connection connection = DriverManager.getConnection(
        "jdbc:h2:mem:persona_migration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        Statement statement = connection.createStatement()) {
      statement.execute("DROP TABLE IF EXISTS ai_personas");
      statement.execute("CREATE TABLE ai_personas (id BIGINT PRIMARY KEY)");
      Context context = Mockito.mock(Context.class);
      when(context.getConnection()).thenReturn(connection);

      V1__AddPersonaVideoUrls migration = new V1__AddPersonaVideoUrls();
      migration.migrate(context);
      migration.migrate(context);

      try (ResultSet columns = connection.getMetaData()
          .getColumns(connection.getCatalog(), null, "AI_PERSONAS", null)) {
        int videoColumnCount = 0;
        while (columns.next()) {
          String columnName = columns.getString("COLUMN_NAME");
          if ("SPEAKING_VIDEO_URL".equalsIgnoreCase(columnName)
              || "SILENCE_VIDEO_URL".equalsIgnoreCase(columnName)) {
            videoColumnCount++;
          }
        }
        assertThat(videoColumnCount).isEqualTo(2);
      }
    }
  }
}
