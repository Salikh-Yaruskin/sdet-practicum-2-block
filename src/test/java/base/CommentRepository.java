package base;

import dto.CommentRow;

import java.util.Map;

import static helper.MappingHelper.toLong;

public class CommentRepository {

    private MainBase base;

    public CommentRepository(MainBase base) {
        this.base = base;
    }

    public CommentRow getCommentById(int id) {
        String sql = "select comment_ID, comment_post_ID, comment_author, comment_content, comment_approved" +
                " from wp_comments where comment_ID = ?";
        Map<String, Object> row = base.row(sql, id);

        if (row.isEmpty()) return null;

        return new CommentRow(
                toLong(row.get("comment_ID")),
                toLong(row.get("comment_post_ID")),
                (String) row.get("comment_author"),
                (String) row.get("comment_content"),
                (String) row.get("comment_approved")
        );
    }

    public void deleteCommentById(int id) {
        String sql = "delete from wp_comments where comment_ID = ?";
        base.update(sql, id);
    }

    public void updateStatusById(int id, String status) {
        String sql = "update wp_comments set comment_approved = ? where comment_ID = ?";
        base.update(sql, status, id);
    }
}
