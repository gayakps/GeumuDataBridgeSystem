package gaya.pe.kr.core.history.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gaya.pe.kr.network.packet.startDirection.server.send.JDBCDataSend;
import gaya.pe.kr.util.ConfigurationManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    private static HikariDataSource dataSource;

    static boolean init = false;

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void init(FileConfiguration configuration) {

        String host = configuration.getString("db.db_host");
        String port = configuration.getString("db.db_port");
        String database = configuration.getString("db.db_database");
        String username = configuration.getString("db.db_username");
        String password = configuration.getString("db.db_password");

        HikariConfig config = new HikariConfig(); // Hikari Connection Pool 을 이용하기 위해서 사용되는 Configuration 이라 보면됨, 기본 설정
        config.setDriverClassName("com.mysql.cj.jdbc.Driver"); // 우리가 어떤 DBMS 를 사용할 것인지 => 저희는 Maria DB 이기때문에 mariadb jdbc driver 를 사용할거에요
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&allowPublicKeyRetrieval=true&useSSL=false&characterEncoding=UTF-8", host, port, database)); // 데이터 서버의 IP , port , DB 명
        config.setUsername(username); // config 에 등록된 관리자 계정명
        config.setPassword(password); // config 에 등록된 관리자 패스워드
        config.addDataSourceProperty("leak-detection-threshold", "true"); // PreparedStatement Caching을 비활성화하고 있기 때문에, 이 옵션을 허용해줘야 아래의 옵션값들이 실제 DB에 영향을 줄 수 있다.
        config.addDataSourceProperty("cachePrepStmts", "true"); // PreparedStatement Caching을 비활성화하고 있기 때문에, 이 옵션을 허용해줘야 아래의 옵션값들이 실제 DB에 영향을 줄 수 있다.
        // 여기서 PreparedStatement 는 Connection 을 가져와 db에 수정 삽입 제거 등 data 관리를 하기 위한 객체라고 보면된다.
        config.addDataSourceProperty("prepStmtCacheSize", "350"); // MySQL 드라이버가 Connection마다 캐싱할 PreparedStatement의 개수를 지정하는 옵션이다. HikariCP에서는 250 ~ 500개 정도를 추천한다
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048"); // default : 256 max : 2048 크게 중요하지 않다. 데이터 캐싱 관련이며 PreparedStatement 와 연관되어있다.

        config.setMaximumPoolSize(3);

        config.setMaxLifetime(580000);
        config.setIdleTimeout(10000);
        config.setConnectionTimeout(10000);
        config.setValidationTimeout(10000);
        config.setMinimumIdle(20);
        config.setPoolName("Geumu System");
        config.setLeakDetectionThreshold(24000);

        dataSource = new HikariDataSource(config);

        System.out.println("[ GAYA_SOFT ] DB 접속 성공");

    }

    public static void close() {
        try {
            dataSource.close();
            System.out.println("DB 접속 정상 해제");
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }


}
