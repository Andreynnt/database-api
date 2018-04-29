package project.services;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.models.DatabaseModel;

@Service
public class ManagerService {

    private JdbcTemplate jdbcTemplate;

    public ManagerService(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DatabaseModel databaseStatus() {
        Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer posts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", Integer.class);
        Integer threads = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM threads", Integer.class);
        Integer forums = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM forums", Integer.class);
        return new DatabaseModel(forums, posts, threads, users);
    }

    public void clear() {
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM threads");
        jdbcTemplate.execute("DELETE FROM forums");
    }

}
