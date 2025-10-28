package dto;

public record PostRow(long id,
                      long postAuthor,
                      String postContent,
                      String postTitle,
                      String postStatus,
                      String commentStatus,
                      String postName,
                      long commentCount) {
}
