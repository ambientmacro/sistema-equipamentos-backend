ALTER TABLE checklist_modelo
ADD COLUMN IF NOT EXISTS arquivo_conteudo BYTEA;

ALTER TABLE checklist_modelo
ADD COLUMN IF NOT EXISTS arquivo_nome VARCHAR(255);

ALTER TABLE checklist_modelo
ADD COLUMN IF NOT EXISTS arquivo_original_nome VARCHAR(255);

ALTER TABLE checklist_modelo
ADD COLUMN IF NOT EXISTS arquivo_caminho TEXT;