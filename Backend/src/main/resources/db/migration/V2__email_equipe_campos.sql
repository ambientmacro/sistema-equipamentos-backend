DO $$
BEGIN
    -- EMAIL
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'email'
    ) THEN
        ALTER TABLE email
        ADD COLUMN IF NOT EXISTS tipo VARCHAR(30) DEFAULT 'DESTINATARIO';
    END IF;

    -- EQUIPE
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'equipe'
    ) THEN
        ALTER TABLE equipe
        ADD COLUMN IF NOT EXISTS ativo BOOLEAN DEFAULT TRUE;
    END IF;

    -- USUARIO
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'usuario'
    ) THEN
        ALTER TABLE usuario
        ADD COLUMN IF NOT EXISTS ativo BOOLEAN DEFAULT TRUE;
    END IF;
END $$;


-- ESSE PODE FICAR NORMAL (porque usa IF NOT EXISTS)

-- ALTER TABLE email ...
-- ALTER TABLE equipe ...
-- ALTER TABLE usuario ...

CREATE TABLE IF NOT EXISTS email_remetente (
    id SERIAL PRIMARY KEY,
    email VARCHAR(300),
    senha VARCHAR(255) NOT NULL,
    nome VARCHAR(300),
    setor VARCHAR(300)
);
