package project.services;


import project.models.ForumModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import project.models.ThreadModel;
import project.models.UserModel;

import java.util.ArrayList;
import java.util.List;

@Service
public class ForumService {

    private JdbcTemplate jdbcTemplate;


    public ForumService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(ForumModel forum) {
        final String sql =
            "INSERT INTO forums (slug, user_id, title) VALUES (?::citext, (SELECT id FROM users WHERE nickname = ?::citext), ?)";
        jdbcTemplate.update(sql, forum.getSlug(), forum.getUser(), forum.getTitle());
    }

    public ForumModel getForumBySlug(String slug) {
        final String sql =
            "SELECT f.slug, f.posts, f.title, f.threads, " +
            "(SELECT nickname FROM users WHERE id = f.user_id)::citext AS nickname " +
            "FROM forums f WHERE slug = ?::citext";
        return jdbcTemplate.queryForObject(sql, ForumModel::getForum, slug);
    }


    private String addParamsForGetThreads(String sql, ArrayList<Object> queryParams, Integer limit, String since, Boolean desc) {
        if (since != null) {
            queryParams.add(since);
            if (desc != null && desc) {
                sql += (" AND created <= ?::TIMESTAMPTZ ");
            } else {
                sql += (" AND created >= ?::TIMESTAMPTZ ");
            }
        }

        if (desc != null && desc) {
            sql += " ORDER BY created DESC";
        } else {
            //если нет desc то у меня сортировка по убыванию
            sql += " ORDER BY created ASC";
        }

        if (limit != null) {
            queryParams.add(limit);
            sql += " LIMIT ?";
        }
        return sql;
    }

    public List<ThreadModel> getThreads(String slug, Integer limit, String since, Boolean desc) {
        ArrayList<Object> queryParams = new ArrayList<>();
        String sql =
            "SELECT (SELECT nickname FROM users WHERE id = t.author_id) AS author, created, " +
            "(SELECT slug FROM forums WHERE id = t.forum_id) AS forum, id, message, slug, title, votes " +
            "FROM threads AS t " +
            "WHERE forum_id = (SELECT id FROM forums WHERE slug = ?::citext) ";
        queryParams.add(slug);
        sql = this.addParamsForGetThreads(sql, queryParams, limit, since, desc);
        return jdbcTemplate.query(sql, ThreadModel::getThread, queryParams.toArray());
    }

    public String getSlugById(Integer id) {
        String sql = "SELECT slug FROM forums WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }

    public Integer getIdBySlug(String slug) {
        String sql = "SELECT id FROM forums WHERE slug = ?::citext";
        return jdbcTemplate.queryForObject(sql, Integer.class, slug);
    }

    public void increasePostsAmount(Integer forumID, Integer size) {
        final String sqlUpdatePostCount = "UPDATE forums SET posts = posts + ? WHERE id = ?";
        jdbcTemplate.update(sqlUpdatePostCount, size, forumID);
    }

    public List<UserModel> getUsers(String slug, String since, Boolean desc, Integer limit) {
        final Integer forumId = getIdBySlug(slug);
        final List<Object> params = new ArrayList<>();
        params.add(forumId);

        String sql =
            "SELECT u.nickname, u.fullname, u.email, u.about FROM users u WHERE u.id IN " +
            "(SELECT user_id FROM forum_users WHERE forum_id = ?)";

        if (since != null) {
            params.add(since);
            sql += " AND u.nickname ";
            if ( desc != null && desc.equals(Boolean.TRUE)) {
                sql += " < ?::citext ";
            } else {
                sql += " > ?::citext ";
            }
        }
        sql += " ORDER BY u.nickname ";
        if (desc != null) {
            if (desc.equals(Boolean.TRUE)) {
                sql += " DESC ";
            }
        }

        if (limit != null) {
            params.add(limit);
            sql +=" LIMIT ? ";
        }
        return jdbcTemplate.query(sql, UserModel::getUser, params.toArray());
    }
}

