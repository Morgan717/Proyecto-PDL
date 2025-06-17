package clasesAux;

import java.util.ArrayList;
import java.util.List;

/**
 * Almacena tokens para proporcionar contexto al analizador semántico
 */
public class TokenBuffer {
    private final List<TokenInfo> tokens = new ArrayList<>();
    private int currentIndex = 0;

    public static class TokenInfo {
        public final String token;
        public final String lexema;
        public final String tipo;

        public TokenInfo(String token, String lexema, String tipo) {
            this.token = token;
            this.lexema = lexema;
            this.tipo = tipo;
        }
    }

    /**
     * Añade un nuevo token al buffer
     */
    public void addToken(String token, String lexema, String tipo) {
        tokens.add(new TokenInfo(token, lexema, tipo));
    }

    /**
     * Obtiene un token relativo a la posición actual
     * @param offset Desplazamiento desde la posición actual
     */
    public TokenInfo getToken(int offset) {
        int index = currentIndex + offset;
        if (index >= 0 && index < tokens.size()) {
            return tokens.get(index);
        }
        return null;
    }

    /**
     * Avanza al siguiente token en el buffer
     */
    public void consumeToken() {
        if (currentIndex < tokens.size()) {
            currentIndex++;
        }
    }

    /**
     * Obtiene todos los tokens del buffer
     */
    public List<TokenInfo> getAllTokens() {
        return new ArrayList<>(tokens);
    }

    /**
     * Obtiene la posición actual en el buffer
     */
    public int getCurrentPosition() {
        return currentIndex;
    }

    public void clear(){
        tokens.clear();
        currentIndex = 0;
    }
}
