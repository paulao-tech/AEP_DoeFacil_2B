package doefacil;
import java.time.LocalDate;
import java.util.Objects;
public record Distribuicao(long itemId,LocalDate data,int quantidade) {
    public Distribuicao {Objects.requireNonNull(data); if(quantidade<=0) throw new IllegalArgumentException("A quantidade deve ser positiva.");}
}
