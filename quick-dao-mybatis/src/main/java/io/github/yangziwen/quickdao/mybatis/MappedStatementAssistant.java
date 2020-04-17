package io.github.yangziwen.quickdao.mybatis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

public class MappedStatementAssistant {

    private static final String DEFAULT_RESULT_MAP_ID = "defaultResultMap";

    private Configuration configuration;

    private LanguageDriver languageDriver;

    public MappedStatementAssistant(Configuration configuration) {
        this.configuration = configuration;
        this.languageDriver = configuration.getDefaultScriptingLanguageInstance();
    }

    private String newMappedStatementId(String sql, SqlCommandType sqlCommandType) {
        StringBuilder idBuilder = new StringBuilder(sqlCommandType.toString());
        idBuilder.append(".").append(sql.hashCode());
        return idBuilder.toString();
    }

    private boolean hasMappedStatement(String id) {
        return configuration.hasStatement(id, false);
    }

    private void newSelectMappedStatement(String id, SqlSource sqlSource, final Class<?> resultType) {
        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(new ResultMap.Builder(configuration, DEFAULT_RESULT_MAP_ID, resultType, new ArrayList<ResultMapping>(0)).build());
        MappedStatement mappedStatement = new MappedStatement.Builder(configuration, id, sqlSource, SqlCommandType.SELECT)
                .resultMaps(resultMaps)
                .build();
        configuration.addMappedStatement(mappedStatement);
    }

    private void newInsertMappedStatement(String id, SqlSource sqlSource, String keyProperty) {
        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(new ResultMap.Builder(configuration, DEFAULT_RESULT_MAP_ID, int.class, new ArrayList<ResultMapping>(0)).build());
        MappedStatement.Builder builder = new MappedStatement.Builder(configuration, id, sqlSource, SqlCommandType.INSERT)
                .resultMaps(resultMaps);
        if (StringUtils.isNotBlank(keyProperty)) {
            builder.keyGenerator(Jdbc3KeyGenerator.INSTANCE).keyProperty(keyProperty);
        }
        MappedStatement mappedStatement = builder.build();
        configuration.addMappedStatement(mappedStatement);
    }

    private void newUpdateMappedStatement(String id, SqlSource sqlSource, SqlCommandType sqlCommandType) {
        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(new ResultMap.Builder(configuration, DEFAULT_RESULT_MAP_ID, int.class, new ArrayList<ResultMapping>(0)).build());
        MappedStatement mappedStatement = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
                .resultMaps(resultMaps)
                .build();
        configuration.addMappedStatement(mappedStatement);
    }

    public String getDynamicSelectStmt(String sql, Class<?> parameterType, Class<?> resultType) {
        String id = newMappedStatementId(resultType + sql + parameterType, SqlCommandType.SELECT);
        if (hasMappedStatement(id)) {
            return id;
        }
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameterType);
        newSelectMappedStatement(id, sqlSource, resultType);
        return id;
    }

    public String getDynamicInsertStmt(String sql, Class<?> parameterType, String keyProperty) {
        String id = newMappedStatementId(sql + parameterType, SqlCommandType.INSERT);
        if (hasMappedStatement(id)) {
            return id;
        }
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameterType);
        newInsertMappedStatement(id, sqlSource, keyProperty);
        return id;
    }

    public String getDynamicUpdateStmt(String sql, Class<?> parameterType) {
        String id = newMappedStatementId(sql + parameterType, SqlCommandType.UPDATE);
        if (hasMappedStatement(id)) {
            return id;
        }
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameterType);
        newUpdateMappedStatement(id, sqlSource, SqlCommandType.UPDATE);
        return id;
    }

    public String getDynamicDeleteStmt(String sql, Class<?> parameterType) {
        String id = newMappedStatementId(sql + parameterType, SqlCommandType.DELETE);
        if (hasMappedStatement(id)) {
            return id;
        }
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameterType);
        newUpdateMappedStatement(id, sqlSource, SqlCommandType.DELETE);
        return id;
    }

}
