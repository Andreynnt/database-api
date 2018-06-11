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
            if (desc != null && desc) {
                sql += (" AND created <= ?::TIMESTAMPTZ ");
            } else {
                sql += (" AND created >= ?::TIMESTAMPTZ ");
            }
            queryParams.add(since);
        }

        if (desc != null && desc) {
            sql += " ORDER BY created DESC";
        } else {
            //если нет desc то у меня сортировка по убыванию
            sql += " ORDER BY created ASC";
        }

        if (limit != null) {
            sql += " LIMIT ?";
            queryParams.add(limit);
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


    private Integer getIdBySLug(String slug) {
        return jdbcTemplate.queryForObject("SELECT id FROM forums f WHERE f.slug = ?::citext", Integer.class, slug);
    }


    public List<UserModel> getUsers(String slug, Integer limit, String since, Boolean desc) {
        Integer forumID = getIdBySLug(slug);
        List<Object> args = new ArrayList<>();
        String sql =
            "SELECT DISTINCT u.nickname, u.about, u.email, u.fullname " +
            "FROM users u JOIN posts p ON p.user_id = u.id AND p.forum_id = ? ";
        args.add(forumID);

        if (since != null) {
            if (desc != null && desc) {
                sql += " AND nickname < ?::citext ";
            } else {
                //если нет desc то у меня сортировка по убыванию
                sql += " AND nickname > ?::citext ";
            }
            args.add(since);
        }

        sql += " UNION ";
        sql += " SELECT DISTINCT users.nickname,  users.about, users.email, users.fullname FROM users JOIN threads th ON th.author_id = users.id AND th.forum_id = ?";
        args.add(forumID);

        if (since != null) {
            if (desc != null && desc) {
                sql += " AND nickname < ?::citext ";
            } else {
                //если нет desc то у меня сортировка по убыванию
                sql += "  AND nickname > ?::citext ";
            }
            args.add(since);
        }
        sql += " ORDER BY nickname";
        if (desc != null && desc != false) {
            sql += " DESC ";
        }
        if (limit != null) {
            sql += " LIMIT ? ";
            args.add(limit);
        }
        return jdbcTemplate.query(sql, UserModel::getUser, args.toArray());
    }


    public String getSlugById(Integer id) {
        String sql = "SELECT slug FROM forums WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }

    public Integer getIdBySlug(String slug) {
        String sql = "SELECT id FROM forums WHERE slug = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, slug);
    }

    public void incrementPosts(Integer forumID) {
        final String sqlUpdatePostCount = "UPDATE forums SET posts = posts + 1 WHERE id = ?";
        jdbcTemplate.update(sqlUpdatePostCount, forumID);
    }

    public void increasePostsAmount(Integer forumID, Integer size) {
        final String sqlUpdatePostCount = "UPDATE forums SET posts = posts + ? WHERE id = ?";
        jdbcTemplate.update(sqlUpdatePostCount, size, forumID);
    }
}
