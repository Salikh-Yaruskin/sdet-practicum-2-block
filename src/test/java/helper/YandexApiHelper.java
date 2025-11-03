package helper;

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
}
