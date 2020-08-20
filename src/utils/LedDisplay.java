package utils;

import javafx.application.Platform;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import ui.Main;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  Esta classe representa um display de LED.
 * O campo number diz qual número o display está apresentando.
 * Sempre que o número for trocado o metodo displayNumber() será chamado, assim fazendo com que
 * o display mude para o novo número.
 *
 *  Os segmentos de cada numeral de LED estão organizados para que sejam identificados individualmente
 *  (começando do meio, segmento de cima e depois seguindo uma ordem horária).
 *
 *  A subclasse LedNumber guarda quais segmentos devem ser ativados para se formar o número que dá nome a cada campo.
 *  Para o funcionamento correto desses padrões de ativação, os segmentos DEVEM estar na ordem descrita no parágrafo
 *  acima. Isso é feito se adicionando as ImageView's na ordem descrita, e depois as posicionando corretamente.
 *  Ex: A primeira ImageView será a do segmento do meio, a segunda será o segmento do topo e assim sucessivamente.
 *
 *  A "ativação" de cada segmento é simplesmente a troca de uma imagem vazia (sem cor) para uma imagem cheia (com cor).
 *  Mais detalhes do sistema de dígitos na classe Controller.
 */
public final class LedDisplay {
    private static String number; // Número exibido no display

    private static Image FULL_SEGMENT_IMAGE; // Imagem com cor
    private static Image EMPTY_SEGMENT_IMAGE; // Imagem sem cor
    private static final Map<String, List<Boolean>> numberPatterns = new HashMap<>(); // Padrões de ativação (0 - 9)

    private LedDisplay() { // Sem instânciação
    }
    static { // Inicialização estática é a melhor opção
        LedDisplay.number = "0";
        LedDisplay.FULL_SEGMENT_IMAGE = new Image(Objects.requireNonNull(
                Main.class.getClassLoader().getResource("full_segment.png")).toExternalForm());
        LedDisplay.EMPTY_SEGMENT_IMAGE = new Image(Objects.requireNonNull(
                Main.class.getClassLoader().getResource("empty_segment.png")).toExternalForm());

        // Esse HashMap irá traduzir os números em suas respectivas sequencias de ativação
        LedDisplay.numberPatterns.put("0", LedNumber.ZERO);
        LedDisplay.numberPatterns.put("1", LedNumber.ONE);
        LedDisplay.numberPatterns.put("2", LedNumber.TWO);
        LedDisplay.numberPatterns.put("3", LedNumber.THREE);
        LedDisplay.numberPatterns.put("4", LedNumber.FOUR);
        LedDisplay.numberPatterns.put("5", LedNumber.FIVE);
        LedDisplay.numberPatterns.put("6", LedNumber.SIX);
        LedDisplay.numberPatterns.put("7", LedNumber.SEVEN);
        LedDisplay.numberPatterns.put("8", LedNumber.EIGHT);
        LedDisplay.numberPatterns.put("9", LedNumber.NINE);
    }

    /**
     *  Esse método faz o display mostrar o número 0 no início.
     * */
    public static void initialize(){
        Main.getController().createNewDigits(1); // Cria um dígito
        LedDisplay.writeDigit(number, Main.getController().getSegments(0));
    }

    /**
     *      Faz o display mostrar o número passado (newNumber) para o método.
     *  Primeiro é verificado quantos dígitos o número tem, em seguida este número é passado para a classe Controller
     *  para que ela os coloque na UI. Por fim o método writeDigit() é chamado para cada dígito do número newNumber.
     *      Números fora do limite imposto pela prova não irão ser mostrados.
     *
     * @param newNumber Número a ser mostrado.
     * @param disableBounds Indica se o limite (1 >= newNumber <= 300) deve ser usado.
     * */
    public static void displayNumber(int newNumber, boolean disableBounds) {
        if ((newNumber < 1 || newNumber > 300) && !disableBounds){
            System.out.println("Número fora dos limites");
            return;
        }
        LedDisplay.number = String.valueOf(newNumber);

        List<String> digits = Arrays.asList(LedDisplay.number.split("")); // Lista com os dígitos
        int digitQuantity = digits.size();

        Main.getController().createNewDigits(digitQuantity); // Cria os dígitos na UI (ainda sem os valores corretos).
        for (int i = 0; i < digits.size(); i++){
            String digit = digits.get(i);
            // Pega os segmentos do dígito correspondente (ver mais em Controller).
            writeDigit(digit, Main.getController().getSegments(i));
        }
    }

    /**
     *      Aceita o numero a ser mostrado juntamente com os segmentos correspondentes a posição do dígito.
     * Em seguida usa o padrão de ativação do parâmetro digit para ativar os segmentos.
     *
     * @param digit Numero a ser mostrado.
     * @param segments Os 7 segmentos do dígito ordenados corretamente.
     */

    private static void writeDigit(String digit, List<ImageView> segments){
        System.out.println("Writing number " + digit);
        List<Boolean> pattern = LedDisplay.numberPatterns.get(digit); // Padrão de ativação para digit
        for (int i = 0 ; i < pattern.size(); i++){
            // Esse loop irá "ativar" os segmentos necessários para formar o número.
            boolean isOn = pattern.get(i);
            ImageView currentSegment = segments.get(i);
            if (isOn){ // Liga
                currentSegment.setImage(LedDisplay.FULL_SEGMENT_IMAGE);
            }else { // Desliga
                currentSegment.setImage(EMPTY_SEGMENT_IMAGE);
            }
        }
    }

    /**
     *      Cria uma WritableImage e compara com a imagem base (FULL_SEGMENT_IMAGE) para pintar a nova cor (newColor)
     * apenas nos pixels que não estejam transparentes.
     *
     * @param newColor Cor a ser pintada nos pixels não transparentes.
     */
    public static void changeColor(Color newColor){
        // Implementação concorrente para melhor performance.
        ExecutorService exec =  Executors.newSingleThreadExecutor();
        exec.execute(()->{
            WritableImage writableImage = new WritableImage((int)FULL_SEGMENT_IMAGE.getWidth(),
                    (int)FULL_SEGMENT_IMAGE.getHeight());
            PixelWriter writer = writableImage.getPixelWriter();
            PixelReader reader = FULL_SEGMENT_IMAGE.getPixelReader(); // Leitor para saber qual píxel é transparente

            Color emptyPixel = reader.getColor(0,0); // O pixel na posição (0, 0) é transparente
            for (int y = 0 ; y < FULL_SEGMENT_IMAGE.getHeight() ; y++){ // Itera sobre todos os pixels na Vertical
                for (int x = 0; x < FULL_SEGMENT_IMAGE.getWidth(); x++){ // Itera sobre todos os pixels na Horizontal
                    if (!reader.getColor(x, y).toString().equals(emptyPixel.toString())){
                        // Se o pixel atual estiver pintado a condição será verdadeira
                        writer.setColor(x, y, newColor); // Pinta o pixel na WritableImage
                    }
                }
            }
            FULL_SEGMENT_IMAGE = writableImage;
            // Atualiza o número exibido no display para a nova cor.
            Platform.runLater(()->displayNumber(Integer.parseInt(number), true));
            exec.shutdown();
        });
    }

    /**
     *      Retorna o número que está sendo exibido
     * @return Número que está sendo exibido
     */
    public String getNumber() {
        return number;
    }

    private static final class LedNumber {
        private static final List<Boolean> ZERO;
        private static final List<Boolean> ONE;
        private static final List<Boolean> TWO;
        private static final List<Boolean> THREE;
        private static final List<Boolean> FOUR;
        private static final List<Boolean> FIVE;
        private static final List<Boolean> SIX;
        private static final List<Boolean> SEVEN;
        private static final List<Boolean> EIGHT;
        private static final List<Boolean> NINE;

        static{
            // Padrões de ativação dos 7 segmentos para os respectivos números.
            ZERO = List.of(false, true, true, true, true, true, true);
            ONE = List.of(false, false, true, true, false, false, false);
            TWO = List.of(true, true, true, false, true, true, false);
            THREE = List.of(true, true, true, true, true, false, false);
            FOUR = List.of(true, false, true, true, false, false, true);
            FIVE = List.of(true, true, false, true, true, false, true);
            SIX = List.of(true, true, false, true, true, true, true);
            SEVEN = List.of(false, true, true, true, false, false, false);
            EIGHT = List.of(true, true, true, true, true, true, true);
            NINE = List.of(true, true, true, true, false, false, true);
        }
    }
}
