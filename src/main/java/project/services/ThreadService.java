package project.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import project.models.ThreadModel;
import project.rowmapper.ApiRowMapper;

import java.util.ArrayList;


@Service
public class ThreadService {

    private JdbcTemplate jdbcTemplate;


    public ThreadService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ThreadModel create(ThreadModel thread, String forumSlug) {
        String sql = "INSERT INTO threads(message, slug, title";

        ArrayList<String> values = new ArrayList<>();
        values.add(thread.getMessage());
        values.add(thread.getSlug());
        values.add(thread.getTitle());

        if (thread.getCreated() != null) {
            sql += ", created ";
        }

        sql += ", author_id, forum_id";
        sql += ") VALUES(?, ?, ?, ";

        // не работает, если пихать время создания

        if (thread.getCreated() != null) {
            sql += "?, ";
            values.add(thread.getCreated());
        }

        sql += ("(SELECT id FROM users WHERE nickname = \'" + thread.getAuthor() + "\') , ");
        sql += ("(SELECT id FROM forums WHERE slug = \'" + forumSlug + "\')) ");

        jdbcTemplate.update(sql, values.toArray());
        return getThread(thread, forumSlug);
    }

    public ThreadModel getThread(ThreadModel thread, String forumSlug) {
        String sql =
            "SELECT (SELECT nickname FROM users WHERE nickname = ?) as author, " +
            "(SELECT slug FROM forums where slug = ?) as forum, " +
            "created, id, message, slug, title, votes FROM threads " +
            "WHERE  slug = ?";

        return jdbcTemplate.queryForObject(sql, ApiRowMapper.getThread, thread.getAuthor(), forumSlug, thread.getSlug());
    }

    public void incrementForumThreads(String slug) {
        String sql = "UPDATE forums SET threads = threads + 1 WHERE slug = ?";
        jdbcTemplate.update(sql, slug);
    }

}
