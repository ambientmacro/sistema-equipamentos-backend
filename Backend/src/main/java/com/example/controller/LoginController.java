package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.entity.Usuario;
import com.example.exception.BusinessException;
import com.example.service.LoginService;
import com.example.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        try {

            System.out.println("Username recebido: " + request.getUsername());
            System.out.println("Senha recebida: " + request.getSenha());

            Usuario user = loginService.authenticate(
                    request.getUsername(),
                    request.getSenha()
            );

            return ResponseEntity.ok(
                    new LoginResponse("Login realizado com sucesso",
                            usuarioService.toResponse(user))
            );

        } catch (BusinessException e) {

            String mensagem;

            switch (e.getMessage()) {

                case "USUARIO_NAO_ENCONTRADO":
                    mensagem = "Usuário não cadastrado";
                    break;

                case "SENHA_INVALIDA":
                    mensagem = "Senha inválida";
                    break;

                case "USUARIO_INATIVO":
                    mensagem = "Usuário inativo";
                    break;

                case "EQUIPE_INVALIDA":
                    mensagem = "Equipe inválida";
                    break;

                case "DADOS_INVALIDOS":
                    mensagem = "Preencha username e senha";
                    break;

                default:
                    mensagem = "Erro no login";
            }

            return ResponseEntity.status(401)
                    .body(new LoginResponse(mensagem, null));
        }
    }
}