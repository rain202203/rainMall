package com.rain.mall.common.config;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.rain.mall.*.dao", sqlSessionFactoryRef = "localSqlSessionFactory")
public class LocalDruidConfig {
    @Value("${local.jdbc.driverClassName}")
    private String driverClassName;
    @Value("${local.jdbc.url}")
    private String url;
    @Value("${local.jdbc.username}")
    private String username;
    @Value("${local.jdbc.password}")
    private String password;
    @Value("${local.jdbc.pool.initialSize}")
    private String initialSize;
    @Value("${local.jdbc.pool.minIdle}")
    private String minIdle;
    @Value("${local.jdbc.pool.maxActive}")
    private String maxActive;
    @Value("${local.mybatis.typeAliasesPackage}")
    private String typeAliasesPackage;
    @Value("${local.mybatis.mapperLocations}")
    private String mapperLocations;


    /**
     * ??????druid?????????????????????
     *
     * @return
     */
    @Bean(name = "localWallConfig")
    WallConfig wallFilterConfig() {
        WallConfig wc = new WallConfig();
        wc.setMultiStatementAllow(true);
        return wc;
    }

    @Bean(name = "localWallFilter")
    @DependsOn("localWallConfig")
    WallFilter wallFilter(WallConfig wallConfig) {
        WallFilter filter = new WallFilter();
        filter.setConfig(wallConfig);
        return filter;
    }

    @Bean(name = "localDataSource")
    @Primary
    public DataSource getDataSource() throws Exception {
        Properties props = new Properties();
        props.put("driverClassName", driverClassName);
        props.put("url", url);
        props.put("username", username);
        props.put("password", password);
        props.put("initialSize", initialSize);
        props.put("minIdle", minIdle);
        props.put("maxActive", maxActive);
        props.put("maxWait", "60000");
        props.put("filters", "stat,wallFilter,slf4j");
        props.put("validationQuery", "SELECT 'x'");
        props.put("testWhileIdle", "true");
        //?????????????????????????????????????????????????????????????????????????????????????????????
        props.put("timeBetweenEvictionRunsMillis", "60000");
        // ??????????????????????????????????????????????????????????????????
        props.put("minEvictableIdleTimeMillis", "300000");
        //?????????true??????????????????
        props.put("testOnBorrow", "true");
        props.put("testOnReturn", "false");

        return DruidDataSourceFactory.createDataSource(props);
    }

    @Bean(name = "localSqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("localDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        //???????????????
        fb.setDataSource(dataSource);
        //????????????
        fb.setTypeAliasesPackage(typeAliasesPackage);
        //??????xml????????????
        fb.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperLocations));
        return fb.getObject();
    }

    @Bean(name = "localSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("localSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


    @Bean(name = "localTransactionManager")
    @Primary
    public DataSourceTransactionManager transactionManager(@Qualifier("localDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    /**
     * ????????????StatViewServlet
     *
     * @return
     */
    @Bean(name = "localServletRegistrationBean")
    public ServletRegistrationBean druidStatViewServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        /** ????????? */
        servletRegistrationBean.addInitParameter("loginUsername", "druid");
        /** ?????? */
        servletRegistrationBean.addInitParameter("loginPassword", "druid");
        /** ?????????????????????Reset All????????? */
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        return servletRegistrationBean;
    }

    /**
     * ???????????????WebStatFilter
     *
     * @return
     */
    @Bean(name = "localFilterRegistrationBean")
    public FilterRegistrationBean druidStatFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        /** ???????????? */
        filterRegistrationBean.addUrlPatterns("/*");
        /** ???????????? */
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid2/*");
        return filterRegistrationBean;
    }


}
