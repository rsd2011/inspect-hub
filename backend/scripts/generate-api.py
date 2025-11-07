#!/usr/bin/env python3
"""
API Generator for Spring Boot Multi-Module Project

⚠️  IMPORTANT: All generated APIs include Swagger/OpenAPI annotations
    - Controllers have @Tag and @Operation
    - DTOs have @Schema with descriptions and examples
    - Access Swagger UI at: http://localhost:8090/swagger-ui.html

Generates:
- Controller (REST API endpoint with Swagger annotations)
- Service (Business logic)
- DTO (Request/Response with Swagger @Schema)
- MyBatis Mapper Interface
- MyBatis XML Mapper
- Unit Tests

Usage:
    python scripts/generate-api.py --module=auth --entity=User --api=create-user
    python scripts/generate-api.py --module=policy --entity=Policy --api=list-policies

Generated APIs are automatically documented in Swagger UI.
"""

import argparse
import os
import sys
from pathlib import Path
from datetime import datetime


class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    RESET = '\033[0m'


def success(msg):
    print(f"{Colors.GREEN}✅ {msg}{Colors.RESET}")


def error(msg):
    print(f"{Colors.RED}❌ {msg}{Colors.RESET}")


def warning(msg):
    print(f"{Colors.YELLOW}⚠️  {msg}{Colors.RESET}")


def info(msg):
    print(f"{Colors.BLUE}ℹ️  {msg}{Colors.RESET}")


def to_pascal_case(text):
    """Convert kebab-case to PascalCase"""
    return ''.join(word.capitalize() for word in text.split('-'))


def to_camel_case(text):
    """Convert kebab-case to camelCase"""
    pascal = to_pascal_case(text)
    return pascal[0].lower() + pascal[1:]


def generate_controller(module, entity, api_name):
    """Generate REST Controller"""
    pascal_entity = to_pascal_case(entity)
    pascal_api = to_pascal_case(api_name)
    camel_api = to_camel_case(api_name)

    return f"""package com.inspecthub.{module}.controller;

import com.inspecthub.{module}.dto.{pascal_api}Request;
import com.inspecthub.{module}.dto.{pascal_api}Response;
import com.inspecthub.{module}.service.{pascal_entity}Service;
import com.inspecthub.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * {pascal_entity} REST Controller
 *
 * @author Inspect-Hub
 * @since {datetime.now().strftime('%Y-%m-%d')}
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/{entity.lower()}")
@RequiredArgsConstructor
@Tag(name = "{pascal_entity}", description = "{pascal_entity} API")
public class {pascal_entity}Controller {{

    private final {pascal_entity}Service {entity.lower()}Service;

    /**
     * {pascal_api}
     */
    @PostMapping("/{api_name}")
    @Operation(summary = "{pascal_api}", description = "{pascal_api} operation")
    public ResponseEntity<ApiResponse<{pascal_api}Response>> {camel_api}(
            @Valid @RequestBody {pascal_api}Request request
    ) {{
        log.info("[{pascal_api}] request: {{}}", request);

        {pascal_api}Response response = {entity.lower()}Service.{camel_api}(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }}
}}
"""


def generate_service(module, entity, api_name):
    """Generate Service"""
    pascal_entity = to_pascal_case(entity)
    pascal_api = to_pascal_case(api_name)
    camel_api = to_camel_case(api_name)

    return f"""package com.inspecthub.{module}.service;

import com.inspecthub.{module}.dto.{pascal_api}Request;
import com.inspecthub.{module}.dto.{pascal_api}Response;
import com.inspecthub.{module}.mapper.{pascal_entity}Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {pascal_entity} Service
 *
 * @author Inspect-Hub
 * @since {datetime.now().strftime('%Y-%m-%d')}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class {pascal_entity}Service {{

    private final {pascal_entity}Mapper {entity.lower()}Mapper;

    /**
     * {pascal_api}
     */
    @Transactional
    public {pascal_api}Response {camel_api}({pascal_api}Request request) {{
        log.debug("[{pascal_api}] Processing request: {{}}", request);

        // TODO: Implement business logic

        // Example:
        // 1. Validate request
        // 2. Call mapper to interact with database
        // 3. Transform entity to response DTO

        return {pascal_api}Response.builder()
                // .field(value)
                .build();
    }}
}}
"""


def generate_dto_request(module, api_name):
    """Generate Request DTO"""
    pascal_api = to_pascal_case(api_name)

    return f"""package com.inspecthub.{module}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {pascal_api} Request DTO
 *
 * @author Inspect-Hub
 * @since {datetime.now().strftime('%Y-%m-%d')}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "{pascal_api} Request")
public class {pascal_api}Request {{

    // TODO: Define request fields

    // Example:
    // @NotBlank(message = "Name is required")
    // @Schema(description = "Name", example = "Example Name")
    // private String name;

    // @NotNull(message = "Type is required")
    // @Schema(description = "Type", example = "TYPE_A")
    // private String type;
}}
"""


def generate_dto_response(module, api_name):
    """Generate Response DTO"""
    pascal_api = to_pascal_case(api_name)

    return f"""package com.inspecthub.{module}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * {pascal_api} Response DTO
 *
 * @author Inspect-Hub
 * @since {datetime.now().strftime('%Y-%m-%d')}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "{pascal_api} Response")
public class {pascal_api}Response {{

    // TODO: Define response fields

    // Example:
    // @Schema(description = "ID", example = "12345")
    // private String id;

    // @Schema(description = "Name", example = "Example Name")
    // private String name;

    // @Schema(description = "Created timestamp")
    // private LocalDateTime createdAt;
}}
"""


def generate_mapper_interface(module, entity):
    """Generate MyBatis Mapper Interface"""
    pascal_entity = to_pascal_case(entity)

    return f"""package com.inspecthub.{module}.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * {pascal_entity} MyBatis Mapper
 *
 * @author Inspect-Hub
 * @since {datetime.now().strftime('%Y-%m-%d')}
 */
@Mapper
public interface {pascal_entity}Mapper {{

    // TODO: Define mapper methods

    // Example:
    // int insert(Map<String, Object> params);
    //
    // int update(Map<String, Object> params);
    //
    // int delete(String id);
    //
    // Map<String, Object> selectById(String id);
    //
    // List<Map<String, Object>> selectList(Map<String, Object> params);
}}
"""


def generate_mapper_xml(module, entity):
    """Generate MyBatis XML Mapper"""
    pascal_entity = to_pascal_case(entity)

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.inspecthub.{module}.mapper.{pascal_entity}Mapper">

    <!-- TODO: Define SQL queries -->

    <!-- Example: Insert -->
    <!--
    <insert id="insert" parameterType="map">
        INSERT INTO {entity.lower()} (
            id,
            name,
            type,
            created_at,
            created_by
        ) VALUES (
            #{{id}},
            #{{name}},
            #{{type}},
            NOW(),
            #{{createdBy}}
        )
    </insert>
    -->

    <!-- Example: Select by ID -->
    <!--
    <select id="selectById" parameterType="string" resultType="map">
        SELECT
            id,
            name,
            type,
            created_at AS createdAt,
            created_by AS createdBy
        FROM {entity.lower()}
        WHERE id = #{{id}}
    </select>
    -->

    <!-- Example: Select List -->
    <!--
    <select id="selectList" parameterType="map" resultType="map">
        SELECT
            id,
            name,
            type,
            created_at AS createdAt
        FROM {entity.lower()}
        <where>
            <if test="name != null">
                AND name LIKE CONCAT('%', #{{name}}, '%')
            </if>
            <if test="type != null">
                AND type = #{{type}}
            </if>
        </where>
        ORDER BY created_at DESC
        LIMIT #{{limit}} OFFSET #{{offset}}
    </select>
    -->

</mapper>
"""


def generate_test(module, entity, api_name):
    """Generate Unit Test"""
    pascal_entity = to_pascal_case(entity)
    pascal_api = to_pascal_case(api_name)
    camel_api = to_camel_case(api_name)

    return f"""package com.inspecthub.{module}.service;

import com.inspecthub.{module}.dto.{pascal_api}Request;
import com.inspecthub.{module}.dto.{pascal_api}Response;
import com.inspecthub.{module}.mapper.{pascal_entity}Mapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {pascal_entity}Service Test
 *
 * @author Inspect-Hub
 * @since {datetime.now().strftime('%Y-%m-%d')}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("{pascal_entity}Service Tests")
class {pascal_entity}ServiceTest {{

    @Mock
    private {pascal_entity}Mapper {entity.lower()}Mapper;

    @InjectMocks
    private {pascal_entity}Service {entity.lower()}Service;

    @Test
    @DisplayName("{pascal_api} should return valid response")
    void {camel_api}_shouldReturnValidResponse() {{
        // Given
        {pascal_api}Request request = {pascal_api}Request.builder()
                // .field(value)
                .build();

        // When
        {pascal_api}Response response = {entity.lower()}Service.{camel_api}(request);

        // Then
        assertThat(response).isNotNull();
        // Add more assertions

        // Verify mapper interactions
        // verify({entity.lower()}Mapper, times(1)).someMethod(any());
    }}
}}
"""


def create_file(path, content):
    """Create file with content"""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding='utf-8')
    success(f"Created: {path}")


def main():
    parser = argparse.ArgumentParser(description='Generate API components for Spring Boot')
    parser.add_argument('--module', required=True, help='Module name (e.g., auth, policy)')
    parser.add_argument('--entity', required=True, help='Entity name (e.g., User, Policy)')
    parser.add_argument('--api', required=True, help='API name in kebab-case (e.g., create-user)')

    args = parser.parse_args()

    module = args.module
    entity = args.entity
    api_name = args.api

    # Validate module exists
    backend_dir = Path(__file__).parent.parent
    module_dir = backend_dir / module

    if not module_dir.exists():
        error(f"Module '{module}' does not exist")
        sys.exit(1)

    info(f"Generating API components for {module}/{entity}...")

    # Paths
    base_path = module_dir / 'src' / 'main' / 'java' / 'com' / 'inspecthub' / module
    test_path = module_dir / 'src' / 'test' / 'java' / 'com' / 'inspecthub' / module
    resources_path = module_dir / 'src' / 'main' / 'resources' / 'mybatis' / 'mapper'

    pascal_entity = to_pascal_case(entity)
    pascal_api = to_pascal_case(api_name)

    # Generate files
    files = [
        (base_path / 'controller' / f'{pascal_entity}Controller.java',
         generate_controller(module, entity, api_name)),

        (base_path / 'service' / f'{pascal_entity}Service.java',
         generate_service(module, entity, api_name)),

        (base_path / 'dto' / f'{pascal_api}Request.java',
         generate_dto_request(module, api_name)),

        (base_path / 'dto' / f'{pascal_api}Response.java',
         generate_dto_response(module, api_name)),

        (base_path / 'mapper' / f'{pascal_entity}Mapper.java',
         generate_mapper_interface(module, entity)),

        (resources_path / f'{pascal_entity}Mapper.xml',
         generate_mapper_xml(module, entity)),

        (test_path / 'service' / f'{pascal_entity}ServiceTest.java',
         generate_test(module, entity, api_name)),
    ]

    for file_path, content in files:
        create_file(file_path, content)

    print()
    success(f"API components generated successfully for {module}/{entity}!")
    print()
    info("Next steps:")
    print(f"  1. Implement business logic in {pascal_entity}Service")
    print(f"  2. Define SQL queries in {pascal_entity}Mapper.xml")
    print(f"  3. Add validation to {pascal_api}Request")
    print(f"  4. Write tests in {pascal_entity}ServiceTest")
    print(f"  5. Run: ./gradlew :{module}:test")


if __name__ == '__main__':
    main()
