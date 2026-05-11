ALTER TABLE estoque
ADD COLUMN IF NOT EXISTS equipe_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_estoque_equipe'
    ) THEN
        ALTER TABLE estoque
        ADD CONSTRAINT fk_estoque_equipe
        FOREIGN KEY (equipe_id)
        REFERENCES equipe(id);
    END IF;
END $$;

UPDATE estoque SET equipe_id = NULL WHERE equipe_id IS NOT NULL;