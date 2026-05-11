DO $$
BEGIN
    BEGIN
        EXECUTE format(
            'ALTER DATABASE %I SET timezone TO ''America/Sao_Paulo''',
            current_database()
        );
    EXCEPTION
        WHEN insufficient_privilege THEN
            RAISE NOTICE 'Sem permissao para ALTER DATABASE.';
    END;
END $$;

SET TIME ZONE 'America/Sao_Paulo';