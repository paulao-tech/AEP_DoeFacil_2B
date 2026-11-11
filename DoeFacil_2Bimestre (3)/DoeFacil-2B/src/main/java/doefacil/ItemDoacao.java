package doefacil;
import java.time.LocalDate;
import java.util.Objects;

/** O saldo consultado é derivado do histórico de saídas, não de uma segunda coluna. */
public abstract class ItemDoacao {
    private final long id;
    private final String descricao;
    private final int quantidadeRecebida;
    private final int distribuido;
    protected ItemDoacao(long id, String descricao, int quantidade, int distribuido) {
        if (quantidade <= 0 || distribuido < 0 || distribuido > quantidade)
            throw new IllegalArgumentException("Quantidades de item inválidas.");
        this.id=id; this.descricao=texto(descricao,150); this.quantidadeRecebida=quantidade; this.distribuido=distribuido;
    }
    static String texto(String s,int max) {
        if(s==null || s.isBlank() || s.strip().length()>max) throw new IllegalArgumentException("Informe texto de 1 a "+max+" caracteres.");
        return s.strip();
    }
    public long id(){return id;}
    public String descricao(){return descricao;}
    public int quantidadeRecebida(){return quantidadeRecebida;}
    public int calcularSaldo(){return quantidadeRecebida-distribuido;}
    public abstract String descreverDetalhes();
    public void validarSaida(int qtd, LocalDate dia) {
        Objects.requireNonNull(dia);
        if(qtd<=0 || qtd>calcularSaldo()) throw new IllegalArgumentException("Quantidade inválida ou saldo insuficiente.");
    }
}
