package tests.apitests;

import base.CategoryRepository;
import base.MainBase;
import base.PostCategoryMemberRepository;
import dto.request.CreateCategory;
import dto.request.LinkCategoryRequest;
import dto.request.UpdateCategory;
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
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

@Epic("WordPress Platform")
@Feature("Categories Manager")
@Slf4j
public class CategoryCrudTest extends BasicTest {

    private CategoryRepository categoryRepository;

    private PostCategoryMemberRepository postCategoryMemberRepository;

    private final Integer categoryEntityId = Integer.parseInt(PropertyProvider.getInstance()
            .getProperty("wb.category.id"));

    private final Integer postPublishId = Integer.parseInt(PropertyProvider.getInstance()
            .getProperty("wb.post.publisher.id"));

    @BeforeClass
    @Step("Инициализация репозиториев и RestAssured спецификации")
    void init() {
        requestSpecification = BaseRequests.initRequestSpecification();
        MainBase base = new MainBase();
        categoryRepository = new CategoryRepository(base);
        postCategoryMemberRepository = new PostCategoryMemberRepository(base);
    }

    @Test(description = "Создание категории")
    @Story("Создание новой категории")
    @Description("Проверяет успешное создание категории и её сохранение в БД")
    void create_category_positive() {
        var createRequest = new CreateCategory("QA Category", "qa-category");

        Integer categoryId = null;
        try {
            categoryId = given()
                    .spec(requestSpecification)
                    .body(createRequest)
                    .when()
                    .post("/categories")
                    .then()
                    .statusCode(201)
                    .extract().path("id");
            var category = categoryRepository.getCategoryById(categoryId);

            assertNotNull(category);
            assertEquals(category.name(), createRequest.name());
        } finally {
            categoryRepository.deleteCategoryById(categoryId);
        }
    }

    @Test(description = "Переименование категории")
    @Story("Редактирование категории")
    @Description("Проверяет возможность переименования категории")
    void rename_category_positive() {
        var categoryBeforeUpdate = categoryRepository.getCategoryById(categoryEntityId);
        var beforeName = categoryBeforeUpdate.name();

        var renameCategory = new UpdateCategory("QA and Testing");

        Integer categoryId = null;
        try {
            categoryId = given()
                    .spec(requestSpecification)
                    .body(renameCategory)
                    .when()
                    .post("/categories/{id}", categoryEntityId)
                    .then()
                    .statusCode(200)
                    .extract().path("id");

            var categoryAfterUpdate = categoryRepository.getCategoryById(categoryId);

            assertEquals(categoryId, categoryEntityId);
            assertNotEquals(categoryAfterUpdate.name(), categoryBeforeUpdate.name());
            assertEquals(categoryAfterUpdate.name(), renameCategory.name());
        } finally {
            categoryRepository.updateCategoryNameById(categoryEntityId, beforeName);
        }
    }

    @Test(description = "Привязка категории к посту")
    @Story("Привязка категории к посту")
    @Description("Проверяет успешное связывание категории и поста")
    void link_category_to_post_positive() {
        var linkCategoryRequest = new LinkCategoryRequest(String.valueOf(categoryEntityId));
        Integer postId = given()
                .spec(requestSpecification)
                .body(linkCategoryRequest)
                .when()
                .post("/posts/{id}", postPublishId)
                .then()
                .statusCode(200)
                .extract().path("id");

        var member = postCategoryMemberRepository.getMember(categoryEntityId);

        assertEquals(member.postId(), postId.longValue());
    }

    @Test(description = "Удаление категории")
    @Story("Удаление категории")
    @Description("Проверяет возможность полного удаления категории из БД")
    void delete_category_positive() {
        var createRequest = new CreateCategory("Category for Delete", "bad category");

        Integer categoryId = given()
                .spec(requestSpecification)
                .body(createRequest)
                .when()
                .post("/categories")
                .then()
                .statusCode(201)
                .extract().path("id");
        var categoryBeforeDelete = categoryRepository.getCategoryById(categoryId);

        given()
                .spec(requestSpecification)
                .when()
                .delete("/categories/{catId}?force=true", categoryId)
                .then()
                .statusCode(200);

        var categoryAfterDelete = categoryRepository.getCategoryById(categoryId);

        assertNotNull(categoryBeforeDelete);
        assertNull(categoryAfterDelete);
    }
}
