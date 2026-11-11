package doefacil;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;
import java.util.*;

public final class Main {
    private final Scanner entrada=new Scanner(System.in);
    private final DoacaoService servico=new DoacaoService();
    private String ler(String msg){System.out.print(msg+": ");return entrada.nextLine().strip();}
    private long id(String msg){return Long.parseLong(ler(msg));}
    private int quantidade(String msg){return Integer.parseInt(ler(msg));}
    private LocalDate data(String msg){return LocalDate.parse(ler(msg+" (AAAA-MM-DD)"));}
    private ItemDoacao novoItem(){
        String tipo=ler("Tipo: R = roupa, A = alimento").toUpperCase(Locale.ROOT);
        String desc=ler("Descrição");int qtd=quantidade("Quantidade recebida");
        return switch(tipo){
            case "R"->new Roupa(0,desc,qtd,0,ler("Tamanho"),ler("Condição"));
            case "A"->new Alimento(0,desc,qtd,0,data("Validade"));
            default->throw new IllegalArgumentException("Escolha R ou A.");
        };
    }
    private void mostrar(Doacao d){
        System.out.printf("Doação #%d | doador #%d | recebida em %s%n",d.id(),d.doadorId(),d.dataRecebimento());
        for(var i:d.itens())System.out.printf("  Item #%d: %s | %s | recebido: %d | saldo: %d%n",
            i.id(),i.descricao(),i.descreverDetalhes(),i.quantidadeRecebida(),i.calcularSaldo());
    }
    private void executar(String opcao)throws SQLException {
        switch(opcao){
            case "1"->System.out.println("Doador #"+servico.cadastrarDoador(new Doador(0,ler("Nome"),ler("Telefone")))+" cadastrado.");
            case "2"->{var todos=servico.listarDoadores();if(todos.isEmpty())System.out.println("Nenhum doador cadastrado.");
                for(var d:todos)System.out.printf("#%d | %s | %s%n",d.id(),d.nome(),d.telefone());}
            case "3"->{long doador=id("ID do doador");var dia=data("Recebimento");var itens=new ArrayList<ItemDoacao>();
                do{itens.add(novoItem());}while("S".equalsIgnoreCase(ler("Adicionar outro item? S/N")));
                System.out.println("Doação #"+servico.receber(new Doacao(0,doador,dia,itens))+" registrada.");}
            case "4"->{var lista=servico.listar();if(lista.isEmpty())System.out.println("Nenhuma doação.");lista.forEach(this::mostrar);}
            case "5"->{long d=id("ID da doação");mostrar(servico.buscar(d));servico.editarCabecalho(d,id("ID do doador"),data("Data de recebimento"));System.out.println("Doação atualizada.");}
            case "6"->{long d=id("ID da doação");mostrar(servico.buscar(d));long i=id("ID do item a corrigir");servico.corrigirItem(d,i,novoItem());System.out.println("Item atualizado.");}
            case "7"->{long d=id("ID da doação");mostrar(servico.buscar(d));if("SIM".equalsIgnoreCase(ler("Digite SIM para excluir"))){servico.excluir(d);System.out.println("Doação excluída.");}}
            case "8"->{long d=id("ID da doação");mostrar(servico.buscar(d));servico.distribuir(d,new Distribuicao(id("ID do item"),data("Saída"),quantidade("Quantidade a distribuir")));System.out.println("Distribuição registrada.");}
            case "9"->mostrar(servico.buscar(id("ID da doação")));
            default->System.out.println("Opção inválida.");
        }
    }
    private void menu(){
        System.out.println("DoeFácil | Controle de doações");
        while(true){
            System.out.println("\n1 Cadastrar doador  | 2 Listar doadores | 3 Receber doação\n4 Listar doações    | 5 Editar doação   | 6 Corrigir item\n7 Excluir doação    | 8 Distribuir item | 9 Consultar por ID | 0 Sair");
            System.out.print("> ");if(!entrada.hasNextLine())return;
            String escolha=entrada.nextLine().strip();if(escolha.equals("0"))return;
            try{executar(escolha);}
            catch(NumberFormatException|DateTimeParseException e){System.out.println("Número ou data inválida. Operação cancelada.");}
            catch(IllegalArgumentException e){System.out.println(e.getMessage());}
            catch(SQLException e){System.out.println("Falha no MySQL ("+e.getSQLState()+"): "+e.getMessage());}
            catch(NoSuchElementException e){return;}
        }
    }
    public static void main(String[] args){new Main().menu();}
}
