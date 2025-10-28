package tests.apitests;

import base.MainBase;
import base.PostRepository;
import helper.BaseRequests;
import helper.PropertyProvider;
import helper.TestDataHelper;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static helper.MappingHelper.toLong;
import static io.restassured.RestAssured.given;
import static java.util.Objects.nonNull;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

@Epic("WordPress Platform")
@Feature("Получение сущностей")
@Slf4j
public class DataSelectTest extends BasicTest {

    private MainBase base;
    private TestDataHelper testDataHelper;

    private final Long authorId = Long.parseLong(PropertyProvider.getInstance().getProperty("wb.user.id"));

    @BeforeClass
    @Step("Инициализация репозиториев и спецификации RestAssured")
    void init() {
        requestSpecification = BaseRequests.initRequestSpecification();
        base = new MainBase();
        testDataHelper = new TestDataHelper(base);

        testDataHelper.createCategory("auto-it-cat", "auto-it-cat");
        testDataHelper.createdPostPublishedId = testDataHelper.createPost(authorId,
                "SDET JDBC Post", "content jdbc", "publish");
        testDataHelper.linkPostToCategory(testDataHelper.createdPostPublishedId, testDataHelper.createdTermTaxonomyId);
        testDataHelper.createdPostDraftId = testDataHelper.createPost(authorId,
                "SDET JDBC Draft", "draft content", "draft");
        testDataHelper.createdCommentId = testDataHelper.createComment(testDataHelper.createdPostPublishedId,
                "QA Bot", "qa@example.local", "Nice!", "0");

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        if (nonNull(testDataHelper)) {
            testDataHelper.cleanup();
        }
    }

    @Test(description = "Получение списка постов")
    @Story("Получение списка постов под авторизованным пользователем")
    @Description("Проверка успешного получение списка постов")
    void get_list_posts_positive() {
        List<Map<String, Object>> posts = given()
                .spec(requestSpecification)
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("$");

        assertNotNull(posts);
        assertFalse(posts.isEmpty());
    }

    @Test(description = "Получение поста по ID")
    @Story("Получение поста под авторизованным пользователем")
    @Description("Проверка успешного получение поста по id")
    void get_post_by_id_positive() {
        Long postId = testDataHelper.createdPostPublishedId;
        Map<String, Object> resp = given()
                .spec(requestSpecification)
                .when()
                .get("/posts/{id}", postId)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getMap("$");

        assertEquals(toLong(resp.get("id")), postId.longValue());
        assertEquals("publish", resp.get("status"));
    }

    @Test(description = "Получение несуществующего поста")
    @Story("Получение несуществующего поста под авторизованным пользователем")
    @Description("Проверка получение Not found при запросе несуществующего поста")
    void get_non_exists_post_by_id_positive() {
        int fakeId = 999999999;
        given()
                .spec(requestSpecification)
                .when()
                .get("/posts/{id}", fakeId)
                .then()
                .statusCode(404);
    }

    @Test(description = "Получение списка категорий")
    @Story("Получение списка категорий под авторизованным пользователем")
    @Description("Проверка успешного получение списка категорий")
    void get_list_categories_positive() {
        List<Map<String, Object>> cats = given()
                .spec(requestSpecification)
                .when()
                .get("/categories")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("$");

        assertNotNull(cats);
        assertFalse(cats.isEmpty());
    }

    @Test(description = "Получение категории по ID")
    @Story("Получение категории под авторизованным пользователем")
    @Description("Проверка успешного получение категории по ID")
    void get_category_by_id_positive() {
        Long termId = testDataHelper.createdTermId;
        Map<String, Object> resp = given()
                .spec(requestSpecification)
                .when()
                .get("/categories/{id}", termId)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getMap("$");

        assertEquals(toLong(resp.get("id")), termId.longValue());
        assertTrue(resp.get("slug").toString().startsWith("auto-it-cat"));
    }

    @Test(description = "Получение списка комментариев")
    @Story("Получение списка комментариев под авторизованным пользователем")
    @Description("Проверка успешного получение списка комментариев")
    void get_list_comments_positive() {
        List<Map<String, Object>> comments = given()
                .spec(requestSpecification)
                .when()
                .get("/comments")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("$");

        assertNotNull(comments);
        assertFalse(comments.isEmpty());
    }

    @Test(description = "Получение комментария по ID")
    @Story("Получение комментария под авторизованным пользователем")
    @Description("Проверка успешного получение комментария по ID")
    void get_comment_by_id_positive() {
        Long commentId = testDataHelper.createdCommentId;
        Map<String, Object> resp = given()
                .spec(requestSpecification)
                .when()
                .get("/comments/{id}", commentId)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getMap("$");

        assertEquals(toLong(resp.get("id")), commentId.longValue());
    }

    @Test(description = "Получение постов по категории")
    @Story("Получение списка постов под авторизованным пользователем")
    @Description("Проверка успешного получение постов по категории")
    void get_posts_by_category_positive() {
        Long termId = testDataHelper.createdTermId;
        List<Map<String, Object>> posts = given()
                .spec(requestSpecification)
                .when()
                .get("/posts?categories={cat}", termId)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("$");

        assertTrue(posts.stream().anyMatch(p -> {
            Object cats = ((Map<?, ?>) p).get("categories");
            if (cats instanceof List) {
                return ((List<?>) cats).contains(termId.intValue());
            }
            return false;
        }));
    }

    @Test(description = "Получение постов по автору")
    @Story("Получение списка постов под авторизованным пользователем")
    @Description("Проверка успешного получение постов по автору")
    void get_posts_by_author_positive() {
        Long userId = authorId;
        List<Map<String, Object>> posts = given()
                .spec(requestSpecification)
                .when()
                .get("/posts?author={author}", userId)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("$");

        assertNotNull(posts);
        assertTrue(posts.stream().anyMatch(p ->
                (toLong((p).get("author"))) == userId));

    }

    @Test(description = "Проверка пагинации списка постов")
    @Story("Получение списка постов под авторизованным пользователем")
    @Description("Проверка успешного получение списка постов с пагинацией")
    void get_posts_with_pagination_positive() {
        var response = given()
                .spec(requestSpecification)
                .when()
                .get("/posts?per_page=5&page=1")
                .then()
                .statusCode(200)
                .extract();

        String total = response.header("X-WP-Total");
        String totalPages = response.header("X-WP-TotalPages");

        assertNotNull(total);
        assertNotNull(totalPages);
    }

    @Test(description = "Проверка фильтрации по статусу")
    @Story("Получение списка постов под авторизованным пользователем")
    @Description("Проверка успешного получение списка постов с фильтрацией по статусам")
    void get_posts_with_filter_by_status() {
        List<Map<String, Object>> drafts = given()
                .spec(requestSpecification)
                .when()
                .get("/posts?status=draft")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("$");

        assertTrue(drafts.stream().anyMatch(p ->
                "draft".equals((p).get("status"))));
    }

    @Test(description = "Получение списка постов под неавторизованным юзером не должно работать")
    @Story("Получение списка постов под неавторизованным пользователем")
    @Description("Проверка провальной попытки получение списка постов под неавторизованным пользователем")
    void get_posts_with_non_auth_user_negative() {
        given()
                .auth().none()
                .when()
                .get("/posts")
                .then()
                .statusCode(404);
    }
}
