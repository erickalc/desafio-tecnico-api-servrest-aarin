package server.rest.utils;

import com.github.javafaker.Faker;
import io.qameta.allure.Epic;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Locale;

import static io.restassured.RestAssured.given;


public class UsuarioUtils {

    private static final String ENDPOINT_USUARIOS = "/usuarios";

    private static final Faker faker = new Faker(new Locale("pt-BR"));
    private final String nome = faker.name().firstName();
    private final String sobrenome = faker.name().lastName();
    private final String email = (nome + "." + sobrenome + "@qa.com.br").replaceAll("\\s+", "").toLowerCase();
    private final String password = (sobrenome + "$" + "123").replaceAll("\\s+", "").toLowerCase();

    public String getBodyUsuario(String admin){
        return getBodyUsuario(admin, this.email);
    }

    public String getBodyUsuario(String admin, String emailInformado){

        return "{\n" +
                "  \"nome\": " + "\""+ nome + " " + sobrenome + "\"" + ",\n" +
                "  \"email\": " + "\"" + emailInformado + "\"" + ",\n" +
                "  \"password\": " + "\"" + password + "\"" + ",\n" +
                "  \"administrador\": " + "\"" + admin + "\"" + "\n" +
                "}";
    }

    @Step("Cadastrando um usuário")
    public Response cadastrarUsuario(String bodyCadastrarUsuario) {

        return given().
                body(bodyCadastrarUsuario).
                log().all().
        when().
                post(ENDPOINT_USUARIOS);
    }

    @Step("Excluindo um usuário")
    public void excluirUsuario(String id) {
        given().
                pathParam("_id", id).
                log().all().
        when().
                delete(ENDPOINT_USUARIOS + "/{_id}");
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }
}
