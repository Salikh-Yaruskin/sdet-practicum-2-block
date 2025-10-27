package helper;

import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class BaseRequests {

    public static RequestSpecification initRequestSpecification() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();

        PreemptiveBasicAuthScheme basic = new PreemptiveBasicAuthScheme ();
        basic.setUserName(PropertyProvider.getInstance().getProperty("basic.auth.username"));
        basic.setPassword(PropertyProvider.getInstance().getProperty("basic.auth.password"));

        return requestSpecBuilder
                .setContentType(ContentType.JSON)
                .setBaseUri(PropertyProvider.getInstance().getProperty("wb.baseurl"))
                .setAccept(ContentType.JSON)
                .setAuth(basic)
                .build();
    }
}
