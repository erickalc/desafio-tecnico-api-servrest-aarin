package server.rest.test;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.rest.config.BaseTest;
import server.rest.utils.LoginUtils;
import server.rest.utils.UsuarioUtils;

import static org.hamcrest.Matchers.*;
import static server.rest.test.UsuarioTest.usuarioUtils;

@Epic("Login")
@Feature("Login")
public class LoginTest extends BaseTest {

    static String authorization;
    private String idUsuarioCadastrado;

    @AfterEach
    public  void tearDown() {
        if (idUsuarioCadastrado != null) {
            usuarioUtils.excluirUsuario(idUsuarioCadastrado);
        }

    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("Login com sucesso")
    @Description("Este teste realiza o login com sucesso e retorna token Bearer válido")
    @DisplayName("Deve realizar login com sucesso e retornar token Bearer válido")
    public void loginComSucesso(){
        // Cadastrando um usuário para realizar o login
        UsuarioUtils usuarioUtils = new UsuarioUtils();
        String bodyCadastro = usuarioUtils.getBodyUsuario();

        idUsuarioCadastrado = usuarioUtils.cadastrarUsuario(bodyCadastro).
                then().
                statusCode(HttpStatus.SC_CREATED).
                extract().jsonPath().getString("_id");


        // Realizando o login com o usuário cadastrado
        LoginUtils loginUtils = new LoginUtils();
        String bodyLogin = loginUtils.getBodyLogin(usuarioUtils.getEmail(), usuarioUtils.getPassword());

        Response response_login = loginUtils.realizaLogin("/login", bodyLogin)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("authorization", notNullValue())
                .body("authorization", startsWith("Bearer "))
                .extract().response();

        authorization = response_login.getBody().jsonPath().getString("authorization");
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Falha de Autenticação")
    @Description("Tentativa de login com senha inválida, deve retornar erro de autenticação")
    @DisplayName("Validação de Regra de Negócio - Login com senha inválida")
    public void naoDeveAutenticarComSenhaInvalida() {
        // Cadastrando um usuário para realizar o login
        UsuarioUtils usuarioUtils = new UsuarioUtils();
        String bodyCadastro = usuarioUtils.getBodyUsuario();

        idUsuarioCadastrado = usuarioUtils.cadastrarUsuario(bodyCadastro).
                then().
                statusCode(HttpStatus.SC_CREATED).
                extract().jsonPath().getString("_id");

        // Realizando o login com senha Incorreta
        LoginUtils loginUtils = new LoginUtils();
        String bodyLogin = loginUtils.getBodyLogin(usuarioUtils.getEmail(), "senhaIncorreta");

        loginUtils.realizaLogin("/login", bodyLogin)
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("message", notNullValue())
                .body("message", endsWithIgnoringCase("inválidos"))
                .extract().response();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Falha de Autenticação")
    @Description("Tentativa de login com e-mail em branco, deve retornar erro de autenticação")
    @DisplayName("Validação de Regra de Negócio - Com email em branco")
    public void naoDeveAutenticarSemEmail() {
        // Cadastrando um usuário para realizar o login
        UsuarioUtils usuarioUtils = new UsuarioUtils();
        String bodyCadastro = usuarioUtils.getBodyUsuario();

        idUsuarioCadastrado = usuarioUtils.cadastrarUsuario(bodyCadastro).
                then().
                statusCode(HttpStatus.SC_CREATED).
                extract().jsonPath().getString("_id");

        // Realizando o login sem enviar o email
        LoginUtils loginUtils = new LoginUtils();
        String bodyLogin = loginUtils.getBodyLogin("", usuarioUtils.getPassword());

        loginUtils.realizaLogin("/login", bodyLogin)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("email", notNullValue())
                .body("email", equalTo("email não pode ficar em branco"))
                .extract().response();
    }
}
