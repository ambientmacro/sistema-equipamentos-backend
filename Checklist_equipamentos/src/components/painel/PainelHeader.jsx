// src/components/painel/PainelHeader.jsx

export default function PainelHeader({ isAdminOrDev, totalEquipes, hoje }) {
    return (
        <section className="operacoes-header">
            <div>
                <p className="operacoes-kicker">Painel</p>

                <h1>
                    {isAdminOrDev
                        ? "Equipes com Login"
                        : "Checklist da Equipe"}
                </h1>

                <p className="operacoes-subtitle">
                    {isAdminOrDev
                        ? "Clique na equipe e no equipamento para ver o checklist feito da semana."
                        : "Cada equipamento só pode ter um checklist por dia. Depois disso ele vira visualização."}
                </p>
            </div>

            <div className="operacoes-summary">
                <span>{isAdminOrDev ? "Total de Equipes" : "Hoje"}</span>

                <strong>
                    {isAdminOrDev ? totalEquipes : hoje}
                </strong>
            </div>
        </section>
    );
}
