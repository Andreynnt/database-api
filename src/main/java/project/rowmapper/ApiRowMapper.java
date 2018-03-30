package project.rowmapper;

import project.models.ForumModel;
import org.springframework.jdbc.core.RowMapper;
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

}
