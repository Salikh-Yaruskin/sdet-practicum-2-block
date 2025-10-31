package helper;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class YandexApiRequests {

    public static RequestSpecification initRequestSpecification() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        return new RequestSpecBuilder()
                .setBaseUri(PropertyProvider.getInstance().getProperty("yandex.api.baseurl"))
                .addHeader("Authorization", "OAuth " + PropertyProvider.getInstance()
                        .getProperty("yandex.api.token"))
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .addFilter(new AllureRestAssured())
                .log(LogDetail.URI)
                .build();
    }
}
