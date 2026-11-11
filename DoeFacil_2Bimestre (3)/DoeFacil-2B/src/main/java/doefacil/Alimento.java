package doefacil;
import java.time.LocalDate;
import java.util.Objects;
public final class Alimento extends ItemDoacao {
    private final LocalDate validade;
    public Alimento(long id,String descricao,int qtd,int saiu,LocalDate validade) {
        super(id,descricao,qtd,saiu);this.validade=Objects.requireNonNull(validade);
    }
    public LocalDate validade(){return validade;}
    @Override public String descreverDetalhes(){return "Alimento | validade: "+validade;}
    @Override public void validarSaida(int qtd,LocalDate dia){
        super.validarSaida(qtd,dia);
        if(dia.isAfter(validade)) throw new IllegalArgumentException("Alimento vencido na data da distribuição.");
    }
}
