package io.github.yangziwen.quickdao.example.repository;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.lang3.math.NumberUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.util.fileloader.DataFileLoader;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public abstract class BaseRepositoryTest {

    protected static final DataSource dataSource = createDataSource();

    protected DataFileLoader loader = new FlatXmlDataFileLoader();

    protected static DataSource createDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .build();
    }

    protected void createTable(String tableName, Connection connection) {
        Resource resource = new ClassPathResource("schema/" + tableName + ".sql");
        ScriptUtils.executeSqlScript(connection, resource);
    }

    protected void truncateTable(String tableName, Connection connection) throws Exception {
        connection.prepareStatement("truncate table " + tableName).execute();
    }

    protected IDataSet loadDataSet(String tableName) {
        return loader.load("/dataset/" + tableName + ".xml");
    }

    protected ITable loadTable(String tableName, Connection connection) throws Exception {
        return loadDataSet(tableName).getTable(tableName);
    }

    protected void loadData(String tableName, Connection connection) throws Exception {
        loadData(loadDataSet(tableName), connection);
    }

    protected void loadData(IDataSet dataSet, Connection connection) throws Exception {
        DatabaseOperation.CLEAN_INSERT.execute(new DatabaseConnection(connection), dataSet);
    }

    protected String getStringValue(ITable table, int row, String column) throws Exception {
        return String.valueOf(table.getValue(row, column));
    }

    protected Long getLongValue(ITable table, int row, String column) throws Exception {
        return NumberUtils.createLong(getStringValue(table, row, column));
    }

    protected Double getDoubleValue(ITable table, int row, String column) throws Exception {
        return NumberUtils.createDouble(getStringValue(table, row, column));
    }

}
