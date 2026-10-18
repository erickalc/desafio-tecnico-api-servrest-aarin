package server.rest.test;

import io.qameta.allure.*;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.rest.config.BaseTest;
import server.rest.utils.CarrinhoUtils;
import server.rest.utils.LoginUtils;
import server.rest.utils.ProdutoUtils;
import server.rest.utils.UsuarioUtils;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Gestão de Carrinho")
@Feature("Fluxo de Compra")
public class CarrinhoTest extends BaseTest {

    private static String bearerToken;
    private String idProdutoCadastrado;

    @BeforeEach
    public void setupMassaCadastro() {
        // Cria um usuário Admin para realizar o login e obter o token Bearer
        UsuarioUtils usuarioUtils = new UsuarioUtils();
        LoginUtils loginUtils = new LoginUtils();

        String bodyCadastro = usuarioUtils.getBodyUsuario();
        usuarioUtils.cadastrarUsuario(bodyCadastro).
                then().
                statusCode(HttpStatus.SC_CREATED);

        // Realiza o login do usuário Admin e obtém o token Bearer
        String bodyLogin = loginUtils.getBodyLogin(usuarioUtils.getEmail(), usuarioUtils.getPassword());
        bearerToken = loginUtils.realizaLogin("/login", bodyLogin).
                then().
                statusCode(HttpStatus.SC_OK).
                extract().
                path("authorization");

        // Cadastra um produto com o token Bearer obtido
        ProdutoUtils produtoUtils = new ProdutoUtils();
        String bodyProduto = produtoUtils.getBodyproduto();
        idProdutoCadastrado = produtoUtils.cadastraProduto("/produtos", bodyProduto, bearerToken).
                then().
                statusCode(HttpStatus.SC_CREATED).
                extract().
                path("_id");
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("Fluxo Principal de Compra Com Sucesso")
    @Description("Este teste realiza o fluxo principal de compra, incluindo cadastro de usuário, login, cadastro de produto, adição ao carrinho e finalização da compra")
    @DisplayName("Deve realizar o fluxo principal de compra com sucesso e verificar a sensibilização do estoque do produto")
    public void fluxoPrincipalDeCompraComSucesso() {

        int estoqueinicial =
        given().
                header("Authorization", bearerToken).
                pathParam("_id", idProdutoCadastrado).
                log().all().
        when().
                get("/produtos/{_id}").
        then().
                log().all().
                statusCode(HttpStatus.SC_OK).
                body("_id", equalTo(idProdutoCadastrado)).
                body("quantidade", notNullValue()).
                extract().
                path("quantidade");

        CarrinhoUtils carrinhoUtils = new CarrinhoUtils();
        int quantidadeItensCarrinho = 1;

        String bodyCarrinho = carrinhoUtils.getBodyCarrinho(idProdutoCadastrado, quantidadeItensCarrinho);

        String id_Carrinho = carrinhoUtils.cadastrandoCarrinho(bodyCarrinho, bearerToken).
                then().
                statusCode(HttpStatus.SC_CREATED).
                body("message", equalTo("Cadastro realizado com sucesso")).
                body("_id", notNullValue()).
                extract().
                path("_id");

        Allure.step("Verificar baixa do estoque ao adicionar ao carrinho", () -> {
            verificaAtualizacaoEstoque(estoqueinicial, quantidadeItensCarrinho);
        });

        Allure.step("Busca informações do carrinho para verificar se o produto foi adicionado corretamente", () -> {
            carrinhoUtils.buscarCarrinho(id_Carrinho, bearerToken).
                    then().
                    log().all().
                    statusCode(HttpStatus.SC_OK).
                    body("_id", equalTo(id_Carrinho)).
                    body("produtos[0].idProduto", equalTo(idProdutoCadastrado)).
                    body("produtos[0].quantidade", equalTo(1));
        });

        // // Conclui a compra do carrinho
        carrinhoUtils.concluirCompra(bearerToken).
                then().
                log().all().
                statusCode(HttpStatus.SC_OK).
                body("message", equalTo("Registro excluído com sucesso"));

        // Busca informações do carrinho após a conclusão da compra
        carrinhoUtils.buscarCarrinho(id_Carrinho, bearerToken).
                then().
                log().all().
                statusCode(HttpStatus.SC_BAD_REQUEST).
                body("message", equalTo("Carrinho não encontrado"));

        Allure.step("Verificar atualização do estoque do produto após a compra ou cancelamento");
        verificaAtualizacaoEstoque(estoqueinicial, quantidadeItensCarrinho);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Fluxo Principal de Compra Cancelamento")
    @Description("Este teste realiza o fluxo principal de compra, incluindo cadastro de usuário, login, cadastro de produto, adição ao carrinho e cancelamento da compra")
    @DisplayName("Deve realizar o fluxo principal de compra com sucesso e verificar que estoque do produto foi reabastecido após o cancelamento da compra")
    public void fluxoPrincipalDeCompraCancelamento() {

        int estoqueinicial =
                given().
                        header("Authorization", bearerToken).
                        pathParam("_id", idProdutoCadastrado).
                        log().all().
                        when().
                        get("/produtos/{_id}").
                        then().
                        log().all().
                        statusCode(HttpStatus.SC_OK).
                        body("_id", equalTo(idProdutoCadastrado)).
                        body("quantidade", notNullValue()).
                        extract().
                        path("quantidade");

        CarrinhoUtils carrinhoUtils = new CarrinhoUtils();
        int quantidadeItensCarrinho = 1;

        String bodyCarrinho = carrinhoUtils.getBodyCarrinho(idProdutoCadastrado, quantidadeItensCarrinho);

        String id_Carrinho = carrinhoUtils.cadastrandoCarrinho(bodyCarrinho, bearerToken).
                then().
                statusCode(HttpStatus.SC_CREATED).
                body("message", equalTo("Cadastro realizado com sucesso")).
                body("_id", notNullValue()).
                extract().
                path("_id");

        Allure.step("Verificar baixa do estoque ao adicionar ao carrinho", () -> {
            verificaAtualizacaoEstoque(estoqueinicial, quantidadeItensCarrinho);
        });

        Allure.step("Busca informações do carrinho para verificar se o produto foi adicionado corretamente", () -> {
            carrinhoUtils.buscarCarrinho(id_Carrinho, bearerToken).
                    then().
                    log().all().
                    statusCode(HttpStatus.SC_OK).
                    body("_id", equalTo(id_Carrinho)).
                    body("produtos[0].idProduto", equalTo(idProdutoCadastrado)).
                    body("produtos[0].quantidade", equalTo(1));
        });

        // Cancela a Compra
        carrinhoUtils.cancelarCompra(bearerToken).
                then().
                log().all().
                statusCode(HttpStatus.SC_OK).
                body("message", equalTo("Registro excluído com sucesso. Estoque dos produtos reabastecido"));

        // Busca informações do carrinho após a conclusão da compra
        carrinhoUtils.buscarCarrinho(id_Carrinho, bearerToken).
                then().
                log().all().
                statusCode(HttpStatus.SC_BAD_REQUEST).
                body("message", equalTo("Carrinho não encontrado"));

        //Busca informações do produto para verificar se a quantidade foi atualizada após a compra
        Allure.step("Verificar atualização do estoque do produto após a compra ou cancelamento");
        verificaAtualizacaoEstoque(estoqueinicial, 0);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Fluxo de Exceção")
    @Description("Não deve permitir ao usuário adicionar mais de um carrinho")
    @DisplayName("Não deve permitir ao usuário adicionar mais de um carrinho")
    public void naoDevePermitirAdicionarMaisDeUmCarrinho() {

        CarrinhoUtils carrinhoUtils = new CarrinhoUtils();
        int quantidadeItensCarrinho = 1;

        String bodyCarrinho = carrinhoUtils.getBodyCarrinho(idProdutoCadastrado, quantidadeItensCarrinho);

        String id_Carrinho = carrinhoUtils.cadastrandoCarrinho(bodyCarrinho, bearerToken).
                then().
                statusCode(HttpStatus.SC_CREATED).
                body("message", equalTo("Cadastro realizado com sucesso")).
                body("_id", notNullValue()).
                extract().
                path("_id");

        Allure.step("Cadastrando um segundo carrinho e verificando se a API retorna o erro esperado");
        carrinhoUtils.cadastrandoCarrinho(bodyCarrinho, bearerToken).
                then().
                statusCode(HttpStatus.SC_BAD_REQUEST).
                body("message", equalTo("Não é permitido ter mais de 1 carrinho"));

        Allure.step("Busca informações do carrinho para verificar se o produto foi adicionado corretamente", () -> {
            carrinhoUtils.buscarCarrinho(id_Carrinho, bearerToken).
                    then().
                    log().all().
                    statusCode(HttpStatus.SC_OK).
                    body("_id", equalTo(id_Carrinho)).
                    body("produtos[0].idProduto", equalTo(idProdutoCadastrado)).
                    body("produtos[0].quantidade", equalTo(1));
        });
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Fluxo de Exceção")
    @Description("Não deve permitir comprar quantidade de produtos maior que a quantidade em estoque")
    @DisplayName("Não deve permitir comprar quantidade de produtos maior que a quantidade em estoque")
    public void naoDevePermitirComprarQuantidadeMaiorQueEstoque() {

        int estoqueinicial =
                given().
                        header("Authorization", bearerToken).
                        pathParam("_id", idProdutoCadastrado).
                        log().all().
                when().
                        get("/produtos/{_id}").
                then().
                        log().all().
                        statusCode(HttpStatus.SC_OK).
                        body("_id", equalTo(idProdutoCadastrado)).
                        body("quantidade", notNullValue()).
                        extract().
                        path("quantidade");

        CarrinhoUtils carrinhoUtils = new CarrinhoUtils();
        int quantidadeItensCarrinho = estoqueinicial + 1; // Tentando adicionar mais itens do que o estoque disponível

        String bodyCarrinho = carrinhoUtils.getBodyCarrinho(idProdutoCadastrado, quantidadeItensCarrinho);

        Allure.step("Cadastrando um carrinho com quantidade maior que o estoque e verificando se a API retorna o erro esperado");
        carrinhoUtils.cadastrandoCarrinho(bodyCarrinho, bearerToken).
                then().
                statusCode(HttpStatus.SC_BAD_REQUEST).
                body("message", equalTo("Produto não possui quantidade suficiente")).
                body("item.idProduto", equalTo(idProdutoCadastrado)).
                body("item.quantidade", equalTo(quantidadeItensCarrinho)).
                body("item.quantidadeEstoque", equalTo(estoqueinicial));

        Allure.step("Verifica que o estoque do produto não foi alterado após a tentativa de adicionar ao carrinho");
        verificaAtualizacaoEstoque(estoqueinicial, 0);
    }


    private void verificaAtualizacaoEstoque(int estoqueinicial, int quantidadeItensCarrinho) {

        given().
                header("Authorization", bearerToken).
                pathParam("_id", idProdutoCadastrado).
                log().all().
        when().
                get("/produtos/{_id}").
        then().
                log().all().
                statusCode(HttpStatus.SC_OK).
                body("_id", equalTo(idProdutoCadastrado)).
                body("quantidade", equalTo(estoqueinicial - quantidadeItensCarrinho));
    }
}
