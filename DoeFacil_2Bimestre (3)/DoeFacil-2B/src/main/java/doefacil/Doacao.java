package doefacil;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
public record Doacao(long id,long doadorId,LocalDate dataRecebimento,List<ItemDoacao> itens) {
    public Doacao {
        if(doadorId<=0) throw new IllegalArgumentException("Informe o ID do doador.");
        Objects.requireNonNull(dataRecebimento);itens=List.copyOf(itens);
        if(itens.isEmpty()) throw new IllegalArgumentException("Uma doação precisa ter pelo menos um item.");
    }
}
