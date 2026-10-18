package server.rest.utils;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

import java.util.Locale;

import static io.restassured.RestAssured.given;

public class ProdutoUtils {

    private final Faker faker = new Faker(new Locale("pt-BR"));

    public String getBodyproduto() {
        return getBodyproduto(gerarNomeProduto());
    }

    public String getBodyproduto(String nomeProduto) {
        String preco = String.valueOf(faker.number().numberBetween(10, 500));
        String descricao = faker.commerce().material();
        String quantidade = String.valueOf(faker.number().numberBetween(1, 50));

        return "{\n" +
                "  \"nome\": \"" + nomeProduto + "\",\n" +
                "  \"preco\": " + preco + ",\n" +
                "  \"descricao\": \"" + descricao + "\",\n" +
                "  \"quantidade\": " + quantidade + "\n" +
                "}";
    }

    @Step("Cadastrando produto na API ServeRest")
    public Response cadastraProduto(String pathProdutos, String bodyCadastrarProduto, String bearerToken) {
        return given().
                header("Authorization", bearerToken).
                body(bodyCadastrarProduto).
                log().all().
        when().
                log().all().
                post(pathProdutos);
    }

    @Step("Excluindo produto na API ServeRest")
    public void excluirProduto(String idProdutoCadastrado, String bearerToken) {
        given().
                header("Authorization", bearerToken).
                pathParam("_id", idProdutoCadastrado).
                log().all().
        when().
            delete("/produtos" + "/{_id}").
        then().
                log().all().
                statusCode(HttpStatus.SC_OK);
    }

    public String gerarNomeProduto() {
        return faker.commerce().productName();
    }

}
