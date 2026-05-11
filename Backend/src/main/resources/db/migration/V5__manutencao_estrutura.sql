ALTER TABLE manutencao
    ADD COLUMN IF NOT EXISTS equipe_ultima_id BIGINT,
    ADD COLUMN IF NOT EXISTS equipe_conclusao_id BIGINT,
    ADD COLUMN IF NOT EXISTS data_entrada TIMESTAMP,
    ADD COLUMN IF NOT EXISTS data_saida TIMESTAMP,
    ADD COLUMN IF NOT EXISTS valor_total NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS valor_unitario_equipamento NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS descricao TEXT,
    ADD COLUMN IF NOT EXISTS foto_nota_fiscal TEXT;

UPDATE manutencao
SET data_entrada = COALESCE(data_entrada, CURRENT_TIMESTAMP)
WHERE data_entrada IS NULL;

ALTER TABLE manutencao
    ALTER COLUMN data_entrada SET DEFAULT CURRENT_TIMESTAMP;

UPDATE manutencao
SET status = 'PENDENTE'
WHERE status = 'SOLICITACAO';

ALTER TABLE manutencao
DROP CONSTRAINT IF EXISTS manutencao_status_check;

ALTER TABLE manutencao
ADD CONSTRAINT manutencao_status_check
CHECK (status IN ('PENDENTE', 'CONCLUIDO', 'INUTILIZADO'));