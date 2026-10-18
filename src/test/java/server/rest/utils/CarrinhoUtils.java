package server.rest.utils;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.hamcrest.Condition;

import static io.restassured.RestAssured.given;

public class CarrinhoUtils {

    private final String PATH_CARRINHO = "/carrinhos";

    public String getBodyCarrinho(String idProduto, int quantidade) {
        return "{\n" +
                "  \"produtos\": [\n" +
                "    {\n" +
                "      \"idProduto\": \"" + idProduto + "\",\n" +
                "      \"quantidade\": " + quantidade + "\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    @Step("Cadastrando carrinho na API ServeRest")
    public Response cadastrandoCarrinho(String bodyCarrinho, String bearerToken) {
        return given().
                header("Authorization", bearerToken).
                body(bodyCarrinho).
                log().body().
                when().
                post(PATH_CARRINHO);
    }


    @Step("Buscar carrinho pelo ID usando path param ID carrinho")
    public Response buscarCarrinhoPorPathId(String idCarrinho, String bearerToken) {
        return given().
                header("Authorization", bearerToken).
                pathParam("_id", idCarrinho).
                log().all().
                when().
                get(PATH_CARRINHO + "/{_id}");
    }

    @Step("Concluir compra do carrinho")
    public Response concluirCompra(String bearerToken) {
        return given().
                header("Authorization", bearerToken).
                log().all().
                when().
                delete(PATH_CARRINHO + "/concluir-compra");
    }

    @Step("Cancelar compra do carrinho")
    public Response cancelarCompra(String bearerToken) {
        return given().
                header("Authorization", bearerToken).
                log().all().
                when().
                delete(PATH_CARRINHO + "/cancelar-compra");
    }

    @Step("Buscar carrinho pelo ID usando query param ID carrinho")
    public Response buscarCarrinhoPorQueryParamIdCarrinho(String idCarrinho, String bearerToken) {
        return given().
                header("Authorization", bearerToken).
                queryParam("_id", idCarrinho).
                log().all().
                when().
                get(PATH_CARRINHO);
    }

    @Step("Buscar carrinho pelo ID usando query param ID Usuário")
    public Response buscarCarrinhoPorQueryParamIdUsuario(String idUsuario, String bearerToken) {
        return given().
                header("Authorization", bearerToken).
                queryParam("idUsuario", idUsuario).
                log().all().
                when().
                get(PATH_CARRINHO);
    }
}
