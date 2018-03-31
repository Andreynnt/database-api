package project.rowmapper;

import project.models.ForumModel;
import org.springframework.jdbc.core.RowMapper;
import project.models.ThreadModel;
import project.models.UserModel;

public class ApiRowMapper {

    public static RowMapper<ForumModel> getForum = (rs, rowNum) -> {
        return new ForumModel(
                rs.getLong("posts"),
                rs.getString("slug"),
                rs.getInt("threads"),
                rs.getString("title"),
                rs.getString("nickname"));
    };

    public static RowMapper<UserModel> getUser = (rs, rowNum) -> {
        return new UserModel(
                rs.getString("about"),
                rs.getString("email"),
                rs.getString("fullname"),
                rs.getString("nickname"));
    };

    public static RowMapper<ThreadModel> getThread = (rs, rowNum) -> {
        return new ThreadModel(
                rs.getString("author"),
                rs.getString("created"),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("votes"));
    };

}
