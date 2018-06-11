package project.services;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.models.PostFullModel;
import project.models.PostModel;
import project.models.ThreadModel;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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



    public Integer getParent(Integer threadID, Integer parentID) {
        String sql = "SELECT id FROM posts WHERE thread_id = ? AND id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, threadID, parentID);
    }

    private Array getPathById(Integer id) {
        String sql = "SELECT path FROM posts WHERE id = ?";
        return  jdbcTemplate.queryForObject(sql, Array.class, id);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<PostModel> create(List<PostModel> posts, String slug_or_id) {
        final String sql =
            "INSERT INTO posts (id, user_id, created, forum_id, message," +
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

            final Integer generatedID =
                jdbcTemplate.queryForObject("SELECT nextval(pg_get_serial_sequence('posts', 'id'))", Integer.class);
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


    public void create2(List<PostModel> posts) {
        final String sql =
            "INSERT INTO posts (id, user_id, created, forum_id, message, parent, thread_id, path, root_id)" +
            " VALUES(?, ?, ?::TIMESTAMPTZ, ?, ?, ?, ?, array_append(?, ?::INTEGER), ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement pst, int i) throws SQLException {
                final Integer postId = getNextPostId();
                posts.get(i).setId(postId);

                pst.setInt(1, posts.get(i).getId());
                pst.setInt(2, posts.get(i).getAuthorId());
                pst.setString(3, posts.get(i).getCreated());
                pst.setInt(4, posts.get(i).getForumId());
                pst.setString(5, posts.get(i).getMessage());
                pst.setInt(6, posts.get(i).getParent());
                pst.setInt(7, posts.get(i).getThread());

                Integer idOfParent = posts.get(i).getParent();
                final Array path = idOfParent == 0 ? null : jdbcTemplate.queryForObject("SELECT path FROM posts WHERE id = ?", Array.class, idOfParent);
                Integer rootId;
                try {
                    rootId = idOfParent == 0 ? postId : ((Integer[]) path.getArray())[0];
                } catch (SQLException e) {
                    rootId = postId;
                }
                pst.setArray(8, path);
                pst.setInt(9, postId);
                pst.setInt(10, rootId);
            }

            @Override
            public int getBatchSize() {
                return posts.size();
            }
        });

        forumService.increasePostsAmount(posts.get(0).getForumId(), posts.size());
    }


    private String getPostMessage(Integer id) {
        final String sql = "SELECT message FROM posts WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }


    private PostModel getPost(Integer id) {
        final String sql =
            "SELECT (SELECT nickname from users WHERE id = p.user_id) as author, " +
            " (SELECT slug FROM Forums WHERE id = p.forum_id) as forum, " +
            " created, id, thread_id as thread, message, parent, is_edited as isEdited" +
            " FROM posts p where id = ?";
        return jdbcTemplate.queryForObject(sql, PostModel::getPost, id);
    }


    public PostModel updatePost(PostModel post, Integer id) {
        final String oldMessage = this.getPostMessage(id);
        final String sql = "UPDATE posts SET is_edited = TRUE, message = ? WHERE id = ?";

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
        "SELECT " +
        "(SELECT slug FROM forums WHERE id = p.forum_id) as forum, " +
        "(SELECT nickname from users WHERE id = p.user_id) as author," +
        " created, id, message, parent, is_edited as isEdited, " +
        " thread_id as thread " +
        " FROM posts p WHERE id = ?";

        final PostModel post = jdbcTemplate.queryForObject(sql, PostModel::getPost, id);
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
            " SELECT u.nickname as author, p.created, p.parent, f.slug as forum, p.id," +
            " p.is_edited as isEdited, p.message, p.thread_id as thread" +
            " FROM posts p JOIN forums f ON p.forum_id = f.id JOIN users u ON p.user_id = u.id" +
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
        return jdbcTemplate.query(sql, PostModel::getPost, queryParams.toArray());
    }

    public Integer getNextPostId() {
        final String sqlGetNext = "SELECT nextval(pg_get_serial_sequence('posts', 'id'))";
        return jdbcTemplate.queryForObject(sqlGetNext, Integer.class);
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
        return jdbcTemplate.query(sql, PostModel::getPost, queryParams.toArray());
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
            " u.nickname AS author, p.is_edited AS isEdited, p.thread_id AS thread, f.slug AS forum" +
            " FROM posts p JOIN users u ON p.user_id = u.id JOIN forums f ON p.forum_id = f.id " +
            " WHERE p.root_id IN ";
        sql += rootsSqlArray;

        sql += " ORDER BY ";
        if (desc != null && desc) {
            sql += " p.root_id DESC, p.path";
        } else {
            sql += " p.root_id ASC, p.path";
        }
        return jdbcTemplate.query(sql, PostModel::getPost);
    }

}
