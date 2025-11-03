package tests.apitests;

import domain.api.Directories;
import domain.api.Item;
import helper.YandexApiHelper;
import helper.YandexApiRequests;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.UUID;

import static helper.PropertyProvider.getInstance;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

@Feature("Yandex Disk API — Управление директориями")
public class YandexDiskDirectoryTest extends BasicTest {

    @BeforeClass
    @Step("Инициализация RestAssured спецификации")
    void init() {
        requestSpecification = YandexApiRequests.initRequestSpecification();
    }

    @Test
    @Story("Создание директорий")
    @Description("Проверяет, что при валидном токене " +
            "вызов PUT /v1/disk/resources?path=disk:/demo/folderA создаёт новую директорию")
    void givenValidToken_whenCreateDirectory_thenReturn200() {
        var directoryName = getInstance().getProperty("yandex.api.directory") + UUID.randomUUID();
        try {
            given().spec(requestSpecification)
                    .when().put("/v1/disk/resources?path=" + directoryName)
                    .then().statusCode(201);

            var directories = YandexApiHelper.getDirectories();
            boolean isExists = isExistsByPath(directories, directoryName);

            assertTrue(isExists);
        } finally {
            YandexApiHelper.deleteDirectoryPermanently(directoryName);
        }
    }

    @Test
    @Story("Создание директорий")
    @Description("Проверяет, что при попытке создать уже существующую директорию API возвращает 409 Conflict, " +
            "а дубликат не создаётся")
    void givenValidToken_whenCreateSameDirectory_whenReturn409() {
        var directoryName = getInstance().getProperty("yandex.api.directory") + UUID.randomUUID();

        try {
            // Предусловия: создание директории
            createDirectory(directoryName);

            // создание дубликата
            given().spec(requestSpecification)
                    .when().put("/v1/disk/resources?path=" + directoryName)
                    .then().statusCode(409);
        } finally {
            YandexApiHelper.deleteDirectoryPermanently(directoryName);
        }
    }

    @Test
    @Story("Авторизация")
    @Description("Проверяет, что при отсутствии токена " +
            "вызов PUT /v1/disk/resources возвращает 401 Unauthorized.")
    void givenNoToken_whenCreateDirectory_thenReturn401() {
        var directoryName = getInstance().getProperty("yandex.api.directory") + UUID.randomUUID();
        given()
                .when().put("https://cloud-api.yandex.net/v1/disk/resources?path=" + directoryName)
                .then().statusCode(401);

        var directories = YandexApiHelper.getDirectories();
        boolean isExists = isExistsByPath(directories, directoryName);

        assertFalse(isExists);
    }

    @Test
    @Story("Удаление директорий")
    @Description("Проверяет, что при удалении без параметра permanently=true " +
            "папка перемещается в корзину")
    void givenValidTokenExistsDirectory_whenDeleteDirectoryInTrash_thenReturn204() {
        var uuid = UUID.randomUUID().toString();
        var directoryPath = getInstance().getProperty("yandex.api.directory") + uuid;
        var directoryName = getInstance().getProperty("yandex.api.directory.name") + uuid;

        try {
            // Предусловия: создание директории
            createDirectory(directoryPath);

            var beforeDeleteDirectories = YandexApiHelper.getDirectories();
            int countDirectoryBefore = beforeDeleteDirectories.getEmbedded().getItems().size();

            given().spec(YandexApiRequests.initRequestSpecification())
                    .when().delete("/v1/disk/resources?path=" + directoryPath)
                    .then().statusCode(204);

            var afterDeleteDirectories = YandexApiHelper.getDirectories();
            int countDirectoryAfter = afterDeleteDirectories.getEmbedded().getItems().size();

            var trashDirectories = YandexApiHelper.getTrashDirectories();
            boolean isExistsByName = isExistsByName(trashDirectories, directoryName);

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(countDirectoryAfter, countDirectoryBefore - 1);
            softAssert.assertTrue(isExistsByName);
        } finally {
            YandexApiHelper.deleteDirectoryInTrash(trashDirectoryPath(directoryName));
        }
    }

    @Test
    @Story("Удаление директорий")
    @Description("Проверяет, что при удалении с параметром permanently=true " +
            "ресурс удаляется без помещения в корзину и возвращается код 204 No Content.")
    void givenValidToken_whenDeleteDirectoryPermanently_thenReturn204() {
        var uuid = UUID.randomUUID().toString();
        var directoryPath = getInstance().getProperty("yandex.api.directory") + uuid;
        var directoryName = getInstance().getProperty("yandex.api.directory.name") + uuid;

        // Предусловия: создание директории
        createDirectory(directoryPath);

        given().spec(YandexApiRequests.initRequestSpecification())
                .when().delete("/v1/disk/resources?path=" + directoryPath + "&permanently=true")
                .then().statusCode(anyOf(is(202), is(204)));

        boolean isExists = isExistsInTrash(directoryName);

        assertFalse(isExists);
    }

    @Test
    @Story("Удаление папок")
    @Description("Проверяет, что при попытке удалить несуществующий ресурс " +
            "возвращается ошибка 404 Not Found")
    void givenValidTokenNonexistentFolder_whenDelete_thenReturn404() {
        var directoryNoExistsPath = getInstance().getProperty("yandex.api.directory.no-exists") + UUID.randomUUID();

        given().spec(YandexApiRequests.initRequestSpecification())
                .when().delete("/v1/disk/resources?path=" + directoryNoExistsPath)
                .then().statusCode(404);
    }

    @Test
    @Story("Восстановление папок")
    @Description("Проверяет, что вызов PUT /v1/disk/trash/resources/restore?path=trash:/folderA " +
            "успешно восстанавливает удалённую директорию и удаляет её из корзины")
    void givenValidToken_whenRestore_thenReturn201() {
        var uuid = UUID.randomUUID().toString();
        var directoryPath = getInstance().getProperty("yandex.api.directory") + uuid;
        var directoryName = getInstance().getProperty("yandex.api.directory.name") + uuid;

        try {

            // Предусловие: создание директории и удалению ее в корзину
            createDirectory(directoryPath);
            YandexApiHelper.deleteDirectoryToTrash(directoryPath);
            var trashPathDirectory = trashDirectoryPath(directoryName);

            // восстановление из корзины
            given().spec(YandexApiRequests.initRequestSpecification())
                    .when().put("/v1/disk/trash/resources/restore?path=" + trashPathDirectory)
                    .then().statusCode(201);

            boolean isExists = isExistsInTrash(directoryName);

            assertFalse(isExists);
        } finally {
            YandexApiHelper.deleteDirectoryPermanently(directoryName);
        }
    }

    private boolean isExistsByPath(Directories directories, String directoryName) {
        return directories.getEmbedded().getItems().stream()
                .map(Item::getPath)
                .anyMatch(path -> path.equals(directoryName));
    }

    private boolean isExistsByName(Directories directories, String directoryName) {
        return directories.getEmbedded().getItems().stream()
                .map(Item::getName)
                .anyMatch(name -> name.equals(directoryName));
    }

    private void createDirectory(String path) {
        given().spec(requestSpecification)
                .when().put("/v1/disk/resources?path=" + path)
                .then().statusCode(201);
    }

    private String trashDirectoryPath(String directoryName) {
        var pathDirectory = "trash:/" + directoryName;
        var trashDirectories = YandexApiHelper.getTrashDirectories();
        return trashDirectories.getEmbedded().getItems().stream()
                .map(Item::getPath)
                .filter(path -> path.startsWith(pathDirectory))
                .findFirst().orElseThrow(() -> new RuntimeException("No exists directory in trash!"));
    }

    private boolean isExistsInTrash(String directoryName) {
        var pathDirectory = "trash:/" + directoryName;
        var trashDirectories = YandexApiHelper.getTrashDirectories();
        return trashDirectories.getEmbedded().getItems().stream()
                .map(Item::getPath)
                .anyMatch(path -> path.startsWith(pathDirectory));
    }
}
