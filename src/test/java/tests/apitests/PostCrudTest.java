package tests.apitests;

import base.MainBase;
import base.PostRepository;
import dto.PostStatus;
import dto.request.CreatePost;
import dto.request.UpdatePost;
import dto.request.UpdateStatusPost;
import helper.BaseRequests;
import helper.PropertyProvider;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

@Slf4j
public class PostCrudTest extends BasicTest {

    private PostRepository postRepository;

    private final int entityPostId = Integer.parseInt(PropertyProvider.getInstance().getProperty("wb.post.id"));

    @BeforeClass
    void init() {
        requestSpecification = BaseRequests.initRequestSpecification();
        MainBase base = new MainBase();
        postRepository = new PostRepository(base);
    }

    @Test
    void create_post_positive() {
        var createRequest = new CreatePost("SDET Post", "Hello new Post", "draft");
        Integer postId = given()
                .spec(requestSpecification)
                .body(createRequest)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .extract().path("id");
        var post = postRepository.getPostById(postId);

        assertNotNull(post);
        assertEquals(post.postTitle(), createRequest.title());
        assertEquals(post.postStatus(), createRequest.status());
        assertEquals(post.postContent(), createRequest.content());
    }

    @Test
    void update_post_positive() {
        var updateRequest = new UpdatePost("Update Post now");
        Integer postId = given()
                .spec(requestSpecification)
                .body(updateRequest)
                .when()
                .post("/posts/" + entityPostId)
                .then()
                .statusCode(200)
                .extract().path("id");

        var post = postRepository.getPostById(postId);

        assertEquals(post.postContent(), updateRequest.content());
    }

    @Test
    void update_status_post_to_publisher_positive() {
        var postBefore = postRepository.getPostById(entityPostId);
        var beforeStatus = postBefore.postStatus();

        var updateStatusPost = new UpdateStatusPost(PostStatus.PUBLISH.toString().toLowerCase());
        Integer postId = null;
        try {
            postId = given()
                    .spec(requestSpecification)
                    .body(updateStatusPost)
                    .when()
                    .post("/posts/" + entityPostId)
                    .then()
                    .statusCode(200)
                    .extract().path("id");

            var post = postRepository.getPostById(postId);
            assertEquals(post.postStatus(), updateStatusPost.status());
        } finally {
            given()
                    .spec(requestSpecification)
                    .body(new UpdateStatusPost(beforeStatus))
                    .when()
                    .post("/posts/" + entityPostId)
                    .then()
                    .statusCode(200)
                    .extract().path("id");
        }
    }

    @Test
    void delete_post_to_trash_positive() {
        var postBefore = postRepository.getPostById(entityPostId);
        var beforeStatus = postBefore.postStatus();

        try {
            given()
                    .spec(requestSpecification)
                    .when()
                    .delete("/posts/{id}?force=false", entityPostId)
                    .then()
                    .statusCode(200);

            var post = postRepository.getPostById(entityPostId);

            assertEquals("trash", post.postStatus());
        } finally {
            given()
                    .spec(requestSpecification)
                    .body(new UpdateStatusPost(beforeStatus))
                    .when()
                    .post("/posts/" + entityPostId)
                    .then()
                    .statusCode(200)
                    .extract().path("id");
        }
    }

    @Test
    void delete_post_positive() {
        var createRequest = new CreatePost("Post for delete", "Very bad post", "draft");
        Integer postIdForDelete = given()
                .spec(requestSpecification)
                .body(createRequest)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .extract().path("id");
        var postBeforeDelete = postRepository.getPostById(postIdForDelete);

        given()
                .spec(requestSpecification)
                .when()
                .delete("/posts/{id}?force=true", postIdForDelete)
                .then()
                .statusCode(200);

        var postAfterDelete = postRepository.getPostById(postIdForDelete);

        assertNotNull(postBeforeDelete);
        assertNull(postAfterDelete);
    }
}
