CREATE INDEX IF NOT EXISTS idx_estoque_ativo_nome
ON estoque (ativo, nome_equipamento);

CREATE INDEX IF NOT EXISTS idx_execucao_data_desc
ON execucao (data DESC);

CREATE INDEX IF NOT EXISTS idx_execucao_estoque_data_desc
ON execucao (estoque_id, data DESC);

CREATE INDEX IF NOT EXISTS idx_manutencao_equipamento_status
ON manutencao (equipamento_id, status);