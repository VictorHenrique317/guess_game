package ui;

import javafx.fxml.FXML;

/**
 *      Essa classe controla a tela inicial de seleção de tamanho para o LED. Cada botão tem seu método que invoca o
 *  increaseDigitSize() do Controller, passando o valor escrito no respectivo botão.
 */
public class InitialController {
    public void initialize(){

    }

    @FXML
    private void onFirstSize(){ // Primeiro botão
        Main.initMainScreen();
        Main.getController().increaseDigitSize(0.6);

    }

    @FXML
    private void onSecondSize(){ // Segundo botão
        Main.initMainScreen();
        Main.getController().increaseDigitSize(0.7);
        
    }

    @FXML
    private void onThirdSize(){ // Terceiro botão
        Main.initMainScreen();
        Main.getController().increaseDigitSize(0.8);
        
    }

    @FXML
    private void onFourthSize(){ // Quarto botão
        Main.initMainScreen();
        Main.getController().increaseDigitSize(0.9);
        
    }

    @FXML
    private void onFifthSize(){ // Quinto botão
        Main.initMainScreen();
        Main.getController().increaseDigitSize(1);
        
    }

    @FXML
    private void onSixthSize(){ // Sexto botão
        Main.initMainScreen();
        Main.getController().increaseDigitSize(1.1);
        
    }
}
