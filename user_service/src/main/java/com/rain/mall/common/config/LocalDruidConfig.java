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
     * 解决druid批量更新的问题
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
        //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        props.put("timeBetweenEvictionRunsMillis", "60000");
        // 配置一个连接在池中最小生存的时间，单位是毫秒
        props.put("minEvictableIdleTimeMillis", "300000");
        //设置为true以后影响性能
        props.put("testOnBorrow", "true");
        props.put("testOnReturn", "false");

        return DruidDataSourceFactory.createDataSource(props);
    }

    @Bean(name = "localSqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("localDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        //指定数据源
        fb.setDataSource(dataSource);
        //指定基包
        fb.setTypeAliasesPackage(typeAliasesPackage);
        //指定xml文件位置
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
     * 注册一个StatViewServlet
     *
     * @return
     */
    @Bean(name = "localServletRegistrationBean")
    public ServletRegistrationBean druidStatViewServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        /** 用户名 */
        servletRegistrationBean.addInitParameter("loginUsername", "druid");
        /** 密码 */
        servletRegistrationBean.addInitParameter("loginPassword", "druid");
        /** 禁用页面上的“Reset All”功能 */
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        return servletRegistrationBean;
    }

    /**
     * 注册一个：WebStatFilter
     *
     * @return
     */
    @Bean(name = "localFilterRegistrationBean")
    public FilterRegistrationBean druidStatFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        /** 过滤规则 */
        filterRegistrationBean.addUrlPatterns("/*");
        /** 忽略资源 */
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid2/*");
        return filterRegistrationBean;
    }


}
