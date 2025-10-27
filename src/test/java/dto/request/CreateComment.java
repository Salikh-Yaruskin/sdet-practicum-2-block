package dto.request;

public record CreateComment (Integer post,
                             String author_name,
                             String author_email,
                             String content) {
}
