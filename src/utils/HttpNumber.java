package utils;

import ui.Main;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *      Essa classe tem o propósito de dar o request e pegar o número que representa a resposta. O request é feito no
 * método request(), esse por sua vez chama o método parseJSON() para ler o arquivo recebido do servidor e retirar
 * o número por meio de uma expressão regular.
 */
public final class HttpNumber {
    private static final Pattern valueRegex = Pattern.compile("\"value\":(\\d+)");
    private static int number =  -1; // Número obtido, -1 significa que ele ainda não foi retornado
    private static int httpCode; // Código retornado do servidor

    public HttpNumber() { // Sem instânciação
    }

    /**
     *      Dá o request lê o arquivo por meio de um InputStreamReader e salva o código recebido do servidor no campo
     *  number. Invoca o método parseJSON com uma String contendo o conteúdo do arquivo e o código recebido do servidor
     */
    public static void request() {
        number = -1;
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://us-central1-ss-devops.cloudfunctions.net/rand?min=1&max=300");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try{ // Caso a conexão expire
                    httpCode = connection.getResponseCode();
                }catch (SocketTimeoutException | SSLException e){
                    Main.getController().showError(connection.getConnectTimeout());
                    return;
                }
                BufferedReader reader;
                String line;
                StringBuilder sb = new StringBuilder();
                if (httpCode == 200) { // Sucesso
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                } else { // Erro
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                String json = sb.toString();
                parseJSON(json, httpCode == 200);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                assert connection != null;
                connection.disconnect();
                exec.shutdown();
            }
        });
    }

    /**
     *      Retira o número da String json caso o boolean success = true (Código http é 200). Se success = false
     * significa que um erro ocorreu, logo o metodo showError() do Controller será invocado para se exibir a mensagem e
     * o código de erro na UI.
     * @param json
     * @param success
     */
    private static void parseJSON(String json, boolean success) {
        Matcher matcher = valueRegex.matcher(json);
        if (success){
            if (matcher.find()) {
                number = Integer.parseInt(matcher.group(1));
                System.out.println(number);
            }
        }else{
            Main.getController().showError(httpCode);
        }
    }

    /**
     *      Retorna o código http obtido do servidor.
     * @return Código http.
     */

    public static int getNumber() {
        return number;
    }
}
