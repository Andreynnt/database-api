package project.services;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.models.PostFullModel;
import project.models.PostModel;
import project.models.ThreadModel;
import project.rowmapper.ApiRowMapper;

import java.sql.Array;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

@Service
public class PostService {

    private JdbcTemplate jdbcTemplate;
    private UserService userService;
    private ThreadService threadService;
    private ForumService forumService;

    public PostService(JdbcTemplate jdbcTemplate, ForumService forumService,
                       UserService userService, ThreadService threadService) {
        this.jdbcTemplate = jdbcTemplate;
        this.threadService = threadService;
        this.forumService = forumService;
        this.userService = userService;
    }



    private Integer getParent(Integer threadID, Integer parentID) {
        String sql = "SELECT id FROM posts WHERE thread_id = ? AND id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, threadID, parentID);
    }

    private Array getPathById(Integer id) {
        String sql = "SELECT path FROM posts WHERE id = ?";
        return  jdbcTemplate.queryForObject(sql, Array.class, id);
    }

    private Integer generateId() {
        return jdbcTemplate.
            queryForObject("SELECT nextval(pg_get_serial_sequence('posts', 'id'))", Integer.class);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<PostModel> create(List<PostModel> posts, String slug_or_id) {
        final String sql = "INSERT INTO posts (id, user_id, created, forum_id, message," +
                " parent, thread_id, path, root_id)" +
                " VALUES(?, ?, ?::TIMESTAMPTZ, ?, ?, ?, ?, array_append(?, ?::INTEGER), ?)";

        final ThreadModel thread;
        if (slug_or_id.matches("\\d+")) {
            thread = threadService.getFullById(Integer.parseInt(slug_or_id));
        } else {
            thread = threadService.getFullBySlug(slug_or_id);
        }

        final String currentTime = ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        final Integer forumID = threadService.getForumIdBySlug(thread.getSlug());
        final String forumSlug = forumService.getSlugById(forumID);

        for (PostModel post : posts) {
            Integer parentID;

            if (post.getParent() == null || post.getParent() == 0) {
                parentID = 0;
            } else {
                try {
                    parentID = this.getParent(thread.getId(), post.getParent());
                } catch (DataAccessException exception) {
                    throw new DuplicateKeyException("NO_PARENTS");
                }
            }

            Array path = null;

            post.setThread(thread.getId());
            post.setForum(forumSlug);
            post.setCreated(currentTime);

            final Integer generatedID = this.generateId();
            Integer rootID;

            if (parentID != 0) {
                path = this.getPathById(parentID);
                try {
                    rootID = ((Integer[]) path.getArray())[0];
                } catch (SQLException error) {
                    rootID = generatedID;
                }
            } else {
                rootID = generatedID;
            }

            post.setId(generatedID);
            post.setParent(parentID);
            Integer userID = userService.getIdByName(post.getAuthor());

            jdbcTemplate.update(sql, generatedID, userID, currentTime, forumID,
                post.getMessage(), parentID, thread.getId(), path, generatedID, rootID);

            forumService.incrementPosts(forumID);
        }
        return posts;
    }


    private String getPostMessage(Integer id) {
        final String sql = "SELECT message FROM posts WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }


    private PostModel getPost(Integer id) {
        final String sql = "SELECT (SELECT nickname from users WHERE id = p.user_id) as author, " +
                " (SELECT slug FROM Forums WHERE id = p.forum_id) as forum, " +
                " created, id, message, parent, is_edited as isEdited, thread_id as thread " +
                " FROM posts p where id = ?";
        return jdbcTemplate.queryForObject(sql, ApiRowMapper.getPost, id);
    }


    public PostModel updatePost(PostModel post, Integer id) {
        final String oldMessage = this.getPostMessage(id);
        final String sql = "UPDATE posts SET message = ?, is_edited = TRUE WHERE id = ?";

        if (post.getMessage() == null) {
           return getPost(id);
        }

        if (post.getMessage().equals(oldMessage)) {
            return getPost(id);
        }

        jdbcTemplate.update(sql, post.getMessage(), id);
        return this.getPost(id);
    }


    public PostFullModel getFullPost(Integer id, List<String> related) {
        String sql =
        "SELECT (SELECT nickname from users WHERE id = p.user_id) as author," +
        " (SELECT slug FROM forums WHERE id = p.forum_id) as forum, " +
        " created, id, is_edited as isEdited, message, parent," +
        " thread_id as thread " +
        " FROM posts p WHERE id = ?";

        final PostModel post = jdbcTemplate.queryForObject(sql, ApiRowMapper.getPost, id);
        final PostFullModel fullData = new PostFullModel(post);

        if (related == null) {
            return fullData;
        }

        if (related.contains("forum")) {
            fullData.setForum(forumService.getForumBySlug(post.getForum()));
        }
        if (related.contains("user")) {
            fullData.setAuthor(userService.getUser(post.getAuthor()));
        }
        if (related.contains("thread")) {
            fullData.setThread(threadService.getFullById(post.getThread()));
        }

        return fullData;
    }



    public List<PostModel> getSortedPosts(String slugOrId, Integer limit, Integer since, String sort, Boolean desc) {

        if (sort == null || sort.equals("flat")) {
            return this.flatSort(slugOrId, limit, since, desc);
        }

        if (sort.equals("parent_tree")) {
            return this.parentTreeSort(slugOrId, limit, since, sort, desc);
        }

        if (sort.equals("tree")) {
            return this.treeSort(slugOrId, limit, since, desc);
        }

        return this.flatSort(slugOrId, limit, since, desc);
    }


    private String getCarcassStringForSort(String slugOrId, ArrayList<Object> queryParams) {
        String sql =
                " SELECT u.nickname as author, p.created, f.slug as forum, p.id," +
                " p.is_edited as isEdited, p.message, p.parent, p.thread_id as thread" +
                " FROM posts p JOIN forums f on p.forum_id = f.id JOIN users u ON p.user_id = u.id" +
                " WHERE thread_id = ? ";
        Integer threadId = threadService.getIdBySlugOrId(slugOrId);
        queryParams.add(threadId);
        return sql;
    }

    private List<PostModel> flatSort(String slugOrId, Integer limit, Integer since, Boolean desc) {
        final ArrayList<Object> queryParams = new ArrayList<>();
        String sql = getCarcassStringForSort(slugOrId, queryParams);

        if (since != null) {
            if (desc != null && desc) {
                sql += " AND p.id < ? ";
            } else {
                sql += " AND p.id > ? ";
            }
            queryParams.add(since);
        }

        if (desc != null && desc) {
            sql += " ORDER BY p.id DESC";
        } else {
            //если desc не указан, то сортировка по убыванию
            sql += " ORDER BY p.id ASC";
        }

        if (limit != null) {
            sql += " LIMIT ?";
            queryParams.add(limit);
        }
        return jdbcTemplate.query(sql, ApiRowMapper.getPost, queryParams.toArray());
    }


    private List<PostModel> treeSort(String slugOrId, Integer limit, Integer since, Boolean desc) {
        final ArrayList<Object> queryParams = new ArrayList<>();
        String sql = getCarcassStringForSort(slugOrId, queryParams);

        if (since != null) {
            if (desc != null && desc) {
                sql += " AND p.path < (SELECT path FROM posts WHERE id = ?) ";
            } else {
                sql += " AND p.path > (SELECT path FROM posts WHERE id = ?) ";
            }
            queryParams.add(since);
        }

        if (desc != null && desc) {
            sql += " ORDER BY p.path DESC";
        } else {
            //если нет desc то у меня сортировка по убыванию
            sql += " ORDER BY p.path ASC";
        }

        if (limit != null) {
            sql += " LIMIT ?";
            queryParams.add(limit);
        }
        return jdbcTemplate.query(sql, ApiRowMapper.getPost, queryParams.toArray());
    }


    private String getRootsId(Integer threadId, Integer limit, Integer since, Boolean desc) {
        final ArrayList<Object> queryParams = new ArrayList<>();
        String sql = "SELECT id FROM posts WHERE thread_id = ? AND parent = 0 ";
        queryParams.add(threadId);

        if (since != null) {
            if (desc != null && desc) {
                sql += " AND id < (SELECT root_id FROM posts WHERE id = ?) ";
            } else {
                sql += " AND id > (SELECT root_id FROM posts WHERE id = ?) ";
            }
            queryParams.add(since);
        }

        if (desc != null && desc) {
            sql += " ORDER BY id DESC";
        } else {
            //если нет desc то у меня сортировка по убыванию
            sql += " ORDER BY id ASC";
        }

        if (limit != null) {
            sql += " LIMIT ? ";
            queryParams.add(limit);
        }

        final List<Integer> roots = jdbcTemplate.queryForList(sql, Integer.class, queryParams.toArray());
        StringBuilder sqlRoots = new StringBuilder("(");
        for (Integer root : roots) {
            sqlRoots.append(" ");
            sqlRoots.append(root.toString());
            sqlRoots.append(",");
        }

        if (sqlRoots.length() > 1) {
            sqlRoots.deleteCharAt(sqlRoots.length() - 1);
            sqlRoots.append(")");
        } else {
            return null;
        }

        return sqlRoots.toString();
    }


    private List<PostModel> parentTreeSort(String slugOrId, Integer limit, Integer since, String sort, Boolean desc) {
        Integer threadId = threadService.getIdBySlugOrId(slugOrId);

        String rootsSqlArray = this.getRootsId(threadId, limit, since, desc);

        if (rootsSqlArray == null) {
            return (List<PostModel>)EMPTY_LIST;
        }

        String sql =
            " SELECT p.created, p.id, p.message, p.parent, " +
            " u.nickname as author, p.is_edited as isEdited, p.thread_id as thread, f.slug as forum" +
            " FROM posts p JOIN users u ON p.user_id = u.id JOIN forums f on p.forum_id = f.id " +
            " WHERE p.root_id IN ";
        sql += rootsSqlArray;

        sql += " ORDER BY ";
        if (desc != null && desc) {
            sql += " p.root_id DESC, p.path";
        } else {
            sql += " p.root_id ASC, p.path";
        }

        return jdbcTemplate.query(sql, ApiRowMapper.getPost);
    }

}
