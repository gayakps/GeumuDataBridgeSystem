package com.velocitypowered.proxy.gaya.databse;

import com.velocitypowered.proxy.config.VelocityConfiguration;
import com.velocitypowered.proxy.gaya.history.manager.HistoryManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    private static HikariDataSource dataSource;
    static List<String> tableCreateList = new ArrayList<>();

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void init(VelocityConfiguration.DataBase dataBaseOption) {

        System.out.println(dataBaseOption.toString());

        HikariConfig config = new HikariConfig(); // Hikari Connection Pool 을 이용하기 위해서 사용되는 Configuration 이라 보면됨, 기본 설정
        config.setDriverClassName("com.mysql.cj.jdbc.Driver"); // 우리가 어떤 DBMS 를 사용할 것인지 => 저희는 Maria DB 이기때문에 mariadb jdbc driver 를 사용할거에요
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&allowPublicKeyRetrieval=true&useSSL=false&characterEncoding=UTF-8", dataBaseOption.getHost(), dataBaseOption.getPort(), dataBaseOption.getDatabase())); // 데이터 서버의 IP , port , DB 명
        config.setUsername(dataBaseOption.getUsername()); // config 에 등록된 관리자 계정명
        config.setPassword(dataBaseOption.getPassword()); // config 에 등록된 관리자 패스워드
        config.addDataSourceProperty("leak-detection-threshold", "true"); // PreparedStatement Caching을 비활성화하고 있기 때문에, 이 옵션을 허용해줘야 아래의 옵션값들이 실제 DB에 영향을 줄 수 있다.
        config.addDataSourceProperty("cachePrepStmts", "true"); // PreparedStatement Caching을 비활성화하고 있기 때문에, 이 옵션을 허용해줘야 아래의 옵션값들이 실제 DB에 영향을 줄 수 있다.
        // 여기서 PreparedStatement 는 Connection 을 가져와 db에 수정 삽입 제거 등 data 관리를 하기 위한 객체라고 보면된다.
        config.addDataSourceProperty("prepStmtCacheSize", "350"); // MySQL 드라이버가 Connection마다 캐싱할 PreparedStatement의 개수를 지정하는 옵션이다. HikariCP에서는 250 ~ 500개 정도를 추천한다
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048"); // default : 256 max : 2048 크게 중요하지 않다. 데이터 캐싱 관련이며 PreparedStatement 와 연관되어있다.

        config.setMaximumPoolSize(20);

        config.setMaxLifetime(580000);
        config.setIdleTimeout(10000);
        config.setConnectionTimeout(10000);
        config.setValidationTimeout(10000);
        config.setMinimumIdle(20);
        config.setPoolName("Geumu System");
        config.setLeakDetectionThreshold(24000);

        dataSource = new HikariDataSource(config);

        initTableList();
        System.out.println("[ GAYA_SOFT ] DB 접속 성공");

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = null;
            for (String tableSQL : tableCreateList) {
                if (preparedStatement != null) {
                    preparedStatement.clearParameters();
                    preparedStatement.clearBatch();
                }
                preparedStatement = connection.prepareStatement(tableSQL);
                preparedStatement.executeUpdate();
            }

            preparedStatement = connection.prepareStatement("DELETE FROM player_history\n" +
                    "WHERE STR_TO_DATE(LEFT(date, 10), '%Y-%m-%d') <= DATE_SUB(CURDATE(), INTERVAL ? DAY);\n");

            preparedStatement.setInt(1, HistoryManager.getInstance().getExpireDay());

            preparedStatement.executeUpdate();

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void close() {
        try {
            dataSource.close();
            System.out.println("DB 접속 정상 해제");
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }

    private static void initTableList() {



        tableCreateList.add("CREATE TABLE IF NOT EXISTS player_history\n" +
                "(\n" +
                "    history_id     int auto_increment\n" +
                "        primary key,\n" +
                "    name           varchar(30)  null,\n" +
                "    uuid           varchar(36)  null,\n" +
                "    server_name    varchar(20)  null,\n" +
                "    inventory      mediumblob   null,\n" +
                "    armor_contents mediumblob   null,\n" +
                "    ender_chest    mediumblob   null,\n" +
                "    pc             mediumblob   null,\n" +
                "    party          mediumblob   null,\n" +
                "    off_hand       mediumblob   null,\n" +
                "    location       varchar(100) null,\n" +
                "    type           tinyint  null,\n" +
                "    date           varchar(35)  null\n" +
                ");\n" +
                "\n");

        tableCreateList.add("CREATE TABLE IF NOT EXISTS player_data\n" +
                "(\n" +
                "    player_uuid   char(36)    not null,\n" +
                "    nbt_data      MEDIUMBLOB  not null,\n" +
                "    game_mode     varchar(20) not null,\n" +
                "    effects       longtext    null,\n" +
                "    poketmon_data MEDIUMBLOB  null,\n" +
                "    pc_data       MEDIUMBLOB  null,\n" +
                "    constraint player_data_pk\n" +
                "        primary key (player_uuid)\n" +
                ");\n" +
                "\n");


    }


}
