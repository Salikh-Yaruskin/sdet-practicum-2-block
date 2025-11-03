package helper;

import domain.api.Directories;
import io.restassured.response.Response;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import static helper.FileLoaderHelper.loadFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class YandexApiHelper {

    public static void createFolder(String path) {
        given().spec(YandexApiRequests.initRequestSpecification())
                .queryParam("path", path)
                .when().put("/v1/disk/resources")
                .then().statusCode(anyOf(is(201), is(409)));
    }

    public static String getUploadHref(String path) {
        return given().spec(YandexApiRequests.initRequestSpecification())
                .queryParam("path", path)
                .when().get("/v1/disk/resources/upload")
                .then().statusCode(200)
                .extract().jsonPath().getString("href");
    }

    public static void putFileToHref(String href, File file) {
        given().spec(YandexApiRequests.initRequestSpecification())
                .body(file)
                .when().put(href)
                .then().statusCode(201);
    }

    public static Response copy(String from, String path) {
        return given().spec(YandexApiRequests.initRequestSpecification())
                .queryParam("from", from)
                .queryParam("path", path)
                .when().post("/v1/disk/resources/copy");
    }

    public static String getDownloadLink(String path) {
        return given().spec(YandexApiRequests.initRequestSpecification())
                .queryParam("path", path)
                .when().get("/v1/disk/resources/upload")
                .then().statusCode(200)
                .extract().jsonPath().getString("href");
    }

    public static void deleteFolder(String path) {
        given().spec(YandexApiRequests.initRequestSpecification())
                .queryParam("path", path)
                .when().delete("delete/v1/disk/resources")
                .then().statusCode(202);
    }

    public static void loadFileToDirectory(String pathFile, String pathDirectory) {
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource(pathFile));
        File file = loadFile(url);

        String href = YandexApiHelper.getUploadHref(pathDirectory);
        YandexApiHelper.putFileToHref(href, file);
    }

    public static URL getPathToFile(String path) {
        return Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource(path));
    }

    public static Directories getDirectories() {
        return given().spec(YandexApiRequests.initRequestSpecification())
                .when().get("/v1/disk/resources?path=disk:/&limit=1000&sort=name&fields=_embedded.items.name," +
                        "_embedded.items.path")
                .then().statusCode(200)
                .extract().as(Directories.class);
    }

    public static void deleteDirectoryPermanently(String path) {
        given().spec(YandexApiRequests.initRequestSpecification())
                .when().delete("/v1/disk/resources?path=" + path + "&permanently=true")
                .then().statusCode(anyOf(is(202), is(204)));
    }

    public static void deleteDirectoryToTrash(String path) {
        given().spec(YandexApiRequests.initRequestSpecification())
                .when().delete("/v1/disk/resources?path=" + path + "&permanently=false")
                .then().statusCode(anyOf(is(202), is(204)));
    }

        public static void deleteDirectoryInTrash(String path) {
        given().spec(YandexApiRequests.initRequestSpecification())
                .when().delete("/v1/disk/trash/resources?path=" + path)
                .then().statusCode(anyOf(is(202), is(204)));
    }

    public static Directories getTrashDirectories() {
        return given().spec(YandexApiRequests.initRequestSpecification())
                    .when().get("/v1/disk/trash/resources?path=trash:/&limit=1000&fields=_embedded.items.name," +
                        "_embedded.items.path")
                .then().statusCode(200)
                .extract().as(Directories.class);
    }
}
