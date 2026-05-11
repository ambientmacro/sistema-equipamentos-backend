// src/components/painel/PainelFeedback.jsx

export default function PainelFeedback({ loading, erro, ok }) {
    if (loading) {
        return (
            <div className="operacoes-feedback">
                Carregando painel...
            </div>
        );
    }

    if (erro) {
        let mensagemErro = "";

        if (typeof erro === "string") {
            mensagemErro = erro;
        } else if (erro?.response?.data?.mensagem) {
            mensagemErro = erro.response.data.mensagem;
        } else if (erro?.message) {
            mensagemErro = erro.message;
        } else {
            mensagemErro = JSON.stringify(erro);
        }

        return (
            <div className="operacoes-feedback erro">
                {mensagemErro}
            </div>
        );
    }

    if (ok) {
        return (
            <div className="operacoes-feedback sucesso">
                {ok}
            </div>
        );
    }

    return null;
}