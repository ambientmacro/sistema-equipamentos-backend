CREATE TABLE IF NOT EXISTS equipamentosLocados (
    id BIGSERIAL PRIMARY KEY,
    nome_locado VARCHAR(100) NOT NULL,
    empresa_id BIGINT NOT NULL,
    quantidade INT NOT NULL DEFAULT 0,
    valor_locacao NUMERIC(10,2) NOT NULL DEFAULT 0,
    valor_unitario NUMERIC(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pecas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    quantidade INT NOT NULL DEFAULT 0,
    equipamento_locado_id BIGINT NOT NULL,
    FOREIGN KEY (equipamento_locado_id)
    REFERENCES equipamentosLocados(id)
    ON DELETE CASCADE
);

ALTER TABLE equipamentosLocados
ADD COLUMN IF NOT EXISTS foto_url TEXT;

ALTER TABLE equipamentosLocados
ADD COLUMN IF NOT EXISTS indenizacao_valor NUMERIC(10,2);