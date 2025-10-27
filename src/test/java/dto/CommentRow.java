package dto;

public record CommentRow(long commendId,
                         long commendPostId,
                         String commentAuthor,
                         String commentContent,
                         String commentApproved) {
}
