package com.example.library.backend.registration.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.example.library.backend.registration.model.ActivationNotificationStatus;
import com.example.library.backend.registration.model.LibraryAccountStatus;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class RegistrationArchiveSchemaInitializer implements ApplicationRunner {

    private static final String TABLE_NAME = "library_registrations";
    private static final String STORAGE_LOCATION = "Bo phan Luu hanh";
    private static final DateTimeFormatter STORAGE_CODE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_ACCOUNT_STATUS = LibraryAccountStatus.PENDING_ACTIVATION.name();
    private static final String DEFAULT_NOTIFICATION_STATUS = ActivationNotificationStatus.NOT_PREPARED.name();
    private static final List<ColumnDefinition> ARCHIVE_COLUMNS = List.of(
            new ColumnDefinition("storage_code", "VARCHAR(40)"),
            new ColumnDefinition("storage_location", "VARCHAR(120)"),
            new ColumnDefinition("library_username", "VARCHAR(255)"),
            new ColumnDefinition("library_password_hash", "VARCHAR(255)"),
            new ColumnDefinition("notification_password_plaintext", "VARCHAR(120)"),
            new ColumnDefinition("account_status", "VARCHAR(30)"),
            new ColumnDefinition("activation_notification_status", "VARCHAR(30)"),
            new ColumnDefinition("account_activated_at", "TIMESTAMP(6)"),
            new ColumnDefinition("account_expires_at", "DATE"),
            new ColumnDefinition("activation_notification_prepared_at", "TIMESTAMP(6)"),
            new ColumnDefinition("activation_notification_sent_at", "TIMESTAMP(6)"));

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public RegistrationArchiveSchemaInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        for (ColumnDefinition column : ARCHIVE_COLUMNS) {
            addColumnIfMissing(column);
        }
        ensureAvatarDataCapacity();

        List<LegacyRegistrationRow> legacyRows = jdbcTemplate.query(
                """
                select id, created_at
                from library_registrations
                where storage_code is null or trim(storage_code) = ''
                """,
                (rs, rowNum) -> new LegacyRegistrationRow(
                        rs.getLong("id"),
                        toLocalDateTime(rs.getTimestamp("created_at"))));

        for (LegacyRegistrationRow legacyRow : legacyRows) {
            jdbcTemplate.update(
                    "update library_registrations set storage_code = ? where id = ?",
                    generateStorageCode(legacyRow.id(), legacyRow.createdAt()),
                    legacyRow.id());
        }

        jdbcTemplate.update(
                """
                update library_registrations
                set storage_location = ?
                where storage_location is null or trim(storage_location) = ''
                """,
                STORAGE_LOCATION);

        jdbcTemplate.update(
                """
                update library_registrations
                set account_status = ?
                where account_status is null or trim(account_status) = ''
                """,
                DEFAULT_ACCOUNT_STATUS);

        jdbcTemplate.update(
                """
                update library_registrations
                set activation_notification_status = ?
                where activation_notification_status is null or trim(activation_notification_status) = ''
                """,
                DEFAULT_NOTIFICATION_STATUS);

        jdbcTemplate.execute(
                createIndexIfMissingSql("idx_library_registrations_storage_code", "storage_code"));
        jdbcTemplate.execute(
                createIndexIfMissingSql("idx_library_registrations_library_username", "library_username"));

        setNotNull("storage_code", "VARCHAR(40)");
        setNotNull("storage_location", "VARCHAR(120)");
        setNotNull("account_status", "VARCHAR(30)");
        setNotNull("activation_notification_status", "VARCHAR(30)");
    }

    private void addColumnIfMissing(ColumnDefinition column) throws SQLException {
        if (!hasColumn(column.name())) {
            jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + column.name() + " " + column.type());
        }
    }

    private String createIndexIfMissingSql(String indexName, String columnName) throws SQLException {
        if (hasIndex(indexName)) {
            return "SELECT 1";
        }
        return "CREATE UNIQUE INDEX " + indexName + " ON " + TABLE_NAME + "(" + columnName + ")";
    }

    private void setNotNull(String columnName, String columnType) throws SQLException {
        if (!isColumnNullable(columnName)) {
            return;
        }
        if (isMySql()) {
            jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " MODIFY COLUMN " + columnName + " " + columnType
                    + " NOT NULL");
        } else {
            jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " ALTER COLUMN " + columnName + " SET NOT NULL");
        }
    }

    private void ensureAvatarDataCapacity() throws SQLException {
        if (!isMySql()) {
            return;
        }

        String typeName = getColumnTypeName("avatar_data");
        if (typeName == null) {
            return;
        }

        String normalizedTypeName = typeName.toLowerCase(Locale.ROOT);
        if (!normalizedTypeName.contains("mediumblob") && !normalizedTypeName.contains("longblob")) {
            jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " MODIFY COLUMN avatar_data MEDIUMBLOB NOT NULL");
        }
    }

    private boolean hasColumn(String columnName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, null, null)) {
                while (columns.next()) {
                    if (TABLE_NAME.equalsIgnoreCase(columns.getString("TABLE_NAME"))
                            && columnName.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getColumnTypeName(String columnName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, null, null)) {
                while (columns.next()) {
                    if (TABLE_NAME.equalsIgnoreCase(columns.getString("TABLE_NAME"))
                            && columnName.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                        return columns.getString("TYPE_NAME");
                    }
                }
            }
        }
        return null;
    }

    private boolean isColumnNullable(String columnName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, null, null)) {
                while (columns.next()) {
                    if (TABLE_NAME.equalsIgnoreCase(columns.getString("TABLE_NAME"))
                            && columnName.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                        return DatabaseMetaData.columnNullable == columns.getInt("NULLABLE");
                    }
                }
            }
        }
        return false;
    }

    private boolean hasIndex(String indexName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet indexes = metadata.getIndexInfo(connection.getCatalog(), null, TABLE_NAME, false, false)) {
                while (indexes.next()) {
                    String existingIndexName = indexes.getString("INDEX_NAME");
                    if (existingIndexName != null && indexName.equalsIgnoreCase(existingIndexName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isMySql() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("mysql");
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp != null) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.now();
    }

    private String generateStorageCode(Long id, LocalDateTime createdAt) {
        return "HS-" + createdAt.format(STORAGE_CODE_TIME_FORMAT) + "-L" + String.format("%04d", id);
    }

    private record LegacyRegistrationRow(Long id, LocalDateTime createdAt) {
    }

    private record ColumnDefinition(String name, String type) {
    }
}
