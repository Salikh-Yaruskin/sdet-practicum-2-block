package tests.apitests;

import base.CommentRepository;
import base.MainBase;
import dto.request.CreateComment;
import dto.request.UpdateStatusComment;
import helper.BaseRequests;
import helper.PropertyProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static dto.CommentStatus.APPROVE;
import static io.restassured.RestAssured.given;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class CommentCrudTest extends BasicTest {

    private CommentRepository commentRepository;

    private final Integer postPublishId = Integer.parseInt(PropertyProvider.getInstance()
            .getProperty("wb.post.publisher.id"));

    private final Integer holdCommentId = Integer.parseInt(PropertyProvider.getInstance()
            .getProperty("wb.commend.id"));

    @BeforeClass
    void init() {
        requestSpecification = BaseRequests.initRequestSpecification();
        MainBase base = new MainBase();
        commentRepository = new CommentRepository(base);
    }

    @Test
    void create_comment_positive() {
        var createRequest = new CreateComment(postPublishId, "QA Bot", "qa@example.com", "Looks good today!");

        Integer commentId = null;
        try {
            commentId = given()
                    .spec(requestSpecification)
                    .body(createRequest)
                    .when()
                    .post("/comments")
                    .then()
                    .statusCode(201)
                    .extract().path("id");

            var comment = commentRepository.getCommentById(commentId);

            assertNotNull(comment);
            assertEquals(comment.commendPostId(), postPublishId.longValue());
            assertEquals(comment.commentContent(), createRequest.content());
        } finally {
            commentRepository.deleteCommentById(commentId);
        }
    }

    @Test
    void update_status_comment_to_approve_positive() {
        var commentBeforeUpdate = commentRepository.getCommentById(holdCommentId);
        var statusBeforeUpdate = commentBeforeUpdate.commentApproved();

        var updateStatus = new UpdateStatusComment(APPROVE.name().toLowerCase());

        Integer commentId = null;
        try {
            commentId = given()
                    .spec(requestSpecification)
                    .body(updateStatus)
                    .when()
                    .post("/comments/{id}", holdCommentId)
                    .then()
                    .statusCode(200)
                    .extract().path("id");

            var commentAfterUpdate = commentRepository.getCommentById(commentId);

            assertEquals("1", commentAfterUpdate.commentApproved());
        } finally {
            commentRepository.updateStatusById(commentId, statusBeforeUpdate);
        }
    }
}
