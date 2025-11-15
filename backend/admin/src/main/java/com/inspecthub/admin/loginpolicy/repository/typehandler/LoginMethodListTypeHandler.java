package com.inspecthub.admin.loginpolicy.repository.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MyBatis TypeHandler for List<LoginMethod>
 * List<LoginMethod>를 JSON 문자열로 변환 (H2: VARCHAR, PostgreSQL: JSONB 모두 지원)
 */
@MappedTypes(List.class)
public class LoginMethodListTypeHandler extends BaseTypeHandler<List<LoginMethod>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<ArrayList<LoginMethod>> TYPE_REF = 
        new TypeReference<ArrayList<LoginMethod>>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<LoginMethod> parameter, JdbcType jdbcType) 
            throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            // H2는 VARCHAR, PostgreSQL은 JSONB - 둘 다 String으로 처리
            ps.setString(i, json);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting List<LoginMethod> to JSON", e);
        }
    }

    @Override
    public List<LoginMethod> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<LoginMethod> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<LoginMethod> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private List<LoginMethod> parseJson(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSONB to List<LoginMethod>: " + json, e);
        }
    }
}
