package project.rowmapper;

import project.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class ApiRowMapper {

    public static RowMapper<ForumModel> getForum = (rs, rowNum) -> {
        return new ForumModel(
                rs.getInt("posts"),
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
        //todo редачить
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return new ThreadModel(
                rs.getString("author"),
                dateFormat.format(timestamp.getTime()),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getString("message"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("votes"));
    };


    public static RowMapper<PostModel> getPost = (rs, rowNum) -> {
        //todo отредачить
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return new PostModel(
                rs.getString("author"),
                dateFormat.format(timestamp.getTime()),
                rs.getString("forum"),
                rs.getInt("id"),
                rs.getBoolean("isEdited"),
                rs.getString("message"),
                rs.getInt("parent"),
                rs.getInt("thread")
        );
    };


}
