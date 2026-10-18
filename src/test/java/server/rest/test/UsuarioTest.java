package server.rest.test;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import server.rest.config.BaseTest;
import server.rest.utils.UsuarioUtils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Gestão de Usuários")
@Feature("Cadastro de Usuário")
public class UsuarioTest extends BaseTest {

    public static UsuarioUtils usuarioUtils = new UsuarioUtils();
    static String idUsuarioCadastrado;

    @AfterAll
    public static void excluirUsuario() {
        if (idUsuarioCadastrado != null) {
            usuarioUtils.excluirUsuario(idUsuarioCadastrado);
        }
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Cadastro de usuário com sucesso")
    @Description("Este teste realiza o cadastro de um usuário com sucesso na API Serverest")
    @DisplayName("Deve cadastrar um usuário administrador com sucesso")
    public void cadastrarUsuario() {

        String body_cadastrar_usuario = usuarioUtils.getBodyUsuario("true");

        Response response_cadastro = usuarioUtils.cadastrarUsuario(body_cadastrar_usuario)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue())
                .extract().response();

        idUsuarioCadastrado = response_cadastro.jsonPath().getString("_id");
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Cadastro de usuário - Email em branco")
    @Description("Este teste realizar o cadastro de um usuário com email em branco na API Serverest")
    @DisplayName("Não deve permitir o cadastro de um usuário com email em branco")
    public void cadastrarUsuarioComEmailEmBranco() {

        String body_cadastrar_usuario = usuarioUtils.getBodyUsuario("true", "");

        usuarioUtils.cadastrarUsuario(body_cadastrar_usuario)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("email", equalTo("email não pode ficar em branco"));

    }

    @ParameterizedTest
    @Severity(SeverityLevel.CRITICAL)
    @Story("Cadastro de usuário - Email inválido")
    @Description("Este teste realizar o cadastro de um usuário com email inválido na API Serverest")
    @DisplayName("Não deve permitir o cadastro de um usuário com email inválido")
    @ValueSource(strings = {
            "email_sem_arroba.com",
            "email@",
            "@dominio.com",
            "email@dominio",
            "email com espaco@qa.com"
    })
    public void cadastrarUsuarioComEmailInvalido(String emailInvalido) {

        String body_cadastrar_usuario = usuarioUtils.getBodyUsuario("true", emailInvalido);

        usuarioUtils.cadastrarUsuario(body_cadastrar_usuario)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("email", equalTo("email deve ser um email válido"));

    }
}
