package ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import utils.HttpNumber;
import utils.LedDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *      A classe Controller controla toda a UI principal, o sistema de dígitos funciona da seguinte forma:
 *
 *  - O método createNewDigits() primeiramente retira todos os dígitos para depois adicionar de acordo com o número.
 *      Ex: O número 89 criará dois dígitos, o 102 três dígitos.
 *  - O método getSegments(int digitIndex) retorna os segmentos do dígito indicado pelo parâmetro.
 *      Ex: Se digitIndex = 0 e o número for igual a 68, o método retornará os segmentos para formar o número 6,
 *      se digitIndex = 1 retornará os segmentos para formar o número 8.
 *
 */
public class Controller {
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private VBox textSizeBox, colorBox;
    @FXML
    private ComboBox<String> comboBox;
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox firstDigit, secondDigit, thirdDigit;
    @FXML
    private HBox numberBox;
    @FXML
    private ImageView textSize, textColor;
    @FXML
    private Label digitCounter, resultLabel;
    @FXML
    private TextField textField;
    @FXML
    private Button sendButton, newRound;

    /**
     *      Inicializa os Nodes com os estados certos.
     */
    public void initialize() {
        Image textImg = new Image(Objects.requireNonNull(
                getClass().getClassLoader().getResource("format_size-24px.png")).toExternalForm()); // Coloca imagem
        Image palletImg = new Image(Objects.requireNonNull(
                getClass().getClassLoader().getResource("palette-24px.png")).toExternalForm()); // Coloca imagem
        String[] options = {"0.6", "0.7", "0.8", "0.9", "1.0", "1.1"}; // Cria as opções de tamanho
        comboBox.getItems().setAll(FXCollections.observableArrayList(options)); // Adiciona as opções
        // Remove a comboBox de tamanhos para somente o icone ficar visível
        this.textSizeBox.getChildren().remove(this.comboBox);
        // Remove o colorPicker para somente o ícone ficar visível
        this.colorBox.getChildren().remove(this.colorPicker);
        // Coloca o valor da cor padrão do LED no colorPicker
        this.colorPicker.setValue(Color.rgb(229, 32, 98));
        this.textSize.setImage(textImg);
        this.textColor.setImage(palletImg);
        // Esconde o botão de nova partida
        this.newRound.setVisible(false);
        waitServer();
    }

    /**
     *      Desativa todos os inputs do usuário até que o número seja adquirido pelo HttpNumber
     */
    private void waitServer() {
        this.resultLabel.setText("");
        disableInput();
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(() -> {
            // Apaga o número anterior e mostra o 0.
            Platform.runLater(() -> LedDisplay.displayNumber(0, true));
            while (HttpNumber.getNumber() == -1) { // Espera até o servidor responder
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(this::enableInput);
            exec.shutdown();
        });
    }

    /**
     *      Tira todos os dígitos para em seguida colocar eles denovo de acordo com o parâmetro
     * @param digitQuantity Número de dígitos a ser colocado
     */
    public void createNewDigits(int digitQuantity) {
        numberBox.getChildren().clear();
        if (digitQuantity < 1 || digitQuantity > 3) {
            throw new IllegalArgumentException("Quantidade de dígitos excedeu o limite");
        }
        switch (digitQuantity) {
            case 1:
                numberBox.getChildren().add(firstDigit);
                break;
            case 2:
                numberBox.getChildren().add(firstDigit);
                numberBox.getChildren().add(secondDigit);
                break;
            case 3:
                numberBox.getChildren().add(firstDigit);
                numberBox.getChildren().add(secondDigit);
                numberBox.getChildren().add(thirdDigit);
                break;
        }
    }

    /**
     *      Aumenta o tamanho do display por meio de um aumento na escala no eixo X e no eixo Y
     * @param rate Taxa de aumento de escala, 1 = escala original
     */

    public void increaseDigitSize(double rate) {
        this.numberBox.setScaleX(rate);
        this.numberBox.setScaleY(rate);
    }

    /**
     *  Permite que o usuário dê input
     */
    private void enableInput() {
        this.textField.setDisable(false);
        this.sendButton.setDisable(false);
        this.digitCounter.setText("0/3");
    }

    /**
     *  Nega input ao usuário
     */

    private void disableInput() {
        this.textField.setDisable(true);
        this.sendButton.setDisable(true);
        this.digitCounter.setText("");
        this.textField.setText("");
    }

    /**
     *      Verifica se o palpite do usuário é maior, menor ou igual ao número retornado pelo HttpNumber
     * @param number Palpite do usuário
     */
    private void verifyResult(int number) {
        int answer = HttpNumber.getNumber();
        if (number == answer) {
            this.resultLabel.setText("Acertou!");
            this.newRound.setVisible(true);
            disableInput();
        } else if (number > answer) {
            this.resultLabel.setText("É menor");
        } else {
            this.resultLabel.setText("É maior");
        }
    }

    /**
     *      Chamado sempre que o HttpNumber identificar um código de erro
     * @param httpCode Código http
     */
    public void showError(int httpCode) {
        Platform.runLater(() -> {
            this.resultLabel.setText("Erro");
            this.newRound.setVisible(true);
            LedDisplay.displayNumber(httpCode, true);
            disableInput();
        });
    }

    /**
     *      Retorna os segmentos para o dígito indicado no parâmetro
     * @param digitIndex Índice do dígito
     * @return Segmentos do dígito indicado
     */
    public List<ImageView> getSegments(int digitIndex) {
        VBox currentDigitBox = (VBox) numberBox.getChildren().get(digitIndex); // pega a VBox contendo o dígito...
        // indicado pelo parâmetro
        List<ImageView> segments = new ArrayList<>();
        for (Node segment : currentDigitBox.getChildren()) {
            if (segment instanceof ImageView) segments.add((ImageView) segment);
        }
        return segments;
    }

    /**
     *      Chamado sempre que o botão enviar for clicado, esse método tenta invocar parseInt(), se não conseguir
     *  significa que o input fornecido pelo usuário é inválido. Em seguida verifica o palpite.
     */
    @FXML
    private void onSend() {
        int number;
        try {
            number = Integer.parseInt(textField.getText());
            verifyResult(number); // Verifíca o resultado
            LedDisplay.displayNumber(number, false);
        } catch (NumberFormatException e) {
            System.out.println("Número inválido");
        }
    }

    /**
     *      É chamado quando o botão de nova partida é clicado. Esse método esconde o botão,
     *  da request para pegar um novo número e espera até o servidor responder.
     */
    @FXML
    private void onNewRound() {
        this.newRound.setVisible(false);
        HttpNumber.request();
        waitServer();
    }

    /**
     *      É chamado sempre quando o usuário selecionar a caixa de texto e soltar uma tecla. Esse método valida
     *  o input do usuário e bloqueia o botão de enviar caso o input seja inválido. Achei que seria uma boa
     *  implementação desativar o botão de enviar e mostrar "Número inválido" quando o usuário digitar um número fora
     *  dos limites impostos (1 >= x <= 300).
     */
    @FXML
    private void onKeyReleased() {
        String number;
        try {
            Integer.parseInt(textField.getText()); // Tenta dar parse para verificar se é um número inteiro
            number = textField.getText();
            if (Integer.parseInt(number) < 1 || Integer.parseInt(number) > 300) { // Número fora dos limites
                this.digitCounter.setText("Número inválido");
                this.sendButton.setDisable(true);
                return;
            }
            if (number.length() > 3) { // Número tem muitos dígitos
                this.digitCounter.setText("Número inválido");
                this.sendButton.setDisable(true);
            } else { // Número válido
                this.digitCounter.setText(number.length() + "/3");
                this.sendButton.setDisable(false);

            }
        } catch (NumberFormatException e) { // Não é um número
            if (this.textField.getText().isEmpty()) { // Caixa de texto vazia
                this.sendButton.setDisable(false);
                this.digitCounter.setText("0/3");
            } else { // Input totalmente inválido
                this.sendButton.setDisable(true);
                this.digitCounter.setText("Número Inválido");
            }
        }
    }

    /**
     *      É chamado sempre que o "botão" de mudar o tamanho do texto for clicado. Esse método remove o botão e coloca
     *  em seu lugar a comboBox com os tamanhos disponíveis.
     */
    @FXML
    private void onTextSize() {
        this.textSizeBox.getChildren().remove(this.textSize);
        this.textSizeBox.getChildren().add(this.comboBox);
        this.comboBox.show();
    }

    /**
     *      É chamado sempre que o usuário escolher um valor de tamanho disponível. Esse método remove a comboBox e coloca
     *  em seu lugar o "botão" de de mudar de tamanho com os tamanhos disponíveis, depois invoca o increaseDigitSize()
     *  regular o tamanho.
     */
    @FXML
    private void onComboBox(){
        increaseDigitSize(Double.parseDouble(this.comboBox.getValue()));
        this.textSizeBox.getChildren().remove(this.comboBox);
        this.textSizeBox.getChildren().add(this.textSize);
        this.comboBox.hide();
    }

    /**
     *      É chamado quando o usuário escolher a cor para o LED. Esse método remove o colorPicker e coloca
     *  em seu lugar o "botão" para mudar de cor.
     */
    @FXML
    private void onColorPicker(){
        Color newColor = this.colorPicker.getValue();
        LedDisplay.changeColor(newColor);
        this.colorBox.getChildren().remove(this.colorPicker);
        this.colorBox.getChildren().add(this.textColor);
    }

    /**
     *      É chamado quando o "botão" de mudar cor for clicado. Esse método remove o "botão" para mudar de cor e coloca
     *  em seu lugar o colorPicker.
     */
    @FXML
    private void onTextColor(){
        this.colorBox.getChildren().remove(this.textColor);
        this.colorBox.getChildren().add(this.colorPicker);
    }

}
