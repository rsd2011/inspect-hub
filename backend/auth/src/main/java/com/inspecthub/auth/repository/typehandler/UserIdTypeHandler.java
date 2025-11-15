package com.inspecthub.auth.repository.typehandler;

import com.inspecthub.auth.domain.UserId;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler for UserId Value Object
 *
 * ULID 기반 UserId를 데이터베이스 VARCHAR(26)와 변환
 */
@MappedTypes(UserId.class)
public class UserIdTypeHandler extends BaseTypeHandler<UserId> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UserId parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public UserId getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : UserId.of(value);
    }

    @Override
    public UserId getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : UserId.of(value);
    }

    @Override
    public UserId getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : UserId.of(value);
    }
}
