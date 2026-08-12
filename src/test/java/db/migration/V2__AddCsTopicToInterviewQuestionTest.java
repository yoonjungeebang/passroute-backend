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

class V2__AddCsTopicToInterviewQuestionTest {

  @Test
  void addsCsTopicColumnAndCanRunAgain() throws Exception {
    try (Connection connection = DriverManager.getConnection(
        "jdbc:h2:mem:cs_topic_migration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        Statement statement = connection.createStatement()) {
      statement.execute("DROP TABLE IF EXISTS interview_questions");
      statement.execute("CREATE TABLE interview_questions (id BIGINT PRIMARY KEY)");
      Context context = Mockito.mock(Context.class);
      when(context.getConnection()).thenReturn(connection);

      V2__AddCsTopicToInterviewQuestion migration = new V2__AddCsTopicToInterviewQuestion();
      migration.migrate(context);
      migration.migrate(context);

      try (ResultSet columns = connection.getMetaData()
          .getColumns(connection.getCatalog(), null, "INTERVIEW_QUESTIONS", null)) {
        int csTopicColumnCount = 0;
        while (columns.next()) {
          String columnName = columns.getString("COLUMN_NAME");
          if ("CS_TOPIC".equalsIgnoreCase(columnName)) {
            csTopicColumnCount++;
          }
        }
        assertThat(csTopicColumnCount).isEqualTo(1);
      }
    }
  }
}
