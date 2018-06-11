package project.services;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import project.models.ThreadModel;
import project.models.VoteModel;

import java.util.ArrayList;
import java.util.List;


@Service
public class ThreadService {

    private JdbcTemplate jdbcTemplate;
    private UserService userService;
    private ForumService forumService;

    public ThreadService(JdbcTemplate jdbcTemplate, UserService userService, ForumService forumService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userService = userService;
        this.forumService = forumService;
    }

    public ThreadModel create2(ThreadModel thread, String forumSlug) {
        String sql = "INSERT INTO threads(message, slug, title";
        ArrayList<String> values = new ArrayList<>();
        values.add(thread.getMessage());
        values.add(thread.getSlug());
        values.add(thread.getTitle());
        if (thread.getCreated() != null) {
            sql += ", created ";
        }
        sql += ", author_id, forum_id";
        sql += ") VALUES(?, ?::citext, ?, ";
        if (thread.getCreated() != null) {
            sql += "?::TIMESTAMPTZ, ";
            values.add(thread.getCreated());
        }
        sql += ("(SELECT id FROM users WHERE nickname = \'" + thread.getAuthor() + "\') , ");
        sql += ("(SELECT id FROM forums WHERE slug = \'" + forumSlug + "\')) RETURNING id");
        Integer newId = jdbcTemplate.queryForObject(sql, Integer.class, values.toArray());
        return getFullById(newId);
    }

    public ThreadModel create(ThreadModel thread, String forumSlug) {
        final StringBuilder sqlCreate = new StringBuilder();
        final List<Object> params = new ArrayList<>();
        final Integer userId = userService.getIdByName(thread.getAuthor());
        final Integer forumID = forumService.getIdBySlug(forumSlug);

        sqlCreate.append("INSERT INTO threads (author_id, ");
        params.add(userId);

        if (thread.getCreated() != null) {
            sqlCreate.append("created, ");
            params.add(thread.getCreated());
        }

        sqlCreate.append("forum_id, slug, message, title) VALUES(?, ");
        if (thread.getCreated() != null) {
            sqlCreate.append("?::TIMESTAMPTZ, ");
        }

        sqlCreate.append("?, ?::citext, ?, ?) RETURNING id");
        params.add(forumID);
        params.add(thread.getSlug());
        params.add(thread.getMessage());
        params.add(thread.getTitle());

        final Integer id = jdbcTemplate.queryForObject(sqlCreate.toString(), Integer.class, params.toArray());
        incrementThreadCount(forumSlug);
        updateForumUsers(userId, forumID);
        return getFullById(id);
    }

    private void updateForumUsers(Integer userId, Integer forumId) {
        final String sql =
            "INSERT INTO forum_users(user_id, forum_id) VALUES (?, ?) ON CONFLICT (user_id, forum_id) DO NOTHING";
        jdbcTemplate.update(sql, userId, forumId);
    }

    public ThreadModel getThread(ThreadModel thread, String forumSlug) {
        String sql =
            "SELECT (SELECT nickname FROM users WHERE nickname = ?::citext) as author, " +
            "(SELECT slug FROM forums where slug = ?::citext) as forum, " +
            "created, id, message, slug, title, votes FROM threads " +
            "WHERE  slug = ?::citext";

        return jdbcTemplate.queryForObject(sql, ThreadModel::getThread, thread.getAuthor(), forumSlug, thread.getSlug());
    }


    public ThreadModel getFullBySlugOrId(String slug_or_id) {
        ThreadModel thread;
        if (slug_or_id.matches("\\d+")) {
            thread = this.getFullById(Integer.parseInt(slug_or_id));
        } else {
            thread = this.getFullBySlug(slug_or_id);
        }
        return thread;
    }

    public Integer getIdBySlugOrId(String slug_or_id) {
        ThreadModel thread;
        if (slug_or_id.matches("\\d+")) {
            thread = this.getFullById(Integer.parseInt(slug_or_id));
        } else {
            thread = this.getFullBySlug(slug_or_id);
        }
        return thread.getId();
    }

    public ThreadModel getBySlugOrId(String slug_or_id) {
        ThreadModel thread;
        if (slug_or_id.matches("\\d+")) {
            thread = this.getById(Integer.parseInt(slug_or_id));
        } else {
            thread = this.getBySlug(slug_or_id);
        }
        return thread;
    }


    public ThreadModel getById(Integer id) {
        String sql = "SELECT * FROM threads WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, ThreadModel::getThread, id);
    }

    public ThreadModel getBySlug(String slug) {
        String sql = "SELECT * FROM threads WHERE slug = ?";
        return jdbcTemplate.queryForObject(sql, ThreadModel::getThread, slug);
    }


//    public ThreadModel getFullById(Integer id) {
//        final String sql =
//                "SELECT *, " +
//                "(SELECT slug FROM forums f WHERE t.forum_id = f.id) as forum, " +
//                "(SELECT nickname FROM users u WHERE t.author_id = u.id) as author FROM threads t WHERE id = ?";
//        return jdbcTemplate.queryForObject(sql, ThreadModel::getThread, id);
//    }


    public ThreadModel getFullBySlug(String slug) {
        final String sql =
            "SELECT *, " +
            "(SELECT slug FROM forums f WHERE t.forum_id = f.id) as forum, " +
            " (SELECT nickname FROM users u WHERE t.author_id = u.id) as author" +
            " FROM threads t WHERE slug = ?::citext";
        return jdbcTemplate.queryForObject(sql, ThreadModel::getThread, slug);
    }


    public Integer getForumIdBySlug(String slug) {
        String sql = "SELECT forum_id FROM threads WHERE slug = ?::citext";
        return jdbcTemplate.queryForObject(sql, Integer.class, slug);
    }

    public ThreadModel updateThread(ThreadModel thread, String slug_or_id) {
        ThreadModel oldThread = this.getFullBySlugOrId(slug_or_id);

        if (thread.getMessage() == null && thread.getMessage() ==  null && thread.getTitle() == null) {
            return oldThread;
        }

        StringBuilder sql = new StringBuilder("UPDATE threads SET");
        boolean needUpdate = false;
        ArrayList<Object> params = new ArrayList<>();

        if (thread.getMessage() != null) {
            sql.append(" message = ?");
            oldThread.setMessage(thread.getMessage());
            needUpdate = true;
            params.add(thread.getMessage());
        }

        if (thread.getTitle() != null && needUpdate) {
            sql.append(" , title = ?");
            oldThread.setTitle(thread.getTitle());
            params.add(thread.getTitle());
        } else if (thread.getTitle() != null) {
            sql.append(" title = ?");
            oldThread.setTitle(thread.getTitle());
            params.add(thread.getTitle());
        }

        sql.append(" WHERE id = ?");
        params.add(oldThread.getId());

        jdbcTemplate.update(sql.toString(), params.toArray());
        return getFullById(oldThread.getId());
    }


    private Integer getVoteValue(Integer threadId, Integer userId) {
        String sql = "SELECT voice FROM votes WHERE thread_id = ? AND user_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, threadId, userId);
    }




    //todo Слишком много подзапросов ?
    public ThreadModel updateVotes(VoteModel vote, String slugOrId) {
        ThreadModel thread = this.getFullBySlugOrId(slugOrId);
        Integer userId =
            jdbcTemplate.queryForObject("SELECT id FROM users WHERE nickname = ?", Integer.class, vote.getNickname());
        Integer oldVoteValue;

        try {
            oldVoteValue = this.getVoteValue(thread.getId(), userId);
        } catch (DataAccessException exception) {
            //Юзер еще не голосовал за этот тред
            jdbcTemplate.update("INSERT INTO votes(user_id, voice, thread_id) VALUES (?, ?, ?)",
                    userId, vote.getVoice(), thread.getId());

            jdbcTemplate.update("UPDATE threads SET votes = (SELECT SUM(voice) FROM votes" +
                    " WHERE thread_id = ?) WHERE id = ?", thread.getId(), thread.getId());
            return this.getFullBySlugOrId(slugOrId);
        }

        //Хочет тоже самое поставить
        if (oldVoteValue.equals(vote.getVoice())) {
            return thread;
        }

        jdbcTemplate.update("UPDATE votes SET voice = ?" +
                "WHERE thread_id = ? AND user_id = ?", vote.getVoice(), thread.getId(), userId);

        jdbcTemplate.update("UPDATE threads SET votes = (SELECT SUM(voice) FROM votes" +
                " WHERE thread_id = ?) WHERE id = ?", thread.getId(), thread.getId());
        return this.getFullBySlugOrId(slugOrId);
    }

    public Integer gerForumIdByThreadID(Integer threadId) {
        String sql = "SELECT forum_id FROM threads WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, threadId);
    }

    public void incrementThreadCount(String slug) {
        final String sqlUpdateThreadCount =
            "UPDATE forums SET threads = threads + 1 WHERE slug = ?::citext";
        jdbcTemplate.update(sqlUpdateThreadCount, slug);
    }

    public ThreadModel getFullById(Integer id) {
        final String sqlGetThreadById = "SELECT *, (SELECT nickname FROM users WHERE threads.author_id = id) as author," +
                " (SELECT slug FROM forums WHERE threads.forum_id = id) as forum FROM Threads where id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlGetThreadById, ThreadModel::getThread, id);
        } catch (DataAccessException ex) {
            return getFullBySlug(id.toString());
        }
    }
}