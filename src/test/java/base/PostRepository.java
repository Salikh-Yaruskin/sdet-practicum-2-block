package base;

import dto.PostRow;

import java.util.Map;

import static helper.MappingHelper.toLong;

public class PostRepository {

    private final MainBase base;

    public PostRepository(MainBase base) {
        this.base = base;
    }

    public PostRow getPostById(int id) {
        String sql = "select ID, post_author, post_content, post_title, post_status, " +
                "comment_status, post_name, comment_count from wp_posts where ID=?";
        Map<String, Object> row = base.row(sql, id);
        if (row.isEmpty()) return null;

        return new PostRow(
                toLong(row.get("ID")),
                toLong(row.get("post_author")),
                (String) row.get("post_content"),
                (String) row.get("post_title"),
                (String) row.get("post_status"),
                (String) row.get("comment_status"),
                (String) row.get("post_name"),
                toLong(row.get("comment_count"))
        );
    }
}
