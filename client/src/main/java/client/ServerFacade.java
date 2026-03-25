package client;
import com.google.gson.Gson;
import model.requestsandresults.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.*;
import java.net.http.*;
import exception.ResponseException;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }
    public RegisterResult register(RegisterRequest registerRequest) throws ResponseException{
        var request = buildRequest("POST", "/user", registerRequest);
        var response = sendRequest(request);
        return handleResponse(response, RegisterResult.class);
    }
    public LoginResult login(LoginRequest loginRequest) throws ResponseException{
        var request = buildRequest("POST", "/session", loginRequest);
        var response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }
    public void joinGame(JoinGameRequest joinGameRequest) {

    }
    public ListGamesResult listGames() {

    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException{
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ResponseException(ResponseException.Code.ServerError, e.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

}
