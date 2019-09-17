package com.js.sas.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfigration {
    @Bean(name = "firstJdbcTemplate")
    @Primary
    public JdbcTemplate primaryJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Primary
    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("secondProperties")
    @ConfigurationProperties(prefix = "spring.second")
    public DataSourceProperties secondProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "secodDataSource")
    public DataSource secodDataSource(@Qualifier(value = "secondProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
    @Bean(name = "secodJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate(@Qualifier("secodDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 用友SQLServer数据源
     * @return
     */
    @Bean("sqlServerProperties")
    @ConfigurationProperties(prefix = "spring.sqlserver")
    public DataSourceProperties sqlServerProperties() {
        return new DataSourceProperties();
    }
    @Bean(name = "sqlServerDataSource")
    public DataSource sqlServerDataSource(@Qualifier(value = "sqlServerProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
    @Bean(name = "sqlServerJdbcTemplate")
    public JdbcTemplate sqlServerJdbcTemplate(@Qualifier("sqlServerDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
