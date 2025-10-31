package tests.apitests;

import helper.YandexApiHelper;
import helper.YandexApiRequests;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static helper.FileLoaderHelper.loadFile;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.urlEncodingEnabled;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;

@Epic("Yandex Disk API")
public class YandexDiscTest extends BasicTest {

    @BeforeClass
    @Step("Инициализация RestAssured спецификации")
    void init() {
        requestSpecification = YandexApiRequests.initRequestSpecification();
    }

    @Test(description = "Авторизация с валидным токеном")
    @Feature("Авторизация")
    @Story("GET /v1/disk/ с валидным токеном")
    @Description("Отправляем GET, ожидаем код 200 и нужные поля")
    void should_by_auth_with_valid_token() {
        given()
                .spec(requestSpecification)
                .when().get("/v1/disk/")
                .then().statusCode(200)
                .body(
                        "user.login", not(emptyOrNullString()),
                        "user.display_name", not(emptyOrNullString())
                );
    }

    @Test(description = "ТК2: Авторизация без токена")
    @Feature("Авторизация")
    @Story("GET /v1/disk/ без токена")
    @Description("Отправляем GET без токена, ожидаем код 401")
    void should_by_error_auth_without_token() {
        given()
                .when().get("https://cloud-api.yandex.net/v1/disk/")
                .then().statusCode(401)
                .body(
                        "error", not(emptyOrNullString()),
                        "description", not(emptyOrNullString()),
                        "message", not(emptyOrNullString())
                );
    }

    @Test(description = "Загрузка и копирование файла")
    @Feature("Файлы: загрузка и копирование")
    @Story("PUT upload → POST copy")
    @Description("Создаём папки input_data и output_data, загружаем data.txt по href, " +
            "копируем в output_data (ожидаем 201), затем повторяем копирование (ожидаем 409)")
    void should_be_upload_file() {
        // предусловие: создание директорий
        YandexApiHelper.createFolder("/input_data");
        YandexApiHelper.createFolder("/output_data");

        // загрузка файла в input_data
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("files/data.txt"));
        File file = loadFile(url);

        String href = YandexApiHelper.getUploadHref("/input_data/data.txt");
        YandexApiHelper.putFileToHref(href, file);

        // копирование файла из input_data в output_data
        var copy = YandexApiHelper.copy("/input_data/data.txt", "/output_data/data.txt");
        copy.then().statusCode(201)
                .body(
                        "method", equalTo("GET"),
                        "href", not(emptyOrNullString()),
                        "templated", equalTo(false)
                );

        // попытка снова скопировать файла из input_data в output_data
        given().spec(YandexApiRequests.initRequestSpecification())
                .queryParam("from", "input_data/data.txt")
                .queryParam("path", "output_data/data.txt")
                .when().post("/v1/disk/resources/copy")
                .then().statusCode(409)
                .body(
                        "error", not(emptyOrNullString()),
                        "description", not(emptyOrNullString()),
                        "message", not(emptyOrNullString())
                );

        // постусловие: удаление директорий
        YandexApiHelper.deleteFolder("/input_data");
        YandexApiHelper.deleteFolder("/output_data");
    }

    @Test(description = "Скачивание файла и сравнение с оригиналом")
    @Feature("Файлы: скачивание")
    @Story("GET /v1/disk/resources/download → GET <href>")
    @Description("Получаем download href для sdet_data/data.txt и скачиваем по прямой ссылке." +
            "Сравниваем содержимое с файлом из ресурсов")
    void should_download_file_equal_to_original() {
        // предусловие: создание директорий
        YandexApiHelper.createFolder("/sdet_data");

        // загрузка файла в sdet_data
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("files/data.txt"));
        File file = loadFile(url);

        String href = YandexApiHelper.getUploadHref("/sdet_data/data.txt");
        YandexApiHelper.putFileToHref(href, file);

        // получение ссылки для скачивания
        String downloadHref = given().spec(YandexApiRequests.initRequestSpecification())
                .queryParam("path", "/sdet_data/data.txt")
                .when().get("/v1/disk/resources/download")
                .then().statusCode(200)
                .body("href", not(emptyOrNullString()))
                .extract().jsonPath().getString("href");

        // получение файла по раннее полученной ссылки
        byte[] bytes;
        boolean oldUrlEncoding = urlEncodingEnabled;
        try {
            urlEncodingEnabled = false;
            bytes = given()
                    .when().get(URI.create(downloadHref))
                    .then().statusCode(anyOf(is(200), is(206)))
                    .extract().asByteArray();
        } finally {
            urlEncodingEnabled = oldUrlEncoding;
        }

        // сравниваем содержимое
        String actual = new String(bytes, StandardCharsets.UTF_8);
        String expected = expectFile(url);
        assertEquals(actual, expected, "Содержимое файла не совпало");

        // постусловие: удаление директорий
        YandexApiHelper.deleteFolder("/sdet_data");
    }

    private String expectFile(URL url) {
        try {
            return Files.readString(Path.of(url.toURI()), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Error for IO operation");
        }
    }
}
