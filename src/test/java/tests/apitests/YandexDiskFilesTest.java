package tests.apitests;

import helper.YandexApiRequests;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

@Feature("Yandex Disk API — Управление файлами")
public class YandexDiskFilesTest extends BasicTest {

    @BeforeClass
    @Step("Инициализация RestAssured спецификации")
    void init() {
        requestSpecification = YandexApiRequests.initRequestSpecification();
    }

    @Test
    @Story("Получение файлов")
    @Description("Проверяет, что при валидном токене " +
            "вызов GET /v1/disk/resources/files отдает нам все файлы и они соответствуют схеме")
    void givenValidToken_whenGetAllFiles_thenReturn200AndValidJsonSchema() {
        given().spec(YandexApiRequests.initRequestSpecification())
                .when().get("/v1/disk/resources/files")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/yadisk_files_schema_strict.json"))
                .body("items", not(empty()));
    }
}
