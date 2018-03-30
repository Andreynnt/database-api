package project.services;


import project.models.ForumModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import project.rowmapper.ApiRowMapper;

@Service
public class ForumService {

    private JdbcTemplate jdbcTemplate;


    public ForumService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(ForumModel forum) {
        final String sql =
            "INSERT INTO forums (slug, title, user_id) VALUES (?, ?, (SELECT id FROM users WHERE nickname = ?))";
        jdbcTemplate.update(sql, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public ForumModel getForumBySlug(String slug) {
        final String sql = "SELECT f.slug, f.title, f.threads, f.posts, " +
                "(SELECT nickname FROM users WHERE id = f.user_id) AS nickname " +
                "FROM forums AS f " +
                "WHERE slug = ?";
        return jdbcTemplate.queryForObject(sql, ApiRowMapper.getForum, slug);
    }
}
