package server.rest.test;

import io.qameta.allure.*;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.rest.config.BaseTest;
import server.rest.utils.LoginUtils;
import server.rest.utils.ProdutoUtils;
import server.rest.utils.UsuarioUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Produtos")
@Feature("Cadastro de Produtos")
public class ProdutosTest extends BaseTest {

    private static final LoginUtils loginUtils = new LoginUtils();
    private static final UsuarioUtils usuarioUtils = new UsuarioUtils();
    private static final ProdutoUtils produtoUtils = new ProdutoUtils();
    private static String bearerToken;
    private static String idProdutoCadastrado;
    private static final List<String> idsUsuariosCadastrados = new ArrayList<>();

    @BeforeEach
    public void setupAdmin() {
        String idUsuario = usuarioUtils.cadastrarUsuario(usuarioUtils.getBodyUsuario("true")).
            then().
            statusCode(HttpStatus.SC_CREATED).
            extract().
            path("_id");
        idsUsuariosCadastrados.add(idUsuario);

        bearerToken = loginUtils.realizaLogin("/login", loginUtils.getBodyLogin(usuarioUtils.getEmail(), usuarioUtils.getPassword()))
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
            idProdutoCadastrado = null;
        }

        if (!idsUsuariosCadastrados.isEmpty()) {
            for (String idUsuarioCadastrado : idsUsuariosCadastrados) {
                usuarioUtils.excluirUsuario(idUsuarioCadastrado);
                System.out.println("ID do usuário excluído: " + idUsuarioCadastrado);
            }
            idsUsuariosCadastrados.clear();
        }
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("Cadastrar Produto Com Sucesso Usando Token de Administrador")
    @Description("Este teste realiza o cadastro de um produto com Sucesso utilizando token de administrador")
    @DisplayName("Deve Cadastrar Produto com Sucesso Usando Token de Administrador")
    public void cadastrarProdutoComoAdmin() {

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

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("Não deve cadastrar produto com usuário comum")
    @Description("Este teste tenta realizar o cadastro de um produto utilizando token de usuário comum")
    @DisplayName("Não Deve Cadastrar Produto com Usuário Comum")
    public void naoDeveCadastrarProdutoComUsuarioComum() {

        Allure.step("Cadastrando um usuário comum para o teste");
        UsuarioUtils usuarioUtilsComum = new UsuarioUtils();
        String bodyUsuario = usuarioUtilsComum.getBodyUsuario("false");
        String idUsuarioComum = usuarioUtilsComum.cadastrarUsuario(bodyUsuario).
                then().
                statusCode(HttpStatus.SC_CREATED).
                extract().
                path("_id");

        String bearerTokenUsuarioComum = loginUtils.realizaLogin("/login", loginUtils.getBodyLogin(usuarioUtilsComum.getEmail(), usuarioUtilsComum.getPassword()))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("authorization");

        String bodyProduto = produtoUtils.getBodyproduto();

        produtoUtils.cadastraProduto("/produtos", bodyProduto, bearerTokenUsuarioComum).
                then().
                statusCode(HttpStatus.SC_FORBIDDEN).
                body("message", equalTo("Rota exclusiva para administradores"));

        idsUsuariosCadastrados.add(idUsuarioComum);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Não deve cadastrar produto sem token de autenticação")
    @Description("Este teste tenta realizar o cadastro de um produto sem fornecer um token de autenticação, esperando uma resposta de erro")
    @DisplayName("Não Deve Cadastrar Produto sem Token de Autenticação")
    public void naoDeveCadastrarProdutoSemTokenDeAutenticacao() {

        bearerToken= loginUtils.realizaLogin("/login", loginUtils.getBodyLogin(usuarioUtils.getEmail(), usuarioUtils.getPassword()))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("authorization");

        String bodyProduto = produtoUtils.getBodyproduto();

        produtoUtils.cadastraProduto("/produtos", bodyProduto, "").
                then().
                statusCode(HttpStatus.SC_UNAUTHORIZED).
                body("message", equalTo("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais"));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Não deve cadastrar produto com nome duplicado")
    @Description("Este teste tenta realizar o cadastro de um produto com nome duplicado, esperando uma resposta de erro")
    @DisplayName("Não Deve Cadastrar Produto com Nome Duplicado")
    public void naoDeveCadastrarProdutoComNomeDuplicado() {

        String nomeProdutoDuplicado = produtoUtils.gerarNomeProduto();
        String bodyProduto = produtoUtils.getBodyproduto(nomeProdutoDuplicado);

        Allure.step("Cadastrando o primeiro produto com sucesso");
        idProdutoCadastrado = produtoUtils.cadastraProduto("/produtos", bodyProduto, bearerToken).
                then().
                statusCode(HttpStatus.SC_CREATED).
                body("message", equalTo("Cadastro realizado com sucesso")).
                body("_id", notNullValue()).
                extract().
                path("_id");

        Allure.step("Tentando cadastrar um segundo produto com o mesmo nome");
        produtoUtils.cadastraProduto("/produtos", produtoUtils.getBodyproduto(nomeProdutoDuplicado), bearerToken).
                then().
                statusCode(HttpStatus.SC_BAD_REQUEST).
                body("message", equalTo("Já existe produto com esse nome"));
    }
}
