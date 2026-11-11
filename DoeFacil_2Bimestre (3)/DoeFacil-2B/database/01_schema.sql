-- Execute em um banco vazio, previamente selecionado. Não apaga dados.
CREATE TABLE doadores (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, nome VARCHAR(120) NOT NULL, telefone VARCHAR(30) NOT NULL
) ENGINE=InnoDB;
CREATE TABLE doacoes (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, doador_id BIGINT NOT NULL, data_recebimento DATE NOT NULL,
 FOREIGN KEY (doador_id) REFERENCES doadores(id)
) ENGINE=InnoDB;
CREATE TABLE itens_doacao (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, doacao_id BIGINT NOT NULL, tipo VARCHAR(10) NOT NULL,
 descricao VARCHAR(150) NOT NULL, quantidade_recebida INT NOT NULL CHECK (quantidade_recebida > 0),
 tamanho VARCHAR(30), condicao VARCHAR(60), validade DATE,
 FOREIGN KEY (doacao_id) REFERENCES doacoes(id) ON DELETE CASCADE,
 CHECK ((tipo='ROUPA' AND tamanho IS NOT NULL AND condicao IS NOT NULL AND validade IS NULL)
     OR (tipo='ALIMENTO' AND validade IS NOT NULL AND tamanho IS NULL AND condicao IS NULL))
) ENGINE=InnoDB;
CREATE TABLE distribuicoes (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, item_id BIGINT NOT NULL, data DATE NOT NULL,
 quantidade INT NOT NULL CHECK (quantidade > 0),
 FOREIGN KEY (item_id) REFERENCES itens_doacao(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
