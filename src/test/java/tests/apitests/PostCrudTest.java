package tests.apitests;

import base.MainBase;
import base.PostRepository;
import dto.PostStatus;
import dto.request.CreatePost;
import dto.request.UpdatePost;
import dto.request.UpdateStatusPost;
import helper.BaseRequests;
import helper.PropertyProvider;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

@Epic("WordPress Platform")
@Feature("Posts Management")
@Slf4j
public class PostCrudTest extends BasicTest {

    private PostRepository postRepository;

    private final int entityPostId = Integer.parseInt(PropertyProvider.getInstance().getProperty("wb.post.id"));

    @BeforeClass
    @Step("Инициализация репозиториев и спецификации RestAssured")
    void init() {
        requestSpecification = BaseRequests.initRequestSpecification();
        MainBase base = new MainBase();
        postRepository = new PostRepository(base);
    }

    @Test(description = "Создание поста")
    @Story("Создание нового поста")
    @Description("Проверяет успешное создание поста через API и наличие его в базе данных")
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

    @Test(description = "Обновление содержимого поста")
    @Story("Редактирование поста")
    @Description("Проверяет возможность обновления содержимого поста через API")
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

    @Test(description = "Обновление статуса поста на 'publish'")
    @Story("Изменение статуса поста")
    @Description("Проверяет перевод поста в статус publish и возврат его в исходное состояние")
    void update_status_post_to_publisher_positive() {
        Integer newPostId = createNewPost();
        var postBefore = postRepository.getPostById(newPostId);
        var beforeStatus = postBefore.postStatus();

        var updateStatusPost = new UpdateStatusPost(PostStatus.PUBLISH.toString().toLowerCase());
        Integer postId = null;
        try {
            postId = given()
                    .spec(requestSpecification)
                    .body(updateStatusPost)
                    .when()
                    .post("/posts/" + newPostId)
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

    @Test(description = "Удаление поста в корзину (trash)")
    @Story("Мягкое удаление поста")
    @Description("Проверяет, что при удалении поста без force он переводится в статус 'trash'")
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

    @Test(description = "Полное удаление поста")
    @Story("Удаление поста без восстановления")
    @Description("Проверяет успешное полное удаление поста через API и отсутствие его в базе")
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

    private Integer createNewPost() {
        return given()
                .spec(requestSpecification)
                .body(new CreatePost("Temp Post", "Content", "draft"))
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .extract().path("id");
    }
}
