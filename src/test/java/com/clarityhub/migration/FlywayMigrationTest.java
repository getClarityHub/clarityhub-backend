package com.clarityhub.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.clarityhub.TestcontainersConfiguration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class FlywayMigrationTest {

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void v1MigrationApplied() {
        List<Map<String, Object>> history =
                jdbcTemplate.queryForList(
                        "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY"
                                + " installed_rank");
        assertThat(history).hasSize(1);
        assertThat(history.getFirst().get("version")).isEqualTo("1");
    }

    @Test
    void vectorExtensionEnabled() {
        List<Map<String, Object>> extensions =
                jdbcTemplate.queryForList(
                        "SELECT extname FROM pg_extension WHERE extname = 'vector'");
        assertThat(extensions).hasSize(1);
    }
}
