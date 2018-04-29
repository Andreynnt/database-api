package project.services;


import org.springframework.dao.DataAccessException;
import project.models.ForumModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import project.models.ThreadModel;
import project.models.UserModel;
import project.rowmapper.ApiRowMapper;

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
            "INSERT INTO forums (slug, title, user_id) VALUES (?::citext, ?, (SELECT id FROM users WHERE nickname = ?::citext))";
        jdbcTemplate.update(sql, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public ForumModel getForumBySlug(String slug) {
        final String sql = "SELECT f.slug, f.title, f.threads, f.posts, " +
                "(SELECT nickname FROM users WHERE id = f.user_id)::citext AS nickname " +
                "FROM forums f WHERE slug = ?::citext";
        return jdbcTemplate.queryForObject(sql, ApiRowMapper.getForum, slug);
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
        String sql = "SELECT (SELECT nickname FROM users WHERE id = t.author_id) AS author, created, " +
                "(SELECT slug FROM forums WHERE id = t.forum_id) AS forum, id, message, slug, title, votes " +
                "FROM threads AS t " +
                "WHERE forum_id = (SELECT id FROM forums WHERE slug = ?::citext) ";
        queryParams.add(slug);
        sql = this.addParamsForGetThreads(sql, queryParams, limit, since, desc);
        return jdbcTemplate.query(sql, ApiRowMapper.getThread, queryParams.toArray());
    }


    private Integer getIdBySLug(String slug) {
        return jdbcTemplate.queryForObject("SELECT id FROM forums f WHERE f.slug = ?::citext", Integer.class, slug);
    }


    //TO FO
    public List<UserModel> getUsers(String slug, Integer limit, String since, Boolean desc) {
        Integer forumID = getIdBySLug(slug);
        List<Object> args = new ArrayList<>();

        String sql = "select distinct u.nickname, u.about, u.email, u.fullname " +
                "from users u join posts p on p.user_id = u.id and p.forum_id = ? ";
        args.add(forumID);

        if (since != null) {
            if (desc != null && desc) {
                sql += " and nickname < ?::citext ";
            } else {
                //если нет desc то у меня сортировка по убыванию
                sql += " and nickname > ?::citext ";
            }
            args.add(since);
        }

        sql += " union ";
        sql += " select distinct users.nickname,  users.about, users.email, users.fullname FROM users  join threads th on th.author_id = users.id  and th.forum_id=?";
        args.add(forumID);

        if (since != null) {
            if (desc != null && desc) {
                sql += " and nickname < ?::citext ";
            } else {
                //если нет desc то у меня сортировка по убыванию
                sql += "  and nickname > ?::citext ";
            }
            args.add(since);
        }

        sql += " order by nickname";
        if (desc != null && desc != false) {
            sql += " desc ";
        }

        if (limit != null) {
            sql += " limit ? ";
            args.add(limit);
        }

        return jdbcTemplate.query(sql, ApiRowMapper.getUser, args.toArray());
    }


    public String getSlugById(Integer id) {
        String sql = "SELECT slug FROM forums WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }


    public void incrementPosts(Integer forumID) {
        final String sqlUpdatePostCount = "UPDATE Forums SET posts = posts + 1 WHERE id = ?";
        jdbcTemplate.update(sqlUpdatePostCount, forumID);
    }
}
