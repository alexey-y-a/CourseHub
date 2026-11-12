package ru.alexey.coursehub.integration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SchemaIT {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void mapsAllSeventeenEntities() {
        assertThat(entityManagerFactory.getMetamodel().getEntities()).hasSize(17);
    }

    @Test
    void createsTablesForAllEntitiesAndJoinTable() {
        Integer tables = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables "
                        + "where table_schema in ('public', 'PUBLIC') and table_type = 'BASE TABLE'",
                Integer.class);

        assertThat(tables).isEqualTo(18);
    }
}
