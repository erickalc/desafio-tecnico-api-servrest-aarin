package server.rest.test;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.rest.config.BaseTest;
import server.rest.utils.LoginUtils;
import server.rest.utils.ProdutoUtils;
import server.rest.utils.UsuarioUtils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ProdutosTest extends BaseTest {

    private static final LoginUtils loginUtils = new LoginUtils();
    private static final UsuarioUtils usuarioUtis = new UsuarioUtils();
    private static final ProdutoUtils produtoUtils = new ProdutoUtils();
    private static String bearerToken;
    private static String idProdutoCadastrado;
    private static String idUsuarioCadastrado;

    @BeforeEach
    public void setupAdmin() {
        idUsuarioCadastrado = usuarioUtis.cadastrarUsuario(usuarioUtis.getBodyUsuario()).
            then().
            statusCode(HttpStatus.SC_CREATED).
            extract().
            path("_id");

        bearerToken = loginUtils.realizaLogin("/login", loginUtils.getBodyLogin(usuarioUtis.getEmail(), usuarioUtis.getPassword()))
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .path("authorization");
    }

    @AfterEach
    public void tearDown() {
        if (idProdutoCadastrado != null) {
            produtoUtils.excluirProduto(idProdutoCadastrado, bearerToken);
            System.out.println("ID do produto excluído: " + idProdutoCadastrado);
        }
        if (idUsuarioCadastrado != null) {
            usuarioUtis.excluirUsuario(idUsuarioCadastrado);
            System.out.println("ID do usuário excluído: " + idUsuarioCadastrado);
        }
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("Cadastrar Produto")
    @Description("Este teste realiza o cadastro de um produto com Sucesso utilizando token de administrador")
    @DisplayName("Deve Cadastrar Produto com Sucesso Usando Token de Administrador")
    public void cadastrarProduto() {

        String bodyProduto = produtoUtils.getBodyproduto();

        idProdutoCadastrado =  produtoUtils.cadastraProduto("/produtos", bodyProduto, bearerToken).
                then().
                statusCode(HttpStatus.SC_CREATED).
                body("message", equalTo("Cadastro realizado com sucesso")).
                body("_id", notNullValue()).
                extract().
                path("_id");

        System.out.println("ID do produto cadastrado: " + idProdutoCadastrado);
    }

}
